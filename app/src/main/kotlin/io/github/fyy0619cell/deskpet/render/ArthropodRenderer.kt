package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import io.github.fyy0619cell.deskpet.render.Feature.ANTENNAE
import io.github.fyy0619cell.deskpet.render.Feature.CURLED_STINGER
import io.github.fyy0619cell.deskpet.render.Feature.EIGHT_LEGS
import io.github.fyy0619cell.deskpet.render.Feature.EYE_STALKS
import io.github.fyy0619cell.deskpet.render.Feature.FUZZY
import kotlin.math.sin

/** Spiders, crabs, scorpions and trilobites. */
class ArthropodRenderer(look: Look) : PetRenderer(look) {
    private val spider = look.has(EIGHT_LEGS)
    private val crab = look.has(EYE_STALKS)
    private val scorpion = look.has(CURLED_STINGER)
    private val bw = 70f * look.bodyLength
    private val bh = 34f * look.bodyHeight
    private val legColor = look.palette.base
    private val farLeg = Tone(ColorX.darker(look.palette.base, 0.1f))

    override val shadowHalfWidth = bw * 0.7f + 14f
    override val shadowCenterX = 100f

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        when {
            spider -> spider(c)
            crab -> crab(c)
            scorpion -> scorpion(c)
            else -> trilobite(c)
        }
    }

    private fun spider(c: Canvas) {
        val cy = GROUND - 30f * look.legLength + tuck * 16f - breathe * 0.4f
        val abdX = 80f
        val headX = 118f
        for (i in 0 until 4) spiderLeg(c, headX - 6f + i * 3f, cy + 4f, i, far = true)
        kit.ovalShaded(c, abdX, cy - 6f, bw * 0.5f, bh * 0.85f, base)
        if (look.pattern == Pattern.SPOTS) {
            val dot = kit.solid(accent.base)
            c.drawCircle(abdX - 6f, cy - 16f, 4f, dot)
            c.drawCircle(abdX + 8f, cy - 10f, 3.4f, dot)
            c.drawCircle(abdX - 2f, cy - 2f, 3f, dot)
        }
        if (look.has(FUZZY)) fuzz(c, abdX, cy - 6f, bw * 0.5f)
        kit.sheen(c, abdX, cy - 6f, bw * 0.5f, bh * 0.85f)
        kit.ovalShaded(c, headX, cy + 2f, 22f, 18f, base)
        for (i in 0 until 4) spiderLeg(c, headX - 4f + i * 4f, cy + 6f, i, far = false)
        faceFront(c, headX + 8f, cy - 2f, 7f, twoSmallEyes = true)
    }

    private fun spiderLeg(c: Canvas, x: Float, y: Float, i: Int, far: Boolean) {
        val st = kit.stroke
        st.strokeWidth = if (far) 3f else 3.8f
        st.color = if (far) farLeg.base else legColor
        st.shader = null
        val dir = if (i < 2) 1f else -1f
        val reach = (16f + (i % 2) * 10f) * look.legLength
        val swing = legSwing * 0.25f * (if (i % 2 == 0) 1 else -1)
        val kneeX = x + dir * reach * 0.7f + swing * 0.3f
        val kneeY = y - 18f * look.legLength + tuck * 10f
        val p = kit.path
        p.rewind()
        p.moveTo(x, y)
        p.lineTo(kneeX, kneeY)
        p.lineTo(kneeX + dir * reach * 0.55f + swing * 0.2f, if (tuck > 0.5f) y + 8f else GROUND - 2f)
        c.drawPath(p, st)
    }

    /** Crabs read best face-on: wide shell, claws raised on both sides. */
    private fun crab(c: Canvas) {
        val cy = GROUND - 28f * look.legLength + tuck * 10f - breathe * 0.4f
        val cx = 100f
        val rx = 40f * look.bodyLength
        val ry = 22f * look.bodyHeight
        for (side in intArrayOf(-1, 1)) {
            for (i in 0 until 3) frontLeg(c, cx + side * rx * 0.6f, cy + 2f + i * 4f, i, side)
        }
        val raise = if (eyeMode == 1) 12f else if (tuck > 0.5f) -10f else 0f
        for (side in intArrayOf(-1, 1)) frontClaw(c, cx + side * rx * 0.75f, cy - 2f, side, raise)
        eyeStalks(c, cx - 6f, cy - ry * 0.8f)
        val p = kit.path
        p.rewind()
        p.moveTo(cx - rx, cy + ry * 0.3f)
        p.cubicTo(cx - rx * 1.05f, cy - ry * 1.15f, cx + rx * 1.05f, cy - ry * 1.15f, cx + rx, cy + ry * 0.3f)
        p.quadTo(cx, cy + ry * 0.95f, cx - rx, cy + ry * 0.3f)
        p.close()
        c.drawPath(p, kit.shaded(base, cy - ry, cy + ry))
        kit.sheen(c, cx, cy - ry * 0.25f, rx, ry * 0.7f)
        kit.blush(c, cx - rx * 0.45f, cy + ry * 0.15f, 5f)
        kit.blush(c, cx + rx * 0.45f, cy + ry * 0.15f, 5f)
        smile(c, cx, cy + ry * 0.2f)
    }

    private fun frontLeg(c: Canvas, x: Float, y: Float, i: Int, side: Int) {
        val st = kit.stroke
        st.strokeWidth = 4.6f; st.color = base.dark; st.shader = null
        val swing = legSwing * 0.18f * (if ((i + side) % 2 == 0) 1 else -1)
        val kneeX = x + side * (16f + i * 6f) + swing * 0.3f
        val kneeY = y - 8f + i * 2f + tuck * 8f
        val footX = kneeX + side * (6f + i * 2f) + swing * 0.2f
        val p = kit.path
        p.rewind()
        p.moveTo(x, y)
        p.lineTo(kneeX, kneeY)
        p.lineTo(footX, if (tuck > 0.5f) y + 8f else GROUND - 2f)
        c.drawPath(p, st)
    }

    private fun frontClaw(c: Canvas, x: Float, y: Float, side: Int, raise: Float) {
        val open = if (eyeMode == 1) 0.5f + 0.5f * kotlin.math.abs(sin(t * 8f)) else 0.25f + 0.15f * sin(t * 2.4f)
        val hx = x + side * 14f
        val hy = y - 22f - raise
        kit.line(c, x, y, hx, hy + 6f, 7f, accent.dark)
        c.save()
        c.translate(hx, hy)
        c.scale(side.toFloat(), 1f)
        kit.ovalShaded(c, 4f, -8f, 13f, 11f, accent)
        c.save()
        c.rotate(-35f * open, 10f, -14f)
        kit.ovalShaded(c, 12f, -22f, 5f, 9f, accent)
        c.restore()
        kit.sheen(c, 4f, -8f, 13f, 11f)
        c.restore()
    }

    private fun crabLeg(c: Canvas, x: Float, y: Float, i: Int, far: Boolean, dir: Float) {
        val st = kit.stroke
        st.strokeWidth = if (far) 4f else 5f
        st.color = if (far) farLeg.base else legColor
        st.shader = null
        val swing = legSwing * 0.2f * (if (i % 2 == 0) 1 else -1)
        val p = kit.path
        p.rewind()
        p.moveTo(x, y)
        p.lineTo(x + dir * (12f + i * 4f) + swing * 0.3f, y - 6f + tuck * 6f)
        p.lineTo(x + dir * (16f + i * 5f) + swing * 0.2f, if (tuck > 0.5f) y + 6f else GROUND - 2f)
        c.drawPath(p, st)
    }

    private fun claw(c: Canvas, x: Float, y: Float, far: Boolean) {
        val tone = if (far) farLeg else accent
        val open = if (eyeMode == 1) 0.6f else 0.25f + 0.15f * sin(t * 2.4f)
        kit.line(c, x - 14f, y + 4f, x - 2f, y - 6f, 6f, tone.base)
        c.save()
        c.rotate(-30f * open, x, y - 6f)
        kit.ovalShaded(c, x + 8f, y - 12f, 13f, 7f, tone)
        c.restore()
        c.save()
        c.rotate(18f * open, x, y - 6f)
        kit.ovalShaded(c, x + 8f, y - 2f, 11f, 5f, tone)
        c.restore()
    }

    private fun eyeStalks(c: Canvas, x: Float, y: Float) {
        for (i in 0..1) {
            val ex = x + i * 12f
            val ey = y - 14f + (if (tuck > 0.5f) 10f else 0f)
            kit.line(c, ex, y + 6f, ex, ey, 3.2f, base.dark)
            c.drawCircle(ex, ey, 6f, kit.solid(Color.WHITE))
            kit.eye(c, ex + 1f, ey, 3.6f, blink, eyeMode, look.palette.eye)
        }
    }

    private fun scorpion(c: Canvas) {
        val cy = GROUND - 18f * look.legLength + tuck * 6f - breathe * 0.3f
        val cx = 104f
        for (i in 0 until 4) crabLeg(c, cx - 18f + i * 8f, cy + 2f, i, far = true, dir = -1f)
        claw(c, cx + 34f, cy - 4f, far = true)
        tailSegments(c, cx - bw * 0.42f, cy - 4f)
        kit.ovalShaded(c, cx, cy - 2f, bw * 0.5f, bh * 0.45f, base)
        val st = kit.stroke
        st.strokeWidth = 1.6f; st.color = base.deep; st.shader = null
        for (i in 1..4) {
            val x = cx - bw * 0.45f + i * bw * 0.18f
            c.drawLine(x, cy - bh * 0.4f, x - 2f, cy + bh * 0.3f, st)
        }
        kit.sheen(c, cx, cy - 2f, bw * 0.5f, bh * 0.45f)
        for (i in 0 until 4) crabLeg(c, cx - 16f + i * 8f, cy + 4f, i, far = false, dir = -1f)
        claw(c, cx + 38f, cy, far = false)
        faceFront(c, cx + bw * 0.38f, cy - 6f, 5f, twoSmallEyes = false)
    }

    private fun tailSegments(c: Canvas, x: Float, y: Float) {
        val curlUp = 1f - tuck * 0.5f
        val sway = tailSway * 3f
        var px = x
        var py = y
        val paint = kit.shaded(base, y - 70f, y)
        // Angles grow from back-and-slightly-up (200°) through straight up (270°) so the
        // tail arches over the back the way a scorpion's does.
        for (i in 0 until 5) {
            val a = Math.toRadians((200.0 + i * 30.0 * curlUp)).toFloat()
            val nx = px + kotlin.math.cos(a) * 13f
            val ny = py + kotlin.math.sin(a) * 13f + (if (i > 2) sway else 0f)
            c.drawCircle(nx, ny, 8f - i * 0.6f, paint)
            px = nx; py = ny
        }
        val p = kit.path
        p.rewind()
        p.moveTo(px - 4f, py)
        p.quadTo(px + 10f, py + 2f, px + 12f, py + 12f)
        p.quadTo(px + 4f, py + 6f, px + 3f, py + 5f)
        p.close()
        c.drawPath(p, kit.solid(accent.deep))
    }

    private fun trilobite(c: Canvas) {
        val cy = GROUND - 16f - breathe * 0.3f + tuck * 2f
        val cx = 98f
        legsUnder(c, cx, cy + bh * 0.35f)
        if (look.has(ANTENNAE)) {
            val st = kit.stroke
            st.strokeWidth = 2f; st.color = base.dark; st.shader = null
            val p = kit.path
            p.rewind()
            p.moveTo(cx + bw * 0.48f, cy)
            p.quadTo(cx + bw * 0.7f, cy - 8f + sin(t * 3f) * 2f, cx + bw * 0.85f, cy - 2f)
            c.drawPath(p, st)
        }
        val p = kit.path
        p.rewind()
        p.moveTo(cx - bw * 0.55f, cy + bh * 0.35f)
        p.cubicTo(cx - bw * 0.55f, cy - bh * 1.1f, cx + bw * 0.55f, cy - bh * 1.1f, cx + bw * 0.55f, cy + bh * 0.35f)
        p.close()
        c.drawPath(p, kit.shaded(base, cy - bh, cy + bh * 0.35f))
        val st = kit.stroke
        st.strokeWidth = 1.8f; st.color = base.deep; st.shader = null
        for (i in 1..7) {
            val x = cx - bw * 0.5f + i * bw * 0.1f
            c.drawLine(x, cy - bh * 0.62f + kotlin.math.abs(i - 4) * 2.5f, x - 2f, cy + bh * 0.3f, st)
        }
        kit.ovalShaded(c, cx + bw * 0.36f, cy - bh * 0.15f, bw * 0.2f, bh * 0.5f, accent)
        kit.sheen(c, cx, cy - bh * 0.3f, bw * 0.55f, bh * 0.6f)
        c.drawCircle(cx + bw * 0.38f, cy - bh * 0.5f, 5f, kit.solid(base.dark))
        kit.eye(c, cx + bw * 0.4f, cy - bh * 0.5f, 3.2f, blink, eyeMode, look.palette.eye)
        kit.blush(c, cx + bw * 0.42f, cy - bh * 0.05f, 4f)
    }

    private fun legsUnder(c: Canvas, cx: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = 2.4f; st.color = farLeg.base; st.shader = null
        for (i in 0 until 6) {
            val x = cx - bw * 0.4f + i * bw * 0.15f
            val swing = legSwing * 0.15f * (if (i % 2 == 0) 1 else -1)
            c.drawLine(x, y - 4f, x + 3f + swing, GROUND - 2f, st)
        }
    }

    private fun faceFront(c: Canvas, x: Float, y: Float, r: Float, twoSmallEyes: Boolean) {
        kit.eye(c, x, y, r, blink, eyeMode, look.palette.eye)
        kit.eye(c, x - r * 2.4f, y + r * 0.2f, r * 0.85f, blink, eyeMode, look.palette.eye)
        if (twoSmallEyes && eyeMode == 0) {
            c.drawCircle(x - r * 0.6f, y - r * 1.6f, r * 0.35f, kit.solid(look.palette.eye))
            c.drawCircle(x - r * 1.8f, y - r * 1.5f, r * 0.35f, kit.solid(look.palette.eye))
        }
        kit.blush(c, x - r * 0.8f, y + r * 1.6f, r * 0.8f)
    }

    private fun smile(c: Canvas, x: Float, y: Float) {
        val p = kit.path
        p.rewind()
        p.moveTo(x - 6f, y)
        p.quadTo(x, y + 5f, x + 6f, y)
        kit.stroke.strokeWidth = 2f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
    }

    private fun fuzz(c: Canvas, cx: Float, cy: Float, r: Float) {
        val paint = kit.solid(base.light)
        for (i in 0 until 12) {
            val a = -3.0f + i * 0.28f
            c.drawCircle(cx + kotlin.math.cos(a) * r * 0.95f, cy + kotlin.math.sin(a) * r * 0.62f, 3.6f, paint)
        }
    }
}
