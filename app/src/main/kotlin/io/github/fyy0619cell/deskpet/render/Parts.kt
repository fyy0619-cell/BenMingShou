package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

object Ears {
    /** (x, y) is the ear base centre on top of the head; s is head radius. */
    fun draw(kit: Kit, c: Canvas, ear: Ear, x: Float, y: Float, s: Float, tilt: Float, outer: Tone, inner: Int) {
        if (ear == Ear.NONE) return
        c.save()
        c.rotate(tilt, x, y)
        val p = kit.path
        when (ear) {
            Ear.POINTY -> {
                tri(p, x, y, s * 0.42f, s * 0.62f)
                c.drawPath(p, kit.shaded(outer, y - s * 0.62f, y))
                tri(p, x, y - s * 0.04f, s * 0.22f, s * 0.38f)
                c.drawPath(p, kit.solid(inner))
            }
            Ear.ROUND -> {
                c.drawCircle(x, y - s * 0.12f, s * 0.27f, kit.shaded(outer, y - s * 0.4f, y + s * 0.15f))
                c.drawCircle(x, y - s * 0.1f, s * 0.14f, kit.solid(inner))
            }
            Ear.LONG -> {
                kit.oval(c, x - s * 0.17f, y - s * 1.0f, x + s * 0.17f, y + s * 0.1f, kit.shaded(outer, y - s, y))
                kit.oval(c, x - s * 0.08f, y - s * 0.86f, x + s * 0.08f, y - s * 0.05f, kit.solid(inner))
            }
            Ear.FLOPPY -> {
                c.rotate(-55f, x, y)
                kit.oval(c, x - s * 0.18f, y - s * 0.1f, x + s * 0.18f, y + s * 0.62f, kit.shaded(outer, y, y + s * 0.62f))
            }
            Ear.BIG_FAN -> {
                kit.oval(c, x - s * 0.62f, y - s * 0.2f, x + s * 0.22f, y + s * 0.95f, kit.shaded(outer, y - s * 0.2f, y + s))
                kit.oval(c, x - s * 0.5f, y - s * 0.05f, x + s * 0.1f, y + s * 0.8f, kit.solid(inner))
            }
            Ear.NONE -> Unit
        }
        c.restore()
    }

    private fun tri(p: Path, x: Float, y: Float, halfW: Float, h: Float) {
        p.rewind()
        p.moveTo(x - halfW, y + h * 0.1f)
        p.quadTo(x - halfW * 0.35f, y - h * 0.65f, x - halfW * 0.08f, y - h)
        p.quadTo(x + halfW * 0.1f, y - h * 1.04f, x + halfW * 0.22f, y - h * 0.92f)
        p.quadTo(x + halfW * 0.8f, y - h * 0.4f, x + halfW, y + h * 0.1f)
        p.close()
    }
}

object Tails {
    private val bandDash = android.graphics.DashPathEffect(floatArrayOf(5f, 7f), 3f)

    /**
     * Tail grows leftwards from (x, y); sway is -1..1. curl 0..1 lays it along the ground
     * towards [groundY] (sleeping). bands draws stripes in [tip] colour.
     */
    fun draw(
        kit: Kit, c: Canvas, tail: Tail, x: Float, y: Float, size: Float, sway: Float,
        tone: Tone, tip: Tone, curl: Float = 0f, groundY: Float = y, bands: Boolean = false,
    ) {
        val st = kit.stroke
        st.shader = null
        when (tail) {
            Tail.NONE -> Unit
            Tail.SHORT -> kit.ovalC(c, x - size * 0.08f, y - size * 0.02f, size * 0.12f, size * 0.1f, kit.solid(tone.light))
            Tail.THIN, Tail.TUFT -> {
                val u = 1f - curl
                val ex = (x - size * 0.45f + sway * size * 0.18f) * u + (x + size * 0.15f) * curl
                val ey = (y - size * 0.62f) * u + (groundY - size * 0.05f) * curl
                val p = kit.path
                p.rewind()
                p.moveTo(x, y)
                p.cubicTo(
                    x - size * 0.35f, y + size * 0.05f + curl * size * 0.2f,
                    (x - size * 0.55f + sway * size * 0.08f) * u + (x - size * 0.4f) * curl,
                    (y - size * 0.25f) * u + groundY * curl,
                    ex, ey,
                )
                st.strokeWidth = size * 0.13f
                st.color = tone.dark
                c.drawPath(p, st)
                if (bands) {
                    st.color = tip.base
                    st.strokeCap = Paint.Cap.BUTT
                    st.pathEffect = bandDash
                    c.drawPath(p, st)
                    st.pathEffect = null
                    st.strokeCap = Paint.Cap.ROUND
                }
                if (tail == Tail.TUFT || bands) {
                    c.drawCircle(ex, ey, size * (if (bands) 0.065f else 0.1f), kit.solid(tip.dark))
                }
            }
            Tail.BUSHY -> {
                c.save()
                c.rotate(-35f + sway * 10f - curl * 25f, x, y)
                val clip = kit.path2
                clip.rewind()
                kit.rect.set(x - size * 0.75f, y - size * 0.2f, x + size * 0.05f, y + size * 0.2f)
                clip.addOval(kit.rect, Path.Direction.CW)
                c.drawPath(clip, kit.shaded(tone, y - size * 0.2f, y + size * 0.2f))
                c.save()
                c.clipPath(clip)
                if (bands) {
                    for (i in 0 until 3) {
                        val bx = x - size * (0.18f + i * 0.2f)
                        kit.rect.set(bx - size * 0.05f, y - size * 0.3f, bx + size * 0.04f, y + size * 0.3f)
                        c.drawRect(kit.rect, kit.solid(tip.base))
                    }
                } else {
                    kit.oval(c, x - size * 0.78f, y - size * 0.14f, x - size * 0.48f, y + size * 0.14f, kit.solid(tip.base))
                }
                c.restore()
                c.restore()
            }
            Tail.THICK -> {
                val p = kit.path
                p.rewind()
                p.moveTo(x + size * 0.05f, y - size * 0.22f)
                p.quadTo(x - size * 0.5f, y - size * 0.2f + sway * size * 0.06f, x - size * 0.95f, y + size * 0.12f + sway * size * 0.1f)
                p.quadTo(x - size * 0.5f, y + size * 0.2f, x + size * 0.05f, y + size * 0.24f)
                p.close()
                c.drawPath(p, kit.shaded(tone, y - size * 0.22f, y + size * 0.24f))
            }
        }
        st.strokeWidth = 1f
        kit.fill.style = Paint.Style.FILL
    }
}
