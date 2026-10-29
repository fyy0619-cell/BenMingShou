package io.github.fyy0619cell.deskpet.overlay

import io.github.fyy0619cell.deskpet.render.Locomotion
import io.github.fyy0619cell.deskpet.render.Rng
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin

/** One wander from (x0, y0) to (x1, y1); positions are window top-left in screen px. */
class Trip(
    val x0: Float, val y0: Float, val x1: Float, val y1: Float,
    val start: Long, val duration: Long, private val loco: Locomotion, private val arc: Float,
) {
    val facingRight: Boolean get() = x1 >= x0

    /** Writes the position at [now] into [out]; returns true once arrived. */
    fun position(now: Long, out: FloatArray): Boolean {
        val t = ((now - start).toFloat() / duration).coerceIn(0f, 1f)
        val e = if (loco == Locomotion.SWIM || loco == Locomotion.FLY) t * t * (3f - 2f * t) else t
        out[0] = x0 + (x1 - x0) * e
        out[1] = y0 + (y1 - y0) * e - (if (loco == Locomotion.FLY) sin(PI.toFloat() * t) * arc else 0f)
        return t >= 1f
    }
}

/** Screen [width]×[height] and the pet window [winW]×[winH], all in px. */
class Bounds(val width: Int, val height: Int, val top: Int, val winW: Int, val winH: Int, val density: Float) {
    val maxX: Float get() = (width - winW).coerceAtLeast(0).toFloat()
    val maxY: Float get() = (height - winH).coerceAtLeast(top).toFloat()

    fun clampX(x: Float) = x.coerceIn(0f, maxX)
    fun clampY(y: Float) = y.coerceIn(top.toFloat(), maxY)
}

object Wander {
    private fun speedDp(loco: Locomotion) = when (loco) {
        Locomotion.WALK -> 55f
        Locomotion.HOP -> 48f
        Locomotion.SLITHER -> 26f
        Locomotion.FLY -> 120f
        Locomotion.SWIM -> 40f
    }

    fun plan(loco: Locomotion, x: Float, y: Float, b: Bounds, rng: Rng, now: Long): Trip {
        val minHop = 90f * b.density
        var tx = rng.range(0f, b.maxX)
        if (abs(tx - x) < minHop) tx = if (x > b.maxX / 2f) rng.range(0f, (x - minHop).coerceAtLeast(0f)) else rng.range((x + minHop).coerceAtMost(b.maxX), b.maxX)
        val ty = if (loco == Locomotion.SWIM) rng.range(b.top.toFloat(), b.top + (b.maxY - b.top) * 0.8f) else y
        val arc = if (loco == Locomotion.FLY) minOf(rng.range(80f, 150f) * b.density, (y - b.top).coerceAtLeast(0f)) else 0f
        val dist = hypot(tx - x, ty - y) + arc
        val ms = (dist / (speedDp(loco) * b.density) * 1000f).toLong().coerceIn(1500L, 12_000L)
        return Trip(x, y, tx, ty, now, ms, loco, arc)
    }
}
