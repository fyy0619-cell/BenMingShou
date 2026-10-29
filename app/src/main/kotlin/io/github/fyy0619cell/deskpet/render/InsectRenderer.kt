package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import io.github.fyy0619cell.deskpet.render.Feature.ANTENNAE
import io.github.fyy0619cell.deskpet.render.Feature.CLEAR_WINGS
import io.github.fyy0619cell.deskpet.render.Feature.COMPOUND_EYES
import io.github.fyy0619cell.deskpet.render.Feature.FUZZY
import io.github.fyy0619cell.deskpet.render.Feature.STINGER
import kotlin.math.sin

/** Walking is rendered as flying: body lifts, legs dangle, wings beat. */
class InsectRenderer(look: Look) : PetRenderer(look) {
    private val hr = 21f * look.headSize
    private val ax = 33f * look.bodyLength
    private val ay = 23f * look.bodyHeight
    private val thoraxCx = 104f
    private val legReach = 22f * look.legLength
    private val abdomenPath = Path()
    private val wingFill = Color.argb(165, 232, 242, 250)
    private val wingVein = Color.argb(150, 140, 162, 186)
    private val legColor = look.palette.dark
    private val eyeTone = Tone(look.palette.eye)

    override val shadowHalfWidth = 60f
    override val shadowCenterX = 98f

    private var flying = false

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        flying = pose.action == Action.WALK || pose.action == Action.HAPPY
        val hover = if (flying) -38f + sin(t * TAU * 1.6f) * 4f else 0f
        val cy = GROUND - legReach - 12f + hover + tuck * 10f
        val abdCx = thoraxCx - 18f - ax * 0.85f
        val headCx = thoraxCx + 18f + hr * 0.7f
        val headCy = cy - 4f + headTilt * 0.4f

        if (look.has(CLEAR_WINGS)) wing(c, thoraxCx - 4f, cy - 14f, far = true)
        legs(c, thoraxCx, cy + 8f, far = true)

        abdomenPath.rewind()
        c.save()
        c.rotate(-8f + breathe * 1.5f, abdCx + ax, cy)
        kit.rect.set(abdCx - ax, cy - ay, abdCx + ax, cy + ay)
        abdomenPath.addOval(kit.rect, Path.Direction.CW)
        c.drawPath(abdomenPath, kit.shaded(accent, cy - ay, cy + ay))
        c.save()
        c.clipPath(abdomenPath)
        if (look.pattern == Pattern.STRIPES) {
            for (i in 0 until 3) {
                kit.rect.set(abdCx - ax * 0.62f + i * ax * 0.5f, cy - ay, abdCx - ax * 0.62f + i * ax * 0.5f + ax * 0.22f, cy + ay)
                c.drawRect(kit.rect, kit.solid(look.palette.dark))
            }
        } else {
            kit.stroke.strokeWidth = 1.6f; kit.stroke.color = accent.deep; kit.stroke.shader = null
            for (i in 1..3) c.drawLine(abdCx - ax + i * ax * 0.45f, cy - ay, abdCx - ax + i * ax * 0.45f - 4f, cy + ay, kit.stroke)
        }
        c.restore()
        kit.sheen(c, abdCx, cy, ax, ay)
        if (look.has(STINGER)) {
            val p = kit.path
            p.rewind()
            p.moveTo(abdCx - ax + 3f, cy - 4f)
            p.lineTo(abdCx - ax - 9f, cy + 1f)
            p.lineTo(abdCx - ax + 3f, cy + 5f)
            p.close()
            c.drawPath(p, kit.solid(look.palette.dark))
        }
        c.restore()

        kit.ovalShaded(c, thoraxCx, cy, 22f, 19f, base)
        if (look.has(FUZZY)) fuzz(c, thoraxCx, cy, 22f)
        kit.sheen(c, thoraxCx, cy, 22f, 19f)
        legs(c, thoraxCx, cy + 8f, far = false)

        c.save()
        c.rotate(headTilt, headCx - hr, headCy + hr * 0.5f)
        head(c, headCx, headCy)
        c.restore()

        if (look.has(CLEAR_WINGS)) wing(c, thoraxCx - 2f, cy - 16f, far = false)
    }

    private fun wing(c: Canvas, x: Float, y: Float, far: Boolean) {
        val rest = -158f + if (far) -8f else 0f
        val beat = if (flying) -60f + flap * 48f else rest + breathe * 2f
        c.save()
        c.rotate(beat, x, y)
        val len = 54f * look.bodyLength
        kit.oval(c, x, y - 13f, x + len, y + 13f, kit.solid(wingFill))
        kit.stroke.strokeWidth = 1.3f; kit.stroke.color = wingVein; kit.stroke.shader = null
        kit.rect.set(x, y - 13f, x + len, y + 13f)
        c.drawOval(kit.rect, kit.stroke)
        c.drawLine(x + 4f, y, x + len - 6f, y - 2f, kit.stroke)
        c.drawLine(x + len * 0.35f, y - 1f, x + len * 0.7f, y + 6f, kit.stroke)
        c.restore()
    }

    private fun legs(c: Canvas, x: Float, y: Float, far: Boolean) {
        val st = kit.stroke
        st.strokeWidth = if (far) 2.6f else 3.2f
        st.color = if (far) ColorX.withAlpha(legColor, 170) else legColor
        st.shader = null
        val p = kit.path
        p.rewind()
        for (i in -1..1) {
            val sx = x + i * 11f + (if (far) 5f else 0f)
            val swing = if (flying) 0f else legSwing * 0.3f * (if (i == 0) -1 else 1)
            if (flying || tuck > 0.5f) {
                p.moveTo(sx, y)
                p.quadTo(sx + i * 6f - 4f, y + 12f, sx + i * 4f - 10f, y + 18f + sin(t * 6f + i) * 2f)
            } else {
                val kneeX = sx + i * 9f + swing * 0.3f
                p.moveTo(sx, y)
                p.lineTo(kneeX, y + legReach * 0.42f)
                p.lineTo(kneeX + i * 4f + swing * 0.2f, GROUND - 2f)
            }
        }
        c.drawPath(p, st)
    }

    private fun head(c: Canvas, cx: Float, cy: Float) {
        if (look.has(ANTENNAE)) antennae(c, cx + hr * 0.1f, cy - hr * 0.8f)
        kit.ovalShaded(c, cx, cy, hr, hr * 0.95f, base)
        if (look.has(COMPOUND_EYES)) {
            val ex = cx + hr * 0.32f
            val ey = cy - hr * 0.12f
            kit.ovalC(c, ex, ey, hr * 0.52f, hr * 0.6f, kit.shaded(eyeTone, ey - hr * 0.6f, ey + hr * 0.6f))
            if (eyeMode == 0 && blink < 0.85f) {
                c.drawCircle(ex + hr * 0.12f, ey - hr * 0.08f, hr * 0.16f, kit.solid(0xFF2B2233.toInt()))
                c.drawCircle(ex + hr * 0.18f, ey - hr * 0.16f, hr * 0.07f, kit.solid(Color.WHITE))
                c.drawCircle(ex - hr * 0.18f, ey - hr * 0.3f, hr * 0.12f, kit.solid(Color.argb(90, 255, 255, 255)))
            } else {
                kit.eye(c, ex + hr * 0.1f, ey, hr * 0.2f, 1f, eyeMode, 0xFF2B2233.toInt())
            }
        } else {
            kit.eye(c, cx + hr * 0.38f, cy - hr * 0.12f, hr * 0.2f, blink, eyeMode, look.palette.eye)
        }
        kit.blush(c, cx + hr * 0.3f, cy + hr * 0.45f, hr * 0.2f)
        kit.sheen(c, cx, cy, hr, hr)
    }

    private fun antennae(c: Canvas, x: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = 2.2f; st.color = look.palette.dark; st.shader = null
        val droop = tuck * 10f
        val p = kit.path
        p.rewind()
        for (i in 0..1) {
            val ox = x + i * 7f
            p.moveTo(ox, y + 4f)
            p.quadTo(ox + 6f, y - 12f + droop, ox + 14f + i * 3f, y - 16f + droop + sin(t * 3f + i) * 1.5f)
        }
        c.drawPath(p, st)
        c.drawCircle(x + 14f, y - 16f + droop, 3f, kit.solid(look.palette.dark))
        c.drawCircle(x + 24f, y - 16f + droop, 3f, kit.solid(look.palette.dark))
    }

    private fun fuzz(c: Canvas, cx: Float, cy: Float, r: Float) {
        val paint = kit.solid(base.light)
        for (i in 0 until 10) {
            val a = -2.6f + i * 0.28f
            c.drawCircle(cx + kotlin.math.cos(a) * r * 0.92f, cy + kotlin.math.sin(a) * r * 0.88f, 4.2f, paint)
        }
    }
}
