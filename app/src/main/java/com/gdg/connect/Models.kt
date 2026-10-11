package com.gdg.connect

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val venue: String,
    val price: String,
    val hosts: List<String>,
    val organizingClub: String,
    val tags: List<String>,
    val capacity: Int,
    val reserved: Int,
    val zone: String = "Asia/Kolkata",
    val sample: Boolean = false
) {
    val seatsLeft: Int get() = (capacity - reserved).coerceAtLeast(0)
    fun isPast(now: Instant) = startsAt <= now
    fun matches(query: String): Boolean {
        val words = query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter { it.isNotBlank() }
        val text = (listOf(title, venue, organizingClub) + tags).joinToString(" ").lowercase(Locale.ROOT)
        return words.all { it in text }
    }
    fun dateLabel(): String = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH).withZone(ZoneId.of(zone)).format(startsAt)
    fun timeLabel(): String = DateTimeFormatter.ofPattern("h:mm a z", Locale.ENGLISH).withZone(ZoneId.of(zone)).format(startsAt)
}

enum class AppTheme { SYSTEM, LIGHT, DARK }
data class User(val uid: String, val email: String, val verified: Boolean)
data class FeedState(val events: List<Event> = emptyList(), val offline: Boolean = false)
