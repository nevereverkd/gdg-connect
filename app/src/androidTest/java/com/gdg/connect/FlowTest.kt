package com.gdg.connect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun awaitText(text: String) { try { ui.waitUntil(20_000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() } } catch (e: Exception) { ui.onRoot().printToLog("GDGTest"); throw e } }
    private fun click(text: String) { ui.onNodeWithText(text).performScrollTo().performClick() }
    @Test fun signupSearchReservePersistCancelAndTheme() {
        check(BuildConfig.USE_FIREBASE_EMULATORS) { "Run only with -PfirebaseEmulators=true" }
        val email = "device-${System.currentTimeMillis()}@example.com"
        awaitText("New here? Create an account")
        click("New here? Create an account")
        ui.onNodeWithText("Email address").performTextReplacement(email)
        ui.onNodeWithText("Password").performTextReplacement("Device-test-8923")
        click("Create account")
        awaitText("Explore events")
        awaitText("Build beautiful Android apps")
        ui.onNodeWithText("Search events, tech, clubs…").performTextInput("Kotlin")
        ui.onNodeWithText("Build beautiful Android apps").assertExists()
        ui.onNodeWithText("From idea to AI prototype").assertDoesNotExist()
        ui.onNodeWithContentDescription("Clear search").performClick()
        ui.onNodeWithText("Past events").performClick()
        awaitText("Web foundations, real connections")
        ui.onNodeWithText("Upcoming").performClick()
        click("Build beautiful Android apps")
        awaitText("Register for event")
        ui.onNodeWithText("Register for event").performClick()
        awaitText("You're on the list!")
        ui.onNodeWithText("Great, let's go").performClick()
        awaitText("Your reservation is confirmed")
        ui.activityRule.scenario.recreate()
        awaitText("Your reservation is confirmed")
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNodeWithText("My schedule").performClick()
        awaitText("Build beautiful Android apps")
        click("Build beautiful Android apps")
        ui.onNodeWithText("Cancel reservation").performClick()
        awaitText("Release your seat?")
        ui.onAllNodesWithText("Cancel reservation").onLast().performClick()
        awaitText("Register for event")
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNodeWithText("Profile").performClick()
        awaitText(email)
        click("Dark")
        click("Sign out")
        awaitText("Sign out?")
        ui.onAllNodesWithText("Sign out").onLast().performClick()
        awaitText("Welcome to GDG Connect")
        ui.onNodeWithText("Email address").performTextReplacement(email)
        ui.onNodeWithText("Password").performTextReplacement("Device-test-8923")
        ui.onNodeWithText("Sign in").assertIsEnabled()
        click("Sign in")
        awaitText("Explore events")
        Tasks.await(FirebaseFirestore.getInstance().disableNetwork())
        awaitText("Offline · showing saved events. Reconnect to reserve.")
        click("Build beautiful Android apps")
        ui.onNodeWithText("Register for event").assertIsNotEnabled()
        Tasks.await(FirebaseFirestore.getInstance().enableNetwork())
        ui.waitUntil(20_000) { ui.onAllNodesWithText("You're offline. Connect to confirm or cancel a reservation.").fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithContentDescription("Back").performClick()
    }
}
