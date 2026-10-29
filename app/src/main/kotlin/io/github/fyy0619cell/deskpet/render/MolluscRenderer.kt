package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import io.github.fyy0619cell.deskpet.render.Feature.BELL
import io.github.fyy0619cell.deskpet.render.Feature.EYE_STALKS
import io.github.fyy0619cell.deskpet.render.Feature.FOOT
import io.github.fyy0619cell.deskpet.render.Feature.SPIRAL_SHELL
import kotlin.math.cos
import kotlin.math.sin

/** Octopus, jellyfish, ammonite and snail. */
class MolluscRenderer(look: Look) : PetRenderer(look) {
    private val jelly = look.has(BELL)
    private val snail = look.has(FOOT)
    private val shelled = look.has(SPIRAL_SHELL)
    private val s = look.bodyLength
    private val bellFill = ColorX.withAlpha(look.palette.base, 205)
    private val bellInner = ColorX.withAlpha(look.palette.belly, 150)
    private val ribbon = ColorX.withAlpha(look.palette.accent, 200)
    private val thread = ColorX.withAlpha(look.palette.accent, 150)
    private val sucker = ColorX.lighter(look.palette.belly, 0.04f)

    override val shadowHalfWidth = 46f * s
    override val shadowCenterX = 100f

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        val moving = pose.action == Action.WALK
        when {
            snail -> snail(c, moving)
            jelly -> jellyfish(c, moving)
            shelled -> ammonite(c, moving)
            else -> octopus(c, moving)
        }
    }

    private fun floatY(base: Float) = base + sin(t * TAU / 2.8f) * 5f + tuck * 30f - jump * 0.5f

    private fun octopus(c: Canvas, moving: Boolean) {
        val cx = 100f
        val cy = floatY(78f)
        val rx = 34f * s
        val ry = 32f * look.bodyHeight
        for (i in 0 until 6) tentacle(c, cx - rx * 0.7f + i * rx * 0.28f, cy + ry * 0.6f, i, moving, far = i % 2 == 1)
        c.save()
        c.rotate(if (moving) -12f else 0f, cx, cy)
        kit.ovalShaded(c, cx, cy - ry * 0.1f, rx, ry * 1.05f, base)
        if (look.pattern == Pattern.SPOTS) {
            val dot = kit.solid(accent.base)
            c.drawCircle(cx - rx * 0.4f, cy - ry * 0.55f, 4.5f, dot)
            c.drawCircle(cx + rx * 0.05f, cy - ry * 0.78f, 3.5f, dot)
            c.drawCircle(cx + rx * 0.45f, cy - ry * 0.45f, 4f, dot)
        }
        kit.sheen(c, cx, cy - ry * 0.2f, rx, ry)
        c.restore()
        face(c, cx + 4f, cy + ry * 0.35f, 6f)
    }

    private fun tentacle(c: Canvas, x: Float, y: Float, i: Int, moving: Boolean, far: Boolean) {
        val st = kit.stroke
        st.strokeWidth = 10f * s
        st.color = if (far) base.dark else base.base
        st.shader = null
        val wave = sin(t * 3f + i * 1.1f) * 6f
        val sweep = if (moving) -26f else 0f
        val reach = (if (tuck > 0.5f) 26f else 46f) * look.legLength
        val p = kit.path
        p.rewind()
        p.moveTo(x, y)
        p.cubicTo(x + (i - 2.5f) * 4f + wave * 0.5f, y + reach * 0.4f, x + sweep + wave, y + reach * 0.7f, x + sweep * 1.4f + (i - 2.5f) * 10f + wave, y + reach)
        c.drawPath(p, st)
        if (!far) {
            val dot = kit.solid(sucker)
            c.drawCircle(x + sweep * 0.4f + wave * 0.4f + 3f, y + reach * 0.45f, 2.2f, dot)
            c.drawCircle(x + sweep * 0.8f + wave * 0.7f + 3f, y + reach * 0.72f, 1.8f, dot)
        }
    }

    private fun jellyfish(c: Canvas, moving: Boolean) {
        val cx = 100f
        val pulse = sin(t * TAU / (if (moving) 0.9f else 1.8f))
        val cy = floatY(70f) - pulse * 3f
        val rx = 40f * s * (1f + pulse * 0.05f)
        val ry = 32f * look.bodyHeight * (1f - pulse * 0.08f)
        val st = kit.stroke
        st.shader = null
        st.strokeWidth = 1.6f; st.color = thread
        val p = kit.path
        p.rewind()
        for (i in 0 until 7) {
            val x = cx - rx * 0.85f + i * rx * 0.28f
            p.moveTo(x, cy + 2f)
            p.cubicTo(x + sin(t * 2f + i) * 6f, cy + 30f, x - sin(t * 2.4f + i) * 8f, cy + 60f, x + sin(t * 1.7f + i) * 5f, cy + 86f - tuck * 30f)
        }
        c.drawPath(p, st)
        st.strokeWidth = 7f; st.color = ribbon
        p.rewind()
        for (i in 0 until 3) {
            val x = cx - 12f + i * 12f
            p.moveTo(x, cy)
            p.cubicTo(x + 10f * sin(t * 2f + i), cy + 22f, x - 10f * sin(t * 2f + i), cy + 38f, x + 4f, cy + 52f - tuck * 16f)
        }
        c.drawPath(p, st)
        p.rewind()
        p.moveTo(cx - rx, cy + 4f)
        p.cubicTo(cx - rx, cy - ry * 1.3f, cx + rx, cy - ry * 1.3f, cx + rx, cy + 4f)
        var x = cx + rx
        var k = 0
        while (x > cx - rx) {
            val nx = x - rx * 0.25f
            p.quadTo((x + nx) / 2f, cy + (if (k % 2 == 0) 10f else 4f), nx, cy + 4f)
            x = nx; k++
        }
        p.close()
        c.drawPath(p, kit.solid(bellFill))
        kit.ovalC(c, cx, cy - ry * 0.35f, rx * 0.55f, ry * 0.45f, kit.solid(bellInner))
        kit.sheen(c, cx, cy - ry * 0.4f, rx, ry)
        face(c, cx + 2f, cy - ry * 0.15f, 5.5f)
    }

    private fun ammonite(c: Canvas, moving: Boolean) {
        val cy = floatY(92f)
        val shellX = 86f
        val r = 36f * s
        for (i in 0 until 5) {
            val st = kit.stroke
            st.strokeWidth = 5f; st.color = if (i % 2 == 0) base.base else base.dark; st.shader = null
            val y = cy + 4f + i * 4f - 8f
            val wave = sin(t * 3f + i) * 4f
            c.drawLine(shellX + r * 0.85f, y, shellX + r * 0.85f + 24f + (if (moving) -6f else 0f), y + 8f + wave, st)
        }
        kit.ovalShaded(c, shellX + r * 0.8f, cy + 4f, 14f, 13f, base)
        spiralShell(c, shellX, cy, r, ribs = true)
        face(c, shellX + r * 0.85f, cy, 4.8f)
    }

    private fun snail(c: Canvas, moving: Boolean) {
        val ripple = if (moving) sin(t * TAU * 1.4f) * 2f else 0f
        val footTop = GROUND - 16f
        val cx = 100f
        val p = kit.path
        p.rewind()
        p.moveTo(cx - 52f * s, GROUND - 2f)
        p.quadTo(cx - 40f * s, footTop + ripple, cx + 10f, footTop)
        val neckY = footTop - 22f + tuck * 18f
        p.quadTo(cx + 30f * s, footTop - 4f, cx + 34f * s, neckY)
        p.quadTo(cx + 50f * s, neckY - 6f, cx + 54f * s, footTop + 2f)
        p.quadTo(cx + 56f * s, GROUND - 2f, cx + 40f * s, GROUND - 2f)
        p.close()
        c.drawPath(p, kit.shaded(belly, footTop - 20f, GROUND))
        if (look.has(EYE_STALKS)) {
            for (i in 0..1) {
                val bx = cx + (40f + i * 8f) * s
                val ey = neckY - 20f + tuck * 16f + sin(t * 2f + i) * 1.5f
                kit.line(c, bx, neckY + 2f, bx + 4f, ey, 3.2f, belly.dark)
                c.drawCircle(bx + 4f, ey, 5f, kit.solid(belly.base))
                kit.eye(c, bx + 5f, ey, 3f, blink, eyeMode, look.palette.eye)
            }
        }
        kit.blush(c, cx + 46f * s, neckY + 12f, 4f)
        spiralShell(c, cx - 8f, footTop - 26f * look.bodyHeight, 30f * look.bodyHeight, ribs = false)
    }

    private fun spiralShell(c: Canvas, cx: Float, cy: Float, r: Float, ribs: Boolean) {
        c.drawCircle(cx, cy, r, kit.shaded(accent, cy - r, cy + r))
        val st = kit.stroke
        st.strokeWidth = 2.2f; st.color = accent.deep; st.shader = null
        val p = kit.path
        p.rewind()
        val turns = 2.6f
        val steps = 48
        for (k in 0..steps) {
            val f = k / steps.toFloat()
            val a = f * turns * TAU
            val rr = r * (0.92f - 0.8f * f)
            val x = cx + cos(a) * rr
            val y = cy + sin(a) * rr
            if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        c.drawPath(p, st)
        if (ribs) {
            st.strokeWidth = 1.2f
            for (i in 0 until 16) {
                val a = i / 16f * TAU
                c.drawLine(cx + cos(a) * r * 0.55f, cy + sin(a) * r * 0.55f, cx + cos(a) * r * 0.96f, cy + sin(a) * r * 0.96f, st)
            }
        }
        kit.sheen(c, cx, cy, r, r)
    }

    private fun face(c: Canvas, x: Float, y: Float, r: Float) {
        if (eyeMode == 0 && blink < 0.85f) {
            c.drawCircle(x - r * 1.6f, y, r * 1.25f, kit.solid(Color.WHITE))
            c.drawCircle(x + r * 1.6f, y, r * 1.25f, kit.solid(Color.WHITE))
        }
        kit.eye(c, x - r * 1.5f, y, r, blink, eyeMode, look.palette.eye)
        kit.eye(c, x + r * 1.7f, y, r, blink, eyeMode, look.palette.eye)
        val p = kit.path
        p.rewind()
        p.moveTo(x - r * 0.6f, y + r * 1.6f)
        p.quadTo(x, y + r * 2.4f, x + r * 0.6f, y + r * 1.6f)
        kit.stroke.strokeWidth = 2f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
        kit.blush(c, x - r * 2.6f, y + r * 1.6f, r * 0.8f)
        kit.blush(c, x + r * 2.8f, y + r * 1.6f, r * 0.8f)
    }
}
