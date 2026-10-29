package io.github.fyy0619cell.deskpet

import io.github.fyy0619cell.deskpet.overlay.Bounds
import io.github.fyy0619cell.deskpet.overlay.Wander
import io.github.fyy0619cell.deskpet.render.Locomotion
import io.github.fyy0619cell.deskpet.render.Rng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TripTest {
    private val b = Bounds(width = 1080, height = 2400, top = 84, winW = 330, winH = 220, density = 3f)
    private val out = FloatArray(2)

    @Test
    fun groundTravellersKeepTheirHeight() {
        for (loco in listOf(Locomotion.WALK, Locomotion.HOP, Locomotion.SLITHER)) {
            val trip = Wander.plan(loco, 300f, 1500f, b, Rng(1), 0L)
            for (t in 0..trip.duration step 100) {
                trip.position(t, out)
                assertEquals(1500f, out[1], 0.01f)
            }
        }
    }

    @Test
    fun fliersArcAndLandWhereTheyTookOff() {
        val trip = Wander.plan(Locomotion.FLY, 300f, 1500f, b, Rng(2), 0L)
        trip.position(trip.duration / 2, out)
        assertTrue("should rise mid-flight", out[1] < 1500f - 100f)
        assertTrue(trip.position(trip.duration, out))
        assertEquals(1500f, out[1], 0.01f)
    }

    @Test
    fun everyTripStaysOnScreenAndIsWorthWatching() {
        val rng = Rng(3)
        for (i in 0 until 500) {
            val loco = Locomotion.entries[i % Locomotion.entries.size]
            val x = rng.range(0f, b.maxX)
            val y = rng.range(b.top.toFloat(), b.maxY)
            val trip = Wander.plan(loco, x, y, b, rng, 0L)
            assertTrue(trip.duration in 1500L..12_000L)
            trip.position(trip.duration, out)
            assertTrue(out[0] in 0f..b.maxX && out[1] in b.top.toFloat()..b.maxY)
            for (t in 0..trip.duration step 250) {
                trip.position(t, out)
                assertTrue("y=${out[1]} above top", out[1] >= b.top - 0.5f)
            }
            assertTrue(abs(trip.x1 - trip.x0) >= 90f * b.density - 1f || trip.x1 == 0f || trip.x1 == b.maxX)
        }
    }
}
