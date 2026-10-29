package io.github.fyy0619cell.deskpet.pet

import android.content.Context
import kotlin.math.max
import kotlin.math.min

data class Stats(
    val hunger: Float = 80f,
    val clean: Float = 85f,
    val mood: Float = 80f,
    val xp: Int = 0,
    val updatedAt: Long = 0L,
    val lastPetXpAt: Long = 0L,
)

/** Pure stat rules. Values are 0..100; time is wall-clock ms and is settled lazily. */
object StatsMath {
    private const val HOUR = 3_600_000f
    const val HUNGER_PER_HOUR = 2.5f
    const val CLEAN_PER_HOUR = 1.5f
    const val MOOD_PER_HOUR = 1f
    const val MOOD_PER_HOUR_NEGLECTED = 3f
    const val NEEDY = 30f
    const val PET_XP_COOLDOWN = 60_000L

    fun settle(s: Stats, now: Long): Stats {
        val hours = max(0L, now - s.updatedAt) / HOUR
        if (hours <= 0f) return s
        val hunger = clamp(s.hunger - hours * HUNGER_PER_HOUR)
        val clean = clamp(s.clean - hours * CLEAN_PER_HOUR)
        val neglected = hunger < NEEDY || clean < NEEDY
        val mood = clamp(s.mood - hours * (if (neglected) MOOD_PER_HOUR_NEGLECTED else MOOD_PER_HOUR))
        return s.copy(hunger = hunger, clean = clean, mood = mood, updatedAt = now)
    }

    /** Experience only when it was actually hungry, so feeding cannot be farmed. */
    fun feed(s: Stats, now: Long): Stats {
        val t = settle(s, now)
        val earned = if (t.hunger < 85f) 6 else 0
        return t.copy(hunger = clamp(t.hunger + 30f), mood = clamp(t.mood + 4f), xp = t.xp + earned)
    }

    fun clean(s: Stats, now: Long): Stats {
        val t = settle(s, now)
        val earned = if (t.clean < 85f) 6 else 0
        return t.copy(clean = clamp(t.clean + 40f), mood = clamp(t.mood + 3f), xp = t.xp + earned)
    }

    /** A tap: always cheers it up a little; experience at most once a minute. */
    fun pet(s: Stats, now: Long): Stats {
        val t = settle(s, now)
        val canEarn = now - t.lastPetXpAt >= PET_XP_COOLDOWN
        return t.copy(
            mood = clamp(t.mood + 5f),
            xp = t.xp + if (canEarn) 2 else 0,
            lastPetXpAt = if (canEarn) now else t.lastPetXpAt,
        )
    }

    /** Level L needs 40 + 20·(L-1) more xp to reach L+1. */
    fun level(xp: Int): Int {
        var level = 1
        var need = 40
        var left = xp
        while (left >= need) { left -= need; level++; need += 20 }
        return level
    }

    fun levelProgress(xp: Int): Float {
        var need = 40
        var left = xp
        while (left >= need) { left -= need; need += 20 }
        return left.toFloat() / need
    }

    private fun clamp(v: Float) = min(100f, max(0f, v))
}

class StatsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("stats", Context.MODE_PRIVATE)

    fun read(now: Long = System.currentTimeMillis()): Stats {
        val stored = if (prefs.contains("updated_at")) {
            Stats(
                prefs.getFloat("hunger", 80f), prefs.getFloat("clean", 85f), prefs.getFloat("mood", 80f),
                prefs.getInt("xp", 0), prefs.getLong("updated_at", now), prefs.getLong("pet_xp_at", 0L),
            )
        } else {
            Stats(updatedAt = now)
        }
        return StatsMath.settle(stored, now).also(::write)
    }

    fun update(now: Long = System.currentTimeMillis(), op: (Stats, Long) -> Stats): Stats =
        op(read(now), now).also(::write)

    private fun write(s: Stats) {
        prefs.edit()
            .putFloat("hunger", s.hunger).putFloat("clean", s.clean).putFloat("mood", s.mood)
            .putInt("xp", s.xp).putLong("updated_at", s.updatedAt).putLong("pet_xp_at", s.lastPetXpAt)
            .apply()
    }
}
