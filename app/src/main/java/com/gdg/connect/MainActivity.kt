package com.gdg.connect

import android.os.Bundle
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import kotlinx.coroutines.delay

val Blue = Color(0xFF1A73E8)
val Green = Color(0xFF188038)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { val vm: ConnectViewModel = viewModel(); ConnectApp(vm) }
    }
}

@Composable
fun ConnectApp(vm: ConnectViewModel) {
    val dark = when (vm.theme) { AppTheme.DARK -> true; AppTheme.LIGHT -> false; else -> isSystemInDarkTheme() }
    val currentView = LocalView.current
    SideEffect {
        (currentView.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, currentView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    val scheme = if (dark) darkColorScheme(primary = Color(0xFF9FC3FF), primaryContainer = Color(0xFF15345F), onPrimaryContainer = Color(0xFFD2E3FC), secondaryContainer = Color(0xFF15345F), onSecondaryContainer = Color(0xFFD2E3FC), secondary = Color(0xFF81C995)) else lightColorScheme(primary = Blue, primaryContainer = Color(0xFFE8F0FE), onPrimaryContainer = Color(0xFF174EA6), secondaryContainer = Color(0xFFE8F0FE), onSecondaryContainer = Color(0xFF174EA6), secondary = Green, background = Color(0xFFF8FAFF), surface = Color.White, surfaceVariant = Color(0xFFEDF2FA))
    MaterialTheme(colorScheme = scheme, shapes = Shapes(medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp))) {
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(vm.user?.uid) { snackbar.currentSnackbarData?.dismiss() }
        LaunchedEffect(vm.message) { vm.message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }
        Scaffold(snackbarHost = { SnackbarHost(snackbar, Modifier.padding(bottom = if (vm.user != null) 90.dp else 0.dp)) }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                if (vm.user == null) LoginScreen(vm) else MainScreen(vm)
            }
        }
        vm.confirmation?.let { text ->
            AlertDialog(onDismissRequest = vm::dismissConfirmation,
                icon = { Icon(Icons.Outlined.CheckCircle, null, tint = Green, modifier = Modifier.size(40.dp)) },
                title = { Text("You're on the list!") }, text = { Text(text) },
                confirmButton = { TextButton(onClick = vm::dismissConfirmation) { Text("Great, let's go") } })
        }
    }
}

@Composable
fun GdgMark(modifier: Modifier = Modifier) {
    Canvas(modifier.size(58.dp, 32.dp)) {
        val w = size.width; val h = size.height
        drawLine(Color(0xFFEA4335), Offset(w*.38f, h*.12f), Offset(w*.08f, h*.5f), 9f, StrokeCap.Round)
        drawLine(Color(0xFF4285F4), Offset(w*.08f, h*.5f), Offset(w*.38f, h*.88f), 9f, StrokeCap.Round)
        drawLine(Color(0xFF34A853), Offset(w*.62f, h*.12f), Offset(w*.92f, h*.5f), 9f, StrokeCap.Round)
        drawLine(Color(0xFFFBBC04), Offset(w*.92f, h*.5f), Offset(w*.62f, h*.88f), 9f, StrokeCap.Round)
    }
}

@Composable
fun LoginScreen(vm: ConnectViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var create by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    var reset by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(26.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Spacer(Modifier.height(16.dp)); GdgMark(); Spacer(Modifier.height(24.dp)); Text("Good people.\nGreat ideas.", fontSize = 39.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Text("Your developer community,\none connection away.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Box(Modifier.fillMaxWidth().height(135.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF174EA6)))).padding(22.dp)) {
                Column { Text("LEARN  ·  BUILD  ·  CONNECT", color = Color(0xFFD2E3FC), fontSize = 11.sp, letterSpacing = 1.5.sp); Spacer(Modifier.height(16.dp)); Text("Find your next\nlightbulb moment.", color = Color.White, fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold) }
                Icon(Icons.Outlined.Code, null, Modifier.size(66.dp).align(Alignment.CenterEnd), tint = Color.White.copy(alpha = .55f))
            }
        }
        if (!vm.backendConfigured) item { Notice("Firebase connection pending. This development build needs its Firebase project configuration before sign-in is available.", Icons.Outlined.CloudOff) }
        item {
            Text(if (create) "Join GDG Connect" else "Welcome to GDG Connect", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email address") }, leadingIcon = { Icon(Icons.Outlined.Mail, null) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), enabled = !vm.busy)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) }, singleLine = true, visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), trailingIcon = { IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Show or hide password") } }, modifier = Modifier.fillMaxWidth(), enabled = !vm.busy)
            if (create) Text("Use at least 8 characters.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            if (!create) TextButton(onClick = { reset = true }, enabled = !vm.busy) { Text("Forgot password?") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.authenticate(email.trim(), password, create) }, enabled = vm.backendConfigured && !vm.busy && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth().height(54.dp)) {
                if (vm.busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Text(if (create) "Create account" else "Sign in", fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { create = !create }, enabled = !vm.busy, modifier = Modifier.fillMaxWidth()) { Text(if (create) "Already a member? Sign in" else "New here? Create an account") }
        }
        item { Text("An independent community event hub.\nBuilt for curious minds.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (reset) AlertDialog(onDismissRequest = { reset = false }, title = { Text("Reset your password") }, text = { Column { Text("Enter your account email and we’ll send a reset link."); Spacer(Modifier.height(12.dp)); OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)) } }, confirmButton = { TextButton(onClick = { reset = false; vm.resetPassword(email.trim()) }) { Text("Send reset link") } }, dismissButton = { TextButton(onClick = { reset = false }) { Text("Cancel") } })
}

@Composable
fun MainScreen(vm: ConnectViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val event = vm.events.find { it.id == selectedId }
    BackHandler(selectedId != null) { selectedId = null }
    if (selectedId != null && event != null) {
        EventDetails(event, vm, onBack = { selectedId = null }); return
    }
    Scaffold(bottomBar = {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            listOf("Discover" to Icons.Outlined.Explore, "My schedule" to Icons.Outlined.CalendarMonth, "Profile" to Icons.Outlined.PersonOutline).forEachIndexed { i, pair ->
                NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(pair.second, null) }, label = { Text(pair.first) })
            }
        }
    }) { pad ->
        Box(Modifier.padding(pad)) {
            when (tab) {
                0 -> Discover(vm) { selectedId = it }
                1 -> Schedule(vm) { selectedId = it }
                else -> Profile(vm) { tab = 1 }
            }
        }
    }
}

@Composable
fun Discover(vm: ConnectViewModel, select: (String) -> Unit) {
    var past by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { now = Instant.now(); delay(30_000) } }
    val filtered = remember(vm.events, query, past, now) { vm.events.filter { it.isPast(now) == past && it.matches(query) }.let { if (past) it.sortedByDescending { e -> e.startsAt } else it.sortedBy { e -> e.startsAt } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { GdgMark(Modifier.size(42.dp, 25.dp)); Spacer(Modifier.width(10.dp)); Text("GDG Connect", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.weight(1f)); IconButton(onClick = vm::refresh) { Icon(Icons.Outlined.Refresh, "Refresh events") } } }
        item { Text("Find your people.\nBuild what’s next.", fontSize = 32.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("Meetups, workshops & moments that matter.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { OutlinedTextField(query, { query = it.take(150) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search events, tech, clubs…") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, "Clear search") } }, shape = RoundedCornerShape(18.dp), singleLine = true) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { FilterChip(!past, { past = false }, label = { Text("Upcoming") }, leadingIcon = { Icon(Icons.Outlined.Event, null, Modifier.size(18.dp)) }); FilterChip(past, { past = true }, label = { Text("Past events") }, leadingIcon = { Icon(Icons.Outlined.History, null, Modifier.size(18.dp)) }) }; if (vm.offline) Notice("Offline · showing saved events. Reconnect to reserve.", Icons.Outlined.CloudOff) }
        if (vm.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        vm.feedError?.let { error -> item { Notice(error, Icons.Outlined.Info); TextButton(onClick = vm::refresh) { Text("Try again") } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (past) "Previously in the community" else "Explore events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("${filtered.size} events", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        if (filtered.isEmpty() && !vm.loading) item { EmptyState(if (query.isNotBlank()) "No matches yet" else if (past) "The story starts here" else "More connections coming soon", if (query.isNotBlank()) "Try a different event, venue, technology, or club." else "Events published by your community will appear here.", Icons.Outlined.Explore) }
        items(filtered, key = { it.id }) { e -> EventCard(e, vm.reservations.contains(e.id), { select(e.id) }) }
        item { Text("A community of builders. A world of possibilities.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp)) }
    }
}

@Composable
fun EventCard(event: Event, booked: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp), shape = RoundedCornerShape(22.dp)) {
        Box(Modifier.fillMaxWidth().height(116.dp).background(Brush.linearGradient(listOf(Color(0xFFE8F0FE), Color(0xFFD7E7FF)))).padding(18.dp)) {
            Column { Text(event.tags.firstOrNull()?.uppercase() ?: "COMMUNITY", color = Color(0xFF174EA6), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp); Spacer(Modifier.height(10.dp)); Text(event.organizingClub, color = Color(0xFF15345F), fontWeight = FontWeight.SemiBold, fontSize = 19.sp, maxLines = 2, modifier = Modifier.fillMaxWidth(.72f)) }
            Icon(when { event.tags.any { it.contains("Android", true) } -> Icons.Outlined.Android; event.tags.any { it.contains("AI", true) } -> Icons.Outlined.AutoAwesome; else -> Icons.Outlined.Code }, null, Modifier.size(55.dp).align(Alignment.CenterEnd), tint = Color(0xFF1A73E8).copy(alpha = .6f))
        }
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(event.dateLabel().uppercase(), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
            Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Place, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(5.dp)); Text(event.venue, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall) }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (event.sample) "Sample event" else event.price, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge); Text(if (booked) "✓ Registered" else "View event  →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
fun EventDetails(event: Event, vm: ConnectViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val booked = vm.reservations.contains(event.id)
    var cancel by remember { mutableStateOf(false) }
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { now = Instant.now(); delay(15_000) } }
    Scaffold(bottomBar = {
        Surface(shadowElevation = 6.dp) { Column(Modifier.fillMaxWidth().padding(20.dp)) {
            if (!event.isPast(now)) Text(if (booked) "Your reservation is confirmed" else "${event.seatsLeft} seats available · ${event.price}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 10.dp))
            Button(onClick = { if (booked) cancel = true else vm.reserve(event) }, enabled = !vm.busy && !vm.offline && !event.isPast(now) && (booked || event.seatsLeft > 0), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (vm.busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Text(when { event.isPast(now) -> "Registration closed"; booked -> "Cancel reservation"; event.seatsLeft == 0 -> "Event is full"; else -> "Register for event" })
            }
        } }
    }) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            item { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }; Text("Event details", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.weight(1f)); IconButton(onClick = { vm.launchIntent(context, Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${event.title}\n${event.dateLabel()} · ${event.timeLabel()}\n${event.venue}\nHosted by ${event.organizingClub}") }, "Share event")) }) { Icon(Icons.Outlined.Share, "Share event") } } }
            item { if (event.sample) Notice("Sample event for testing. Not an official scheduled meetup.", Icons.Outlined.Info); Text(event.tags.joinToString("  /  "), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge); Spacer(Modifier.height(12.dp)); Text(event.title, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Text("Hosted by ${event.organizingClub}", style = MaterialTheme.typography.bodyLarge) }
            item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) { DetailRow(Icons.Outlined.CalendarMonth, event.dateLabel(), event.timeLabel()); DetailRow(Icons.Outlined.Place, "Venue", event.venue); DetailRow(Icons.Outlined.ConfirmationNumber, "Registration", event.price) } } }
            item { Text("About this event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Text(event.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp) }
            item { Text("Your community hosts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); event.hosts.forEach { host -> Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Text(host.take(1), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(12.dp)); Text(host) } } }
            item {
                OutlinedButton(onClick = { vm.launchIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(event.venue)}"))) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Directions, null); Spacer(Modifier.width(8.dp)); Text("Get directions") }
                if (booked) OutlinedButton(onClick = { vm.launchIntent(context, Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.Events.TITLE, event.title).putExtra(CalendarContract.Events.EVENT_LOCATION, event.venue).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startsAt.toEpochMilli()).putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endsAt.toEpochMilli()).putExtra(CalendarContract.Events.DESCRIPTION, event.description)) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text("Add to calendar") }
                if (!event.price.equals("Free", true)) Text("This app reserves your place. Any ticket payment is arranged with the organizer; no payment is collected here.", style = MaterialTheme.typography.bodySmall)
                if (vm.offline) Notice("You're offline. Connect to confirm or cancel a reservation.", Icons.Outlined.CloudOff)
            }
        }
    }
    if (cancel) AlertDialog(onDismissRequest = { cancel = false }, title = { Text("Release your seat?") }, text = { Text("Your place at ${event.title} will become available to another community member.") }, confirmButton = { TextButton(onClick = { cancel = false; vm.cancel(event) }) { Text("Cancel reservation") } }, dismissButton = { TextButton(onClick = { cancel = false }) { Text("Keep my seat") } })
}

@Composable
fun Schedule(vm: ConnectViewModel, select: (String) -> Unit) {
    val registered = vm.events.filter { it.id in vm.reservations }.sortedBy { it.startsAt }
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text("My schedule", fontSize = 32.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("Make time for your next big idea.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (vm.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (registered.isEmpty() && !vm.loading) item { EmptyState("Your next chapter awaits", "Register for an event in Discover and find your confirmed reservations here.", Icons.Outlined.CalendarMonth) }
        items(registered, key = { it.id }) { EventCard(it, true) { select(it.id) } }
        vm.scheduleError?.let { item { Notice(it, Icons.Outlined.Info); TextButton(onClick = vm::refresh) { Text("Retry") } } }
    }
}

@Composable
fun Profile(vm: ConnectViewModel, schedule: () -> Unit) {
    var logout by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item { Text("Your space", fontSize = 32.sp, fontWeight = FontWeight.Bold) }
        item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.PersonOutline, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }; Text(vm.user?.email.orEmpty(), style = MaterialTheme.typography.titleMedium); Text(if (vm.user?.verified == true) "Email verified" else "Email not verified", color = if (vm.user?.verified == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant); if (vm.user?.verified == false) { TextButton(onClick = vm::sendVerification, enabled = !vm.busy) { Text("Send verification email") }; TextButton(onClick = vm::reloadUser, enabled = !vm.busy) { Text("I've verified my email") } } } } }
        item { Card(onClick = schedule, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.ConfirmationNumber, null); Spacer(Modifier.width(16.dp)); Column { Text("${vm.reservations.size} registered events", fontWeight = FontWeight.Bold); Text("View your schedule", style = MaterialTheme.typography.bodySmall) } } } }
        item { Text("Make it yours", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Text("Appearance", style = MaterialTheme.typography.titleSmall); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AppTheme.entries.forEach { theme -> FilterChip(selected = vm.theme == theme, onClick = { vm.setAppTheme(theme) }, label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) }) } } }
        item { OutlinedButton(onClick = { vm.user?.email?.let(vm::resetPassword) }, modifier = Modifier.fillMaxWidth(), enabled = !vm.busy) { Text("Send password reset email") }; TextButton(onClick = { logout = true }, modifier = Modifier.fillMaxWidth(), enabled = !vm.busy) { Icon(Icons.AutoMirrored.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text("Sign out") } }
        item { HorizontalDivider(); Spacer(Modifier.height(16.dp)); GdgMark(); Spacer(Modifier.height(10.dp)); Text("GDG Connect · v1.0", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp)); Text("Your email and reservations are stored in Firebase and accessible only to your account. Events are managed by community organizers. An independent community app, not an official Google product.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (logout) AlertDialog(onDismissRequest = { logout = false }, title = { Text("Sign out?") }, text = { Text("Your reservations are saved to your account.") }, confirmButton = { TextButton(onClick = { logout = false; vm.signOut() }) { Text("Sign out") } }, dismissButton = { TextButton(onClick = { logout = false }) { Text("Stay") } })
}

@Composable
fun DetailRow(icon: ImageVector, title: String, subtitle: String) { Row { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(14.dp)); Column { Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable
fun Notice(text: String, icon: ImageVector) { Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(icon, null, Modifier.size(20.dp)); Text(text, style = MaterialTheme.typography.bodySmall) } }
@Composable
fun EmptyState(title: String, text: String, icon: ImageVector) { Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) { Icon(icon, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
