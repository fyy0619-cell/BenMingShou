package io.github.fyy0619cell.deskpet.pet

import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.Rng

/** WOKE = woken by a tap (worth a sound); GREETED = woken by unlocking the phone (silent). */
enum class BrainEvent { FELL_ASLEEP, WOKE, GREETED, TAPPED, WANDER, ARRIVED, FED }

/**
 * What the pet is doing, driven purely by timestamps (ms) so it can be unit-tested.
 * Once asleep it stays asleep until touched or greeted on unlock: no wake/sleep loops.
 */
class PetBrain(seed: Long, var wanderEnabled: Boolean = true) {
    private val rng = Rng(seed xor 0x5DEECE66DL)

    var action: Action = Action.IDLE
        private set
    var actionStart = 0L
        private set
    private var actionEnd = 0L
    var lastInteraction = 0L
        private set
    private var nextWanderAt = Long.MAX_VALUE
    private var blinkUntil = 0L
    private var nextBlinkAt = 0L

    var onEvent: ((BrainEvent) -> Unit)? = null

    val asleep: Boolean get() = action == Action.SLEEP

    fun start(now: Long) {
        lastInteraction = now
        set(Action.IDLE, now)
        scheduleWander(now)
        nextBlinkAt = now + 2500
    }

    fun update(now: Long) {
        when (action) {
            Action.HAPPY, Action.EAT -> if (now >= actionEnd) {
                set(Action.IDLE, now)
                scheduleWander(now)
            }
            Action.IDLE -> when {
                now - lastInteraction >= SLEEP_AFTER_MS -> {
                    set(Action.SLEEP, now)
                    emit(BrainEvent.FELL_ASLEEP)
                }
                wanderEnabled && now >= nextWanderAt -> {
                    set(Action.WALK, now)
                    emit(BrainEvent.WANDER)
                }
            }
            Action.WALK, Action.SLEEP -> Unit
        }
        if (action != Action.SLEEP && now >= nextBlinkAt) {
            blinkUntil = now + BLINK_MS
            nextBlinkAt = now + (2500 + rng.nextFloat() * 3500).toLong()
        }
    }

    fun blink(now: Long): Float = if (now < blinkUntil && action != Action.SLEEP) 1f else 0f

    /** 0..1 through the current timed action (HAPPY / EAT). */
    fun progress(now: Long): Float =
        if (actionEnd > actionStart) ((now - actionStart).toFloat() / (actionEnd - actionStart)).coerceIn(0f, 1f) else 0f

    fun arrived(now: Long) {
        if (action != Action.WALK) return
        set(Action.IDLE, now)
        scheduleWander(now)
        emit(BrainEvent.ARRIVED)
    }

    fun tap(now: Long) {
        val wasAsleep = asleep
        touch(now)
        react(Action.HAPPY, HAPPY_MS, now)
        emit(if (wasAsleep) BrainEvent.WOKE else BrainEvent.TAPPED)
    }

    /** Called when the user unlocks the phone. */
    fun greet(now: Long) {
        if (!asleep) return
        touch(now)
        react(Action.HAPPY, HAPPY_MS, now)
        emit(BrainEvent.GREETED)
    }

    fun feed(now: Long) {
        touch(now)
        react(Action.EAT, EAT_MS, now)
        emit(BrainEvent.FED)
    }

    /** A happy hop with no event, e.g. after being cleaned from the app. */
    fun cheer(now: Long) {
        touch(now)
        react(Action.HAPPY, HAPPY_MS, now)
    }

    /** Picked up by a drag: stop whatever it was doing. */
    fun grab(now: Long) {
        touch(now)
        set(Action.IDLE, now)
    }

    fun release(now: Long) = touch(now)

    private fun touch(now: Long) {
        lastInteraction = now
        scheduleWander(now)
    }

    private fun react(a: Action, duration: Long, now: Long) {
        set(a, now)
        actionEnd = now + duration
    }

    private fun set(a: Action, now: Long) {
        action = a
        actionStart = now
        actionEnd = now
    }

    private fun scheduleWander(now: Long) {
        nextWanderAt = now + (WANDER_MIN_MS + rng.nextFloat() * (WANDER_MAX_MS - WANDER_MIN_MS)).toLong()
    }

    private fun emit(e: BrainEvent) = onEvent?.invoke(e)

    companion object {
        const val SLEEP_AFTER_MS = 120_000L
        const val WANDER_MIN_MS = 25_000L
        const val WANDER_MAX_MS = 60_000L
        const val HAPPY_MS = 1400L
        const val EAT_MS = 1800L
        const val BLINK_MS = 140L
    }
}
