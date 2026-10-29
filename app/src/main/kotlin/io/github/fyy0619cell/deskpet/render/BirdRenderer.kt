package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import io.github.fyy0619cell.deskpet.render.Feature.BEAK_FLAT
import io.github.fyy0619cell.deskpet.render.Feature.BEAK_HOOK
import io.github.fyy0619cell.deskpet.render.Feature.BEAK_LONG
import io.github.fyy0619cell.deskpet.render.Feature.COMB
import io.github.fyy0619cell.deskpet.render.Feature.CREST
import io.github.fyy0619cell.deskpet.render.Feature.EAR_TUFTS
import io.github.fyy0619cell.deskpet.render.Feature.LONG_LEGS
import io.github.fyy0619cell.deskpet.render.Feature.LONG_NECK
import io.github.fyy0619cell.deskpet.render.Feature.LONG_TAIL
import io.github.fyy0619cell.deskpet.render.Feature.MEMBRANE_WINGS
import io.github.fyy0619cell.deskpet.render.Feature.UPRIGHT
import kotlin.math.abs
import kotlin.math.sin

class BirdRenderer(look: Look) : PetRenderer(look) {
    private val upright = look.has(UPRIGHT)
    private val owl = look.has(EAR_TUFTS)
    private val canFly = look.locomotion == Locomotion.FLY
    private val membrane = look.has(MEMBRANE_WINGS)
    private val bw = (if (upright) 46f else 56f) * look.bodyLength
    private val bh = (if (upright) 62f else 44f) * look.bodyHeight
    private val hr = 20f * look.headSize
    private val legLen = 18f * look.legLength
    private val bodyCx = 94f
    private val beakColor = if (look.has(COMB) || upright) 0xFFF2A341.toInt() else 0xFFE9B455.toInt()
    private val beakTone = Tone(if (membrane) look.palette.base else beakColor)
    private val legColor = when {
        membrane -> look.palette.base
        look.has(LONG_LEGS) -> ColorX.darker(look.palette.base, 0.06f)
        else -> 0xFFE59A4E.toInt()
    }
    private val wingTone = Tone(look.palette.accent)
    private val farWing = Tone(ColorX.darker(look.palette.accent, 0.08f))
    private val combColor = 0xFFE0524F.toInt()

    override val shadowHalfWidth = bw * 0.6f + 6f
    override val shadowCenterX = bodyCx

    private var flying = false

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        flying = canFly && (pose.action == Action.WALK || pose.action == Action.HAPPY)
        val hover = if (flying) -40f + sin(t * TAU * 1.8f) * 4f else 0f
        val legsVisible = if (flying) 0f else legLen * (1f - tuck * 0.9f)
        val bodyCy = GROUND - legsVisible - bh * 0.5f + 4f + hover - breathe * 0.4f
        val waddle = if (!flying && pose.action == Action.WALK) sin(pose.walkPhase * TAU) * 6f else 0f

        c.save()
        c.rotate(waddle, bodyCx, GROUND)
        tail(c, bodyCx - bw * 0.48f, bodyCy + (if (upright) bh * 0.32f else 0f))
        if (flying) wing(c, bodyCx, bodyCy - bh * 0.15f, farWing, far = true)
        if (legsVisible > 1f) legs(c, bodyCy + bh * 0.4f)
        else if (flying) tuckedLegs(c, bodyCy + bh * 0.4f)

        kit.ovalShaded(c, bodyCx, bodyCy, bw / 2f, bh / 2f, base)
        if (upright && !owl) {
            kit.ovalC(c, bodyCx + bw * 0.13f, bodyCy + bh * 0.06f, bw * 0.3f, bh * 0.4f, kit.solid(belly.base))
        } else {
            kit.ovalC(c, bodyCx + bw * (if (upright) 0.1f else 0.12f), bodyCy + bh * 0.1f, bw * 0.34f, bh * 0.36f, kit.solid(belly.base))
        }
        kit.sheen(c, bodyCx, bodyCy, bw / 2f, bh / 2f)
        if (flying) wing(c, bodyCx, bodyCy - bh * 0.15f, wingTone, far = false)
        else foldedWing(c, bodyCx - bw * (if (upright) 0.2f else 0.05f), bodyCy - bh * 0.05f)

        val hx = bodyCx + (if (upright) bw * 0.12f else bw * 0.42f)
        val penguinLike = upright && !owl
        var hy = bodyCy - bh * (if (penguinLike) 0.36f else if (upright) 0.5f else 0.42f) -
            hr * (if (penguinLike) 0.2f else 0.35f) + tuck * hr * 0.5f
        if (look.has(LONG_NECK)) {
            hy -= 34f * (1f - tuck * 0.6f)
            neck(c, bodyCx + bw * 0.32f, bodyCy - bh * 0.2f, hx, hy + hr * 0.5f)
        }
        c.save()
        c.rotate(headTilt, hx, hy + hr)
        if (owl) owlHead(c, hx, hy) else head(c, hx, hy)
        c.restore()
        c.restore()
    }

    private fun legs(c: Canvas, top: Float) {
        val st = kit.stroke
        st.strokeWidth = if (look.has(LONG_LEGS)) 3.4f else 3f
        st.color = legColor; st.shader = null
        for (i in 0..1) {
            val x = bodyCx + (i * 10f - 4f)
            val swing = legSwing * 0.35f * (if (i == 0) 1 else -1)
            val footX = x + swing
            c.drawLine(x, top - 4f, footX, GROUND - 2f, st)
            if (upright && !owl) {
                kit.ovalC(c, footX + 5f, GROUND - 2f, 8f, 3.2f, kit.solid(legColor))
            } else {
                c.drawLine(footX, GROUND - 2f, footX + 8f, GROUND - 1f, st)
                c.drawLine(footX, GROUND - 2f, footX - 4f, GROUND - 1f, st)
            }
        }
    }

    private fun tuckedLegs(c: Canvas, y: Float) {
        val st = kit.stroke
        st.strokeWidth = 3f; st.color = legColor; st.shader = null
        c.drawLine(bodyCx - 2f, y - 4f, bodyCx - 14f, y + 6f, st)
        c.drawLine(bodyCx + 6f, y - 4f, bodyCx - 6f, y + 7f, st)
    }

    private fun tail(c: Canvas, x: Float, y: Float) {
        val long = look.has(LONG_TAIL)
        val len = if (long) 52f else if (upright && !owl) 10f else 22f
        val angle = if (upright) 30f else -12f + tailSway * 4f
        c.save()
        c.rotate(angle, x, y)
        val p = kit.shaded(wingTone, y - 8f, y + 8f)
        for (i in -1..1) {
            c.save()
            c.rotate(i * 12f, x + 6f, y)
            kit.oval(c, x - len, y - 5f, x + 8f, y + 5f, p)
            c.restore()
        }
        c.restore()
    }

    private fun foldedWing(c: Canvas, cx: Float, cy: Float) {
        c.save()
        c.rotate(if (upright) -70f else -8f, cx, cy)
        val p = kit.path
        val w = (if (upright) bh * 0.85f else bw) * 0.5f
        val h = (if (upright) bw * 0.62f else bh) * 0.28f
        p.rewind()
        p.moveTo(cx + w * 0.55f, cy - h * 0.6f)
        p.quadTo(cx + w * 0.7f, cy + h * 0.9f, cx - w * 0.2f, cy + h * 0.7f)
        p.quadTo(cx - w * 0.75f, cy + h * 0.5f, cx - w * 0.95f, cy + h * 0.1f)
        p.quadTo(cx - w * 0.2f, cy - h * 1.1f, cx + w * 0.55f, cy - h * 0.6f)
        p.close()
        c.drawPath(p, kit.shaded(wingTone, cy - h, cy + h))
        c.restore()
    }

    private fun wing(c: Canvas, x: Float, y: Float, tone: Tone, far: Boolean) {
        val beat = flap * 50f + (if (far) -12f else 0f)
        c.save()
        c.rotate(-beat - 20f, x, y)
        val p = kit.path
        val span = if (membrane) 74f else 58f
        p.rewind()
        if (membrane) {
            p.moveTo(x - 4f, y)
            p.lineTo(x + span * 0.15f, y - span)
            p.quadTo(x - span * 0.25f, y - span * 0.45f, x - span * 0.55f, y + 4f)
            p.close()
            c.drawPath(p, kit.shaded(tone, y - span, y))
            kit.line(c, x - 2f, y, x + span * 0.15f, y - span, 2.6f, tone.deep)
        } else {
            p.moveTo(x - 10f, y + 4f)
            p.quadTo(x - 8f, y - span * 0.8f, x + span * 0.18f, y - span)
            p.quadTo(x + span * 0.05f, y - span * 0.5f, x + 14f, y + 2f)
            p.close()
            c.drawPath(p, kit.shaded(tone, y - span, y))
            kit.stroke.strokeWidth = 1.4f; kit.stroke.color = tone.deep; kit.stroke.shader = null
            for (i in 1..2) c.drawLine(x - 4f + i * 4f, y - 4f, x - 6f + i * 9f, y - span * (0.45f + i * 0.15f), kit.stroke)
        }
        c.restore()
    }

    private fun neck(c: Canvas, x0: Float, y0: Float, x1: Float, y1: Float) {
        val st = kit.stroke
        st.strokeWidth = hr * 0.55f; st.color = base.base; st.shader = null
        val p = kit.path
        p.rewind()
        p.moveTo(x0, y0)
        p.cubicTo(x0 + 22f, y0 - 10f, x1 - 24f, (y0 + y1) / 2f, x1, y1)
        c.drawPath(p, st)
    }

    private fun head(c: Canvas, cx: Float, cy: Float) {
        if (look.has(CREST)) crest(c, cx, cy)
        if (look.has(COMB)) comb(c, cx, cy)
        kit.ovalShaded(c, cx, cy, hr, hr * 0.95f, base)
        kit.sheen(c, cx, cy, hr, hr)
        beak(c, cx + hr * 0.82f, cy + hr * 0.08f)
        val ex = cx + hr * 0.3f
        val ey = cy - hr * 0.12f
        kit.eye(c, ex, ey, hr * 0.17f, blink, eyeMode, look.palette.eye)
        if (!membrane) kit.blush(c, ex - hr * 0.05f, cy + hr * 0.35f, hr * 0.17f)
    }

    private fun owlHead(c: Canvas, cx: Float, cy: Float) {
        val p = kit.path
        p.rewind()
        for (i in 0..1) {
            val tx = cx - hr * 0.55f + i * hr * 1.1f
            p.moveTo(tx - hr * 0.22f, cy - hr * 0.6f)
            p.lineTo(tx + (if (i == 0) -hr * 0.25f else hr * 0.25f), cy - hr * 1.2f)
            p.lineTo(tx + hr * 0.25f * (if (i == 0) 1 else -1) + (if (i == 0) 0f else hr * 0.1f), cy - hr * 0.55f)
            p.close()
        }
        c.drawPath(p, kit.solid(base.dark))
        kit.ovalShaded(c, cx, cy, hr * 1.15f, hr, base)
        val disc = kit.solid(belly.base)
        c.drawCircle(cx - hr * 0.42f, cy, hr * 0.48f, disc)
        c.drawCircle(cx + hr * 0.42f, cy, hr * 0.48f, disc)
        if (eyeMode == 0 && blink < 0.85f) {
            for (i in 0..1) {
                val ex = cx - hr * 0.42f + i * hr * 0.84f
                c.drawCircle(ex, cy, hr * 0.3f, kit.solid(0xFFF3C14B.toInt()))
            }
        }
        kit.eye(c, cx - hr * 0.42f, cy, hr * 0.17f, blink, eyeMode, look.palette.eye)
        kit.eye(c, cx + hr * 0.42f, cy, hr * 0.17f, blink, eyeMode, look.palette.eye)
        p.rewind()
        p.moveTo(cx - hr * 0.12f, cy + hr * 0.18f)
        p.quadTo(cx, cy + hr * 0.6f, cx + hr * 0.12f, cy + hr * 0.18f)
        p.close()
        c.drawPath(p, kit.solid(beakColor))
        kit.sheen(c, cx, cy, hr * 1.1f, hr)
    }

    private fun beak(c: Canvas, x: Float, y: Float) {
        val p = kit.path
        p.rewind()
        when {
            look.has(BEAK_HOOK) -> {
                val s = hr * (if (look.headSize > 1.2f) 0.95f else 0.6f)
                p.moveTo(x - s * 0.3f, y - s * 0.45f)
                p.quadTo(x + s * 0.9f, y - s * 0.55f, x + s * 0.75f, y + s * 0.35f)
                p.quadTo(x + s * 0.4f, y + s * 0.05f, x + s * 0.2f, y + s * 0.25f)
                p.lineTo(x - s * 0.3f, y + s * 0.35f)
                p.close()
            }
            look.has(BEAK_FLAT) -> {
                kit.roundRect(c, x - hr * 0.3f, y - hr * 0.18f, x + hr * 0.75f, y + hr * 0.2f, hr * 0.18f, kit.shaded(beakTone, y - hr * 0.2f, y + hr * 0.2f))
                return
            }
            look.has(BEAK_LONG) -> {
                val s = hr * (if (membrane) 2.1f else 1.25f)
                p.moveTo(x - hr * 0.2f, y - hr * 0.25f)
                p.quadTo(x + s * 0.6f, y - hr * 0.15f, x + s, y + (if (membrane) 0f else hr * 0.55f))
                p.quadTo(x + s * 0.5f, y + hr * 0.15f, x - hr * 0.2f, y + hr * 0.22f)
                p.close()
            }
            else -> {
                p.moveTo(x - hr * 0.2f, y - hr * 0.2f)
                p.lineTo(x + hr * 0.5f, y + hr * 0.02f)
                p.lineTo(x - hr * 0.2f, y + hr * 0.22f)
                p.close()
            }
        }
        c.drawPath(p, kit.shaded(beakTone, y - hr * 0.4f, y + hr * 0.4f))
    }

    private fun crest(c: Canvas, cx: Float, cy: Float) {
        val p = kit.path
        p.rewind()
        if (membrane) {
            p.moveTo(cx - hr * 0.2f, cy - hr * 0.6f)
            p.lineTo(cx - hr * 1.9f, cy - hr * 1.05f)
            p.lineTo(cx - hr * 0.5f, cy + hr * 0.1f)
            p.close()
        } else {
            for (i in 0..2) {
                p.moveTo(cx - hr * 0.1f, cy - hr * 0.7f)
                p.quadTo(cx - hr * (0.3f + i * 0.25f), cy - hr * (1.5f - i * 0.15f), cx - hr * (0.7f + i * 0.2f), cy - hr * (1.3f - i * 0.25f))
                p.quadTo(cx - hr * (0.4f + i * 0.2f), cy - hr * 0.8f, cx - hr * 0.1f, cy - hr * 0.55f)
            }
        }
        c.drawPath(p, kit.solid(accent.base))
    }

    private fun comb(c: Canvas, cx: Float, cy: Float) {
        val paint = kit.solid(combColor)
        for (i in 0..2) c.drawCircle(cx - hr * 0.35f + i * hr * 0.32f, cy - hr * (0.9f - abs(i - 1) * 0.1f), hr * 0.24f, paint)
        kit.ovalC(c, cx + hr * 0.75f, cy + hr * 0.55f, hr * 0.14f, hr * 0.24f, paint)
    }
}
