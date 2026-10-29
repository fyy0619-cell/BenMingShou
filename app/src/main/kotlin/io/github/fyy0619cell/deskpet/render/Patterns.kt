package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas

object Patterns {
    fun stripeOffsets(seed: Long, n: Int): FloatArray {
        val r = Rng(seed * 31 + 7)
        return FloatArray(n) { r.range(-1f, 1f) }
    }

    fun spotOffsets(seed: Long, n: Int): FloatArray {
        val r = Rng(seed * 17 + 3)
        return FloatArray(n * 2) { r.range(-0.9f, 0.9f) }
    }

    /** A tapered, slightly curved wedge hanging down from (x, top). */
    fun stripe(kit: Kit, c: Canvas, x: Float, top: Float, len: Float, halfW: Float, color: Int) {
        val p = kit.path2
        p.rewind()
        p.moveTo(x - halfW, top)
        p.quadTo(x - halfW * 0.2f + 2f, top + len * 0.55f, x + 1.5f, top + len)
        p.quadTo(x + halfW * 0.9f + 2f, top + len * 0.5f, x + halfW, top)
        p.close()
        c.drawPath(p, kit.solid(color))
    }
}
