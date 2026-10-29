package io.github.fyy0619cell.deskpet

import io.github.fyy0619cell.deskpet.pet.Stats
import io.github.fyy0619cell.deskpet.pet.StatsMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsTest {
    private val hour = 3_600_000L

    @Test
    fun decaysWithTimeAwayAndNeverGoesNegative() {
        val s = StatsMath.settle(Stats(updatedAt = 0L), 10 * hour)
        assertEquals(80f - 25f, s.hunger, 0.01f)
        assertEquals(85f - 15f, s.clean, 0.01f)
        val week = StatsMath.settle(Stats(updatedAt = 0L), 24 * 7 * hour)
        assertEquals(0f, week.hunger)
        assertEquals(0f, week.mood)
    }

    @Test
    fun neglectMakesMoodDropFaster() {
        val fine = StatsMath.settle(Stats(hunger = 90f, clean = 90f, mood = 80f, updatedAt = 0L), hour)
        val hungry = StatsMath.settle(Stats(hunger = 10f, clean = 90f, mood = 80f, updatedAt = 0L), hour)
        assertTrue(hungry.mood < fine.mood)
    }

    @Test
    fun feedingAFullPetEarnsNothing() {
        var s = Stats(hunger = 95f, updatedAt = 0L)
        repeat(20) { s = StatsMath.feed(s, 0L) }
        assertEquals(0, s.xp)
        assertEquals(100f, s.hunger)
    }

    @Test
    fun tapSpamEarnsAtMostOncePerMinute() {
        var s = Stats(updatedAt = 0L, lastPetXpAt = -StatsMath.PET_XP_COOLDOWN)
        for (i in 0 until 100) s = StatsMath.pet(s, i * 1000L)
        assertEquals(2 * 2, s.xp)
    }

    @Test
    fun levelsNeedMoreXpEachTime() {
        assertEquals(1, StatsMath.level(0))
        assertEquals(1, StatsMath.level(39))
        assertEquals(2, StatsMath.level(40))
        assertEquals(2, StatsMath.level(99))
        assertEquals(3, StatsMath.level(100))
        assertEquals(0.5f, StatsMath.levelProgress(70), 0.001f)
    }
}
