package com.gdg.connect

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Patterns
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConnectViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    var theme by mutableStateOf(runCatching { AppTheme.valueOf(preferences.getString("theme", "SYSTEM")!!) }.getOrDefault(AppTheme.SYSTEM)); private set
    var user by mutableStateOf<User?>(null); private set
    var events by mutableStateOf<List<Event>>(emptyList()); private set
    var reservations by mutableStateOf<Set<String>>(emptySet()); private set
    var offline by mutableStateOf(false); private set
    var loading by mutableStateOf(false); private set
    var busy by mutableStateOf(false); private set
    var message by mutableStateOf<String?>(null); private set
    var confirmation by mutableStateOf<String?>(null); private set
    var feedError by mutableStateOf<String?>(null); private set
    var scheduleError by mutableStateOf<String?>(null); private set
    val backendConfigured: Boolean get() = auth != null
    private var auth: FirebaseAuth? = null
    private var store: EventStore? = null
    private var sessionUid: String? = null
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val current = firebaseAuth.currentUser
        user = current?.let { User(it.uid, it.email.orEmpty(), it.isEmailVerified) }
        if (sessionUid != current?.uid) {
            sessionUid = current?.uid
            store?.close()
            events = emptyList(); reservations = emptySet(); feedError = null; scheduleError = null
            if (current != null) refresh()
        }
    }
    init {
        if (FirebaseApp.initializeApp(application) != null) {
            auth = FirebaseAuth.getInstance()
            auth?.addAuthStateListener(authListener)
        }
    }
    fun refresh() {
        val uid = user?.uid ?: return
        store?.close()
        loading = true; feedError = null; scheduleError = null
        store = FirebaseEventStore()
        store?.listen(uid, { feed -> events = feed.events; offline = feed.offline; loading = false; feedError = null }, { ids -> reservations = ids; scheduleError = null }, { error -> loading = false; feedError = error }, { error -> scheduleError = error })
    }
    fun authenticate(email: String, password: String, create: Boolean) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { message = "Enter a valid email address."; return }
        if (create && password.length < 8) { message = "Use a password with at least 8 characters."; return }
        perform {
            val service = requireAuth()
            if (create) service.createUserWithEmailAndPassword(email, password).await() else service.signInWithEmailAndPassword(email, password).await()
            if (create) runCatching { service.currentUser?.sendEmailVerification()?.await() }.onFailure { message = "Account created. You can resend verification from Profile." }
        }
    }
    fun resetPassword(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { message = "Enter a valid account email first."; return }
        perform { requireAuth().sendPasswordResetEmail(email).await(); message = "If an account exists, a password reset email has been sent." }
    }
    fun sendVerification() = perform { requireAuth().currentUser?.sendEmailVerification()?.await(); message = "Verification email sent. Check your inbox and spam folder." }
    fun reloadUser() = perform {
        requireAuth().currentUser?.reload()?.await()
        user = auth?.currentUser?.let { User(it.uid, it.email.orEmpty(), it.isEmailVerified) }
        message = if (user?.verified == true) "Your email is verified." else "Email is not verified yet. Open the link in your inbox."
    }
    fun reserve(event: Event) = perform {
        val uid = user?.uid ?: error("Sign in to reserve a seat.")
        check(!offline) { "Connect to the internet to reserve a seat." }
        checkNotNull(store).reserve(uid, event.id)
        confirmation = "Your seat at ${event.title} is confirmed. You can find it in My schedule." + if (event.sample) " This is a sample event for testing." else ""
    }
    fun cancel(event: Event) = perform {
        val uid = user?.uid ?: error("Sign in to manage your reservation.")
        check(!offline) { "Connect to the internet to cancel a reservation." }
        checkNotNull(store).cancel(uid, event.id)
        message = "Reservation cancelled. Your seat has been released."
    }
    fun signOut() { message = null; store?.close(); store = null; auth?.signOut(); user = null; events = emptyList(); reservations = emptySet(); confirmation = null }
    fun setAppTheme(value: AppTheme) { theme = value; preferences.edit().putString("theme", value.name).apply() }
    fun clearMessage() { message = null }
    fun dismissConfirmation() { confirmation = null }
    fun launchIntent(context: Context, intent: Intent) { runCatching { context.startActivity(intent) }.onFailure { message = "No compatible app is installed for this action." } }
    private fun requireAuth(): FirebaseAuth = auth ?: error("Firebase setup is not complete. Install the configured APK after connecting a Firebase project.")
    private fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try { action() } catch (e: Exception) {
                message = when (e) {
                    is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> "Unable to sign in. Check your email and password."
                    is FirebaseAuthUserCollisionException -> "This email already has an account. Sign in or reset your password."
                    is FirebaseNetworkException -> "No connection. Check your internet and try again."
                    is FirebaseTooManyRequestsException -> "Too many attempts. Please wait a moment and try again."
                    is IllegalStateException, is IllegalArgumentException -> e.message ?: "Please try again."
                    else -> "The request could not be completed. Check your connection and try again."
                }
            } finally { busy = false }
        }
    }
    override fun onCleared() { store?.close(); auth?.removeAuthStateListener(authListener); super.onCleared() }
}

interface EventStore {
    fun listen(uid: String, feed: (FeedState) -> Unit, registered: (Set<String>) -> Unit, feedError: (String) -> Unit, scheduleError: (String) -> Unit)
    suspend fun reserve(uid: String, eventId: String)
    suspend fun cancel(uid: String, eventId: String)
    fun close()
}
