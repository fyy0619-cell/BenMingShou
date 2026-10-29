package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import io.github.fyy0619cell.deskpet.render.Feature.ANTENNAE
import io.github.fyy0619cell.deskpet.render.Feature.BIG_WINGS
import io.github.fyy0619cell.deskpet.render.Feature.CLEAR_WINGS
import io.github.fyy0619cell.deskpet.render.Feature.COMPOUND_EYES
import io.github.fyy0619cell.deskpet.render.Feature.ELYTRA
import io.github.fyy0619cell.deskpet.render.Feature.FUZZY
import io.github.fyy0619cell.deskpet.render.Feature.HORN
import io.github.fyy0619cell.deskpet.render.Feature.LONG_ABDOMEN
import io.github.fyy0619cell.deskpet.render.Feature.PROBOSCIS
import io.github.fyy0619cell.deskpet.render.Feature.STINGER
import kotlin.math.abs
import kotlin.math.sin

/** Winged insects render walking as flying: body lifts, legs dangle, wings beat. */
class InsectRenderer(look: Look) : PetRenderer(look) {
    private val hr = 21f * look.headSize
    private val ax = 33f * look.bodyLength
    private val ay = 23f * look.bodyHeight
    private val shelled = look.has(ELYTRA)
    private val butterfly = look.has(BIG_WINGS)
    private val dragonfly = look.has(LONG_ABDOMEN)
    private val ant = look.locomotion == Locomotion.WALK
    private val thoraxR = if (ant) 15f else if (butterfly || dragonfly) 15f else 22f
    private val thoraxCx = if (dragonfly) 112f else 104f
    private val legReach = 22f * look.legLength
    private val abdomenPath = Path()
    private val wingFill = Color.argb(165, 232, 242, 250)
    private val wingVein = Color.argb(150, 140, 162, 186)
    private val legColor = look.palette.dark
    private val eyeTone = Tone(look.palette.eye)
    private val shellTone = Tone(look.palette.accent)
    private val farWingTone = Tone(ColorX.darker(look.palette.accent, 0.09f))
    private val headEyeInk = if (ColorX.hsl(look.palette.base)[2] < 0.3f) 0xFFF4F1EC.toInt() else look.palette.eye

    override val shadowHalfWidth = if (dragonfly) 70f else 60f
    override val shadowCenterX = 98f

    private var flying = false

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        flying = !ant && (pose.action == Action.WALK || pose.action == Action.HAPPY)
        val hover = if (flying) -38f + sin(t * TAU * 1.6f) * 4f else 0f
        val cy = GROUND - legReach - (if (shelled) 16f else 12f) + hover + tuck * 10f
        val abdCx = thoraxCx - thoraxR * 0.8f - ax * 0.85f - (if (ant) 8f else 0f)
        val headCx = thoraxCx + thoraxR * 0.8f + hr * 0.7f
        val headCy = cy - 4f + headTilt * 0.4f

        if (butterfly) butterflyWings(c, thoraxCx - 2f, cy - 8f, far = true)
        if (look.has(CLEAR_WINGS) && !shelled) clearWing(c, thoraxCx - 4f, cy - 12f, far = true)
        legs(c, thoraxCx, cy + 8f, far = true)

        if (shelled) {
            if (flying) clearWing(c, thoraxCx - 6f, cy - 16f, far = false)
            elytra(c, thoraxCx - 6f, cy)
        } else {
            if (ant) c.drawCircle(thoraxCx - thoraxR - 3f, cy + 2f, 5f, kit.solid(base.dark))
            abdomen(c, abdCx, cy)
            kit.ovalShaded(c, thoraxCx, cy, thoraxR, thoraxR * 0.86f, base)
            if (look.has(FUZZY)) fuzz(c, thoraxCx, cy, thoraxR)
            kit.sheen(c, thoraxCx, cy, thoraxR, thoraxR * 0.86f)
        }
        legs(c, thoraxCx, cy + 8f, far = false)

        c.save()
        c.rotate(headTilt, headCx - hr, headCy + hr * 0.5f)
        head(c, headCx, headCy)
        c.restore()

        if (look.has(CLEAR_WINGS) && !shelled) clearWing(c, thoraxCx - 2f, cy - 14f, far = false)
        if (butterfly) butterflyWings(c, thoraxCx, cy - 8f, far = false)
    }

    private fun abdomen(c: Canvas, cx: Float, cy: Float) {
        abdomenPath.rewind()
        c.save()
        c.rotate(-8f + breathe * 1.5f, cx + ax, cy)
        kit.rect.set(cx - ax, cy - ay, cx + ax, cy + ay)
        abdomenPath.addOval(kit.rect, Path.Direction.CW)
        c.drawPath(abdomenPath, kit.shaded(accent, cy - ay, cy + ay))
        c.save()
        c.clipPath(abdomenPath)
        if (look.pattern == Pattern.STRIPES) {
            for (i in 0 until 3) {
                kit.rect.set(cx - ax * 0.62f + i * ax * 0.5f, cy - ay, cx - ax * 0.62f + i * ax * 0.5f + ax * 0.22f, cy + ay)
                c.drawRect(kit.rect, kit.solid(look.palette.dark))
            }
        } else {
            kit.stroke.strokeWidth = 1.6f; kit.stroke.color = accent.deep; kit.stroke.shader = null
            val n = if (dragonfly) 6 else 3
            for (i in 1..n) {
                val x = cx - ax + i * ax * 2f / (n + 1)
                c.drawLine(x, cy - ay, x - 2f, cy + ay, kit.stroke)
            }
        }
        c.restore()
        kit.sheen(c, cx, cy, ax, ay)
        if (look.has(STINGER)) {
            val p = kit.path
            p.rewind()
            p.moveTo(cx - ax + 3f, cy - 4f)
            p.lineTo(cx - ax - 9f, cy + 1f)
            p.lineTo(cx - ax + 3f, cy + 5f)
            p.close()
            c.drawPath(p, kit.solid(look.palette.dark))
        }
        c.restore()
    }

    private fun elytra(c: Canvas, cx: Float, cy: Float) {
        val rx = ax
        val ry = ay
        val open = if (flying) 18f + flap * 6f else 0f
        c.save()
        c.rotate(-open, cx + rx * 0.6f, cy - ry * 0.4f)
        val p = kit.path
        p.rewind()
        p.moveTo(cx - rx, cy + ry * 0.35f)
        p.cubicTo(cx - rx, cy - ry * 1.2f, cx + rx, cy - ry * 1.2f, cx + rx, cy + ry * 0.35f)
        p.close()
        c.drawPath(p, kit.shaded(shellTone, cy - ry, cy + ry * 0.4f))
        if (look.pattern == Pattern.SPOTS) {
            val dot = kit.solid(look.palette.dark)
            c.drawCircle(cx - rx * 0.45f, cy - ry * 0.2f, ry * 0.2f, dot)
            c.drawCircle(cx + rx * 0.05f, cy - ry * 0.45f, ry * 0.22f, dot)
            c.drawCircle(cx + rx * 0.5f, cy - ry * 0.05f, ry * 0.18f, dot)
            c.drawCircle(cx - rx * 0.05f, cy + ry * 0.12f, ry * 0.16f, dot)
        }
        kit.sheen(c, cx, cy - ry * 0.3f, rx, ry * 0.7f)
        c.restore()
        kit.roundRect(c, cx - rx * 0.95f, cy + ry * 0.2f, cx + rx * 0.95f, cy + ry * 0.55f, ry * 0.2f, kit.solid(base.dark))
    }

    private fun clearWing(c: Canvas, x: Float, y: Float, far: Boolean) {
        val rest = if (dragonfly) -172f else -158f + if (far) -8f else 0f
        val beat = if (flying) (if (dragonfly) -150f else -60f) + flap * (if (dragonfly) 18f else 48f) else rest + breathe * 2f
        val len = (if (dragonfly) 64f else 54f) * (if (dragonfly) 1f else look.bodyLength)
        val pairs = if (dragonfly) 2 else 1
        for (k in 0 until pairs) {
            c.save()
            c.rotate(beat + k * 14f + (if (far) -6f else 0f), x, y)
            val h = if (dragonfly) 8f else 13f
            kit.oval(c, x, y - h, x + len, y + h, kit.solid(wingFill))
            kit.stroke.strokeWidth = 1.3f; kit.stroke.color = wingVein; kit.stroke.shader = null
            kit.rect.set(x, y - h, x + len, y + h)
            c.drawOval(kit.rect, kit.stroke)
            c.drawLine(x + 4f, y, x + len - 6f, y - 2f, kit.stroke)
            c.restore()
        }
    }

    private fun butterflyWings(c: Canvas, x: Float, y: Float, far: Boolean) {
        val open = if (flying) 0.35f + 0.65f * abs(flap) else 0.9f + breathe * 0.04f
        val tone = if (far) farWingTone else accent
        c.save()
        c.translate(x, y)
        c.scale(1f, open)
        c.rotate(if (far) -16f else -6f)
        val p = kit.path
        p.rewind()
        p.moveTo(0f, 0f)
        p.cubicTo(-10f, -50f, 40f, -78f, 46f, -46f)
        p.cubicTo(50f, -24f, 20f, -6f, 0f, 0f)
        p.moveTo(0f, 2f)
        p.cubicTo(-30f, -6f, -40f, -44f, -16f, -40f)
        p.cubicTo(-4f, -36f, 2f, -16f, 0f, 2f)
        c.drawPath(p, kit.shaded(tone, -70f, 0f))
        if (!far) {
            c.drawCircle(28f, -44f, 7f, kit.solid(look.palette.belly))
            c.drawCircle(28f, -44f, 3.5f, kit.solid(look.palette.dark))
            c.drawCircle(-18f, -28f, 4.5f, kit.solid(look.palette.belly))
            kit.stroke.strokeWidth = 2f; kit.stroke.color = tone.deep; kit.stroke.shader = null
            c.drawPath(p, kit.stroke)
        }
        c.restore()
    }

    private fun legs(c: Canvas, x: Float, y: Float, far: Boolean) {
        val st = kit.stroke
        st.strokeWidth = if (far) 2.4f else 3f
        st.color = if (far) ColorX.withAlpha(legColor, 170) else legColor
        st.shader = null
        val p = kit.path
        p.rewind()
        val spread = if (ant) 9f else 11f
        for (i in -1..1) {
            val sx = x + i * spread + (if (far) 5f else 0f)
            val swing = if (flying) 0f else legSwing * 0.3f * (if (i == 0) -1 else 1)
            if (flying || tuck > 0.5f) {
                p.moveTo(sx, y)
                p.quadTo(sx + i * 6f - 4f, y + 12f, sx + i * 4f - 10f, y + 18f + sin(t * 6f + i) * 2f)
            } else {
                val kneeX = sx + i * 9f + swing * 0.3f
                p.moveTo(sx, y)
                p.lineTo(kneeX, y - legReach * 0.15f)
                p.lineTo(kneeX + i * 5f + swing * 0.2f, GROUND - 2f)
            }
        }
        c.drawPath(p, st)
    }

    private fun head(c: Canvas, cx: Float, cy: Float) {
        if (look.has(ANTENNAE)) antennae(c, cx + hr * 0.1f, cy - hr * 0.8f)
        if (look.has(HORN)) {
            val p = kit.path
            p.rewind()
            p.moveTo(cx + hr * 0.4f, cy - hr * 0.2f)
            p.quadTo(cx + hr * 1.5f, cy - hr * 0.4f, cx + hr * 1.25f, cy - hr * 1.6f)
            p.quadTo(cx + hr * 1.05f, cy - hr * 0.6f, cx + hr * 0.2f, cy - hr * 0.7f)
            p.close()
            c.drawPath(p, kit.shaded(base, cy - hr * 1.6f, cy))
        }
        kit.ovalShaded(c, cx, cy, hr, hr * 0.95f, base)
        if (look.has(PROBOSCIS)) {
            kit.line(c, cx + hr * 0.8f, cy + hr * 0.3f, cx + hr * 2.1f, cy + hr * 1.3f, 2.2f, look.palette.dark)
        }
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
            kit.eye(c, cx + hr * 0.38f, cy - hr * 0.12f, hr * 0.2f, blink, eyeMode, headEyeInk)
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
            if (ant) {
                p.lineTo(ox + 3f, y - 10f + droop)
                p.lineTo(ox + 14f + i * 3f, y - 14f + droop + sin(t * 3f + i) * 1.5f)
            } else {
                p.quadTo(ox + 6f, y - 12f + droop, ox + 14f + i * 3f, y - 16f + droop + sin(t * 3f + i) * 1.5f)
            }
        }
        c.drawPath(p, st)
        if (!ant) {
            c.drawCircle(x + 14f, y - 16f + droop, 3f, kit.solid(look.palette.dark))
            c.drawCircle(x + 24f, y - 16f + droop, 3f, kit.solid(look.palette.dark))
        }
    }

    private fun fuzz(c: Canvas, cx: Float, cy: Float, r: Float) {
        val paint = kit.solid(base.light)
        for (i in 0 until 10) {
            val a = -2.6f + i * 0.28f
            c.drawCircle(cx + kotlin.math.cos(a) * r * 0.92f, cy + kotlin.math.sin(a) * r * 0.88f, 4.2f, paint)
        }
    }
}
