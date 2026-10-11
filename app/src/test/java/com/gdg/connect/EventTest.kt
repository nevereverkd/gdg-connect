package com.gdg.connect
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
class EventTest {
    private val start = Instant.parse("2026-11-20T04:30:00Z")
    private val event = Event("1", "Android AI Lab", "Learn", start, start.plusSeconds(7200), "Tech Park Mumbai", "Free", listOf("A host"), "GDG Mumbai", listOf("Kotlin", "AI"), 20, 3)
    @Test fun searchFindsEverySpecifiedFieldIgnoringCase() {
        listOf("android", "KOTLIN", "tech PARK", "gdg mumbai", "android mumbai", "  AI  ", "").forEach { assertTrue(it, event.matches(it)) }
        assertFalse(event.matches("flutter")); assertFalse(event.matches("android delhi"))
    }
    @Test fun exactStartClosesRegistration() { assertFalse(event.isPast(start.minusNanos(1))); assertTrue(event.isPast(start)); assertTrue(event.isPast(start.plusSeconds(1))) }
    @Test fun dateUsesEventZoneNotPhoneZone() { assertEquals("Fri, 20 Nov 2026", event.dateLabel()); assertTrue(event.timeLabel().startsWith("10:00 AM")) }
    @Test fun seatsCannotBecomeNegative() { assertEquals(17, event.seatsLeft); assertEquals(0, event.copy(reserved = 21).seatsLeft) }
}
