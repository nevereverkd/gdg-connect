package com.gdg.connect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class LiveFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun awaitText(text: String) { ui.waitUntil(30_000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() } }
    @Test fun liveSignInReadReserveCancelAndSignOut() {
        check(!BuildConfig.USE_FIREBASE_EMULATORS)
        val args = InstrumentationRegistry.getArguments()
        val email = requireNotNull(args.getString("testEmail"))
        val password = requireNotNull(args.getString("testPassword"))
        awaitText("Email address")
        ui.onNodeWithText("Email address").performTextReplacement(email)
        ui.onNodeWithText("Password").performTextReplacement(password)
        ui.onNodeWithText("Sign in").performScrollTo().performClick()
        awaitText("Build beautiful Android apps")
        ui.onNodeWithText("Build beautiful Android apps").performScrollTo().performClick()
        ui.waitUntil(30_000) { ui.onAllNodesWithText("You're offline. Connect to confirm or cancel a reservation.").fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithText("Register for event").assertIsEnabled().performClick()
        awaitText("You're on the list!")
        ui.onNodeWithText("Great, let's go").performClick()
        awaitText("Your reservation is confirmed")
        ui.onNodeWithText("Cancel reservation").performClick()
        awaitText("Release your seat?")
        ui.onAllNodesWithText("Cancel reservation").onLast().performClick()
        awaitText("Register for event")
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNodeWithText("Profile").performClick()
        awaitText(email)
        ui.onNodeWithText("Sign out").performScrollTo().performClick()
        awaitText("Sign out?")
        ui.onAllNodesWithText("Sign out").onLast().performClick()
        awaitText("Welcome to GDG Connect")
    }
}
