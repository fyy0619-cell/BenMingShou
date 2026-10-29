package io.github.fyy0619cell.deskpet

import io.github.fyy0619cell.deskpet.pet.BrainEvent
import io.github.fyy0619cell.deskpet.pet.PetBrain
import io.github.fyy0619cell.deskpet.render.Action
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetBrainTest {
    private val events = mutableListOf<Pair<Long, BrainEvent>>()

    private fun brain(wander: Boolean = true) = PetBrain(seed = 9L, wanderEnabled = wander).also { b ->
        b.onEvent = { events += now to it }
        b.start(0L)
    }

    private var now = 0L

    /** Simulates the overlay at ~12 fps; walks finish after 4 s like a real trip. */
    private fun run(b: PetBrain, untilMs: Long) {
        var walkEnd = -1L
        while (now < untilMs) {
            now += 83
            b.update(now)
            if (b.action == Action.WALK) {
                if (walkEnd < 0) walkEnd = now + 4000
                if (now >= walkEnd) { b.arrived(now); walkEnd = -1 }
            }
        }
    }

    @Test
    fun anHourAloneMeansOneSleepAndNoNoise() {
        val b = brain()
        run(b, 60 * 60_000L)
        val sleeps = events.count { it.second == BrainEvent.FELL_ASLEEP }
        val wakes = events.count { it.second == BrainEvent.WOKE }
        val lateWanders = events.count { it.second == BrainEvent.WANDER && it.first > PetBrain.SLEEP_AFTER_MS + 10_000 }
        assertEquals(1, sleeps)
        assertEquals(0, wakes)
        assertEquals(0, lateWanders)
        assertTrue(b.asleep)
    }

    @Test
    fun wandersAFewTimesBeforeFallingAsleep() {
        val b = brain()
        run(b, PetBrain.SLEEP_AFTER_MS)
        val wanders = events.count { it.second == BrainEvent.WANDER }
        assertTrue("wanders=$wanders", wanders in 1..5)
    }

    @Test
    fun tapWakesItAndResetsTheSleepClock() {
        val b = brain()
        run(b, 10 * 60_000L)
        assertTrue(b.asleep)
        b.tap(now)
        assertEquals(Action.HAPPY, b.action)
        assertEquals(BrainEvent.WOKE, events.last().second)
        run(b, now + 60_000L)
        assertFalse(b.asleep)
    }

    @Test
    fun unlockGreetingWakesOnlyASleepingPet() {
        val b = brain()
        b.greet(1000L)
        assertTrue(events.none { it.second == BrainEvent.GREETED })
        run(b, 5 * 60_000L)
        b.greet(now)
        assertEquals(BrainEvent.GREETED, events.last().second)
        assertFalse(b.asleep)
    }

    @Test
    fun wanderingCanBeTurnedOff() {
        val b = brain(wander = false)
        run(b, PetBrain.SLEEP_AFTER_MS)
        assertTrue(events.none { it.second == BrainEvent.WANDER })
    }

    @Test
    fun blinksAreBriefAndNeverWhileAsleep() {
        val b = brain()
        var blinkFrames = 0
        var frames = 0
        while (now < 60_000L) {
            now += 20
            b.update(now)
            frames++
            if (b.blink(now) > 0f) blinkFrames++
        }
        assertTrue(blinkFrames in 1 until frames / 10)
        run(b, 10 * 60_000L)
        assertEquals(0f, b.blink(now))
    }
}
