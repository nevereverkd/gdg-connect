package com.gdg.connect

import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId

/** Firestore Standard: live reads + offline cache; reservations require online transactions. */
class FirebaseEventStore : EventStore {
    private val db = FirebaseFirestore.getInstance()
    private val listeners = mutableListOf<ListenerRegistration>()
    override fun listen(uid: String, feed: (FeedState) -> Unit, registered: (Set<String>) -> Unit, feedError: (String) -> Unit, scheduleError: (String) -> Unit) {
        close()
        listeners += db.collection("events").whereEqualTo("published", true).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) { feedError("Events could not be loaded. Check your connection and try again."); return@addSnapshotListener }
            if (snapshot != null) {
                try { feed(FeedState(snapshot.documents.map { it.toEvent() }, snapshot.metadata.isFromCache)) }
                catch (_: Exception) { feedError("An event has invalid details. Please contact the organizer.") }
            }
        }
        listeners += db.collection("users").document(uid).collection("registrations").addSnapshotListener { snapshot, error ->
            if (error != null) { scheduleError("Your schedule could not be synced. Please try again."); return@addSnapshotListener }
            if (snapshot != null) registered(snapshot.documents.map { it.id }.toSet())
        }
    }
    override suspend fun reserve(uid: String, eventId: String) {
        val eventRef = db.collection("events").document(eventId)
        val registrationRef = db.collection("users").document(uid).collection("registrations").document(eventId)
        db.runTransaction { transaction ->
            val event = transaction.get(eventRef)
            val registration = transaction.get(registrationRef)
            check(event.exists() && event.getBoolean("published") == true) { "This event is no longer available." }
            check(event.getTimestamp("startsAt")!!.toDate().toInstant() > Instant.now()) { "Registration for this event has closed." }
            check(!registration.exists()) { "You are already registered for this event." }
            val reserved = event.getLong("reserved") ?: 0
            val capacity = event.getLong("capacity") ?: 0
            check(reserved < capacity) { "This event is full. Check again later for available seats." }
            transaction.update(eventRef, "reserved", reserved + 1)
            transaction.set(registrationRef, mapOf("uid" to uid, "eventId" to eventId, "createdAt" to FieldValue.serverTimestamp()))
            null
        }.await()
    }
    override suspend fun cancel(uid: String, eventId: String) {
        val eventRef = db.collection("events").document(eventId)
        val registrationRef = db.collection("users").document(uid).collection("registrations").document(eventId)
        db.runTransaction { transaction ->
            val event = transaction.get(eventRef)
            val registration = transaction.get(registrationRef)
            check(registration.exists()) { "This reservation has already been cancelled." }
            check(event.getTimestamp("startsAt")!!.toDate().toInstant() > Instant.now()) { "This event has already started." }
            val reserved = event.getLong("reserved") ?: 0
            check(reserved > 0) { "Please contact the organizer to cancel this reservation." }
            transaction.update(eventRef, "reserved", reserved - 1)
            transaction.delete(registrationRef)
            null
        }.await()
    }
    override fun close() { listeners.forEach { it.remove() }; listeners.clear() }
    private fun DocumentSnapshot.toEvent(): Event {
        val zone = getString("zone") ?: "Asia/Kolkata"
        ZoneId.of(zone)
        return Event(id, getString("title")!!, getString("description")!!,
            getTimestamp("startsAt")!!.toDate().toInstant(), getTimestamp("endsAt")!!.toDate().toInstant(),
            getString("venue")!!, getString("price")!!, stringList("hosts"), getString("organizingClub")!!,
            stringList("tags"), (getLong("capacity") ?: 0).toInt(), (getLong("reserved") ?: 0).toInt(), zone,
            getBoolean("sample") ?: false)
    }
    private fun DocumentSnapshot.stringList(key: String): List<String> = (get(key) as? List<*>)?.filterIsInstance<String>() ?: emptyList()
}
