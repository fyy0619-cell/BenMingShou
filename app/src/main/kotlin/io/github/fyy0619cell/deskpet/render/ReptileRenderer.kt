package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Path
import io.github.fyy0619cell.deskpet.render.Feature.ARMOR
import io.github.fyy0619cell.deskpet.render.Feature.BACK_PLATES
import io.github.fyy0619cell.deskpet.render.Feature.CLUB_TAIL
import io.github.fyy0619cell.deskpet.render.Feature.CREST
import io.github.fyy0619cell.deskpet.render.Feature.CURLED_TAIL
import io.github.fyy0619cell.deskpet.render.Feature.EYE_TURRET
import io.github.fyy0619cell.deskpet.render.Feature.FRILL_HORNS
import io.github.fyy0619cell.deskpet.render.Feature.LONG_NECK
import io.github.fyy0619cell.deskpet.render.Feature.LONG_SNOUT
import io.github.fyy0619cell.deskpet.render.Feature.SHELL
import io.github.fyy0619cell.deskpet.render.Feature.TAIL_SPIKES
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Sprawling or pillar-legged four-footed reptiles and dinosaurs. */
class ReptileRenderer(look: Look) : PetRenderer(look) {
    private val shell = look.has(SHELL)
    private val longNeck = look.has(LONG_NECK)
    private val croc = look.has(LONG_SNOUT)
    private val bw = 86f * look.bodyLength
    private val bh = 42f * look.bodyHeight
    private val legLen = 20f * look.legLength
    private val hr = 22f * look.headSize
    private val lw = 14f * (0.75f + 0.25f * look.bodyHeight)
    private val bodyCx = when {
        longNeck -> 80f
        croc -> 90f
        else -> 92f - (bw - 86f) * 0.2f
    }
    private val far = Tone(base.dark)
    private val bodyPath = Path()
    private val horn = 0xFFEFE4CC.toInt()
    private val spots = Patterns.spotOffsets(look.patternSeed, 10)

    override val shadowHalfWidth = bw * 0.7f + 14f
    override val shadowCenterX = bodyCx + 4f

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        val standing = 1f - tuck * 0.85f
        val bodyBottom = GROUND - legLen * standing + 4f
        val bodyCy = bodyBottom - bh / 2f - breathe * 0.5f
        val legTop = bodyCy + bh * 0.1f
        val legH = GROUND - legTop
        val backX = bodyCx - bw * 0.28f
        val frontX = bodyCx + bw * 0.26f

        tail(c, bodyCx - bw * 0.42f, bodyCy + bh * 0.04f)
        if (standing > 0.3f) {
            leg(c, backX + 7f, legTop, legH, -legSwing, far)
            leg(c, frontX + 7f, legTop, legH, legSwing, far)
        }
        if (look.has(BACK_PLATES)) plates(c, bodyCy - bh / 2f)

        if (!shell) {
            bodyPath.rewind()
            kit.rect.set(bodyCx - bw / 2f, bodyCy - bh / 2f, bodyCx + bw / 2f, bodyCy + bh / 2f)
            bodyPath.addOval(kit.rect, Path.Direction.CW)
            c.drawPath(bodyPath, kit.shaded(base, bodyCy - bh / 2f, bodyCy + bh / 2f))
            c.save()
            c.clipPath(bodyPath)
            kit.ovalC(c, bodyCx + bw * 0.06f, bodyCy + bh * 0.45f, bw * 0.42f, bh * 0.3f, kit.solid(belly.base))
            if (look.has(ARMOR)) armor(c, bodyCy)
            if (look.pattern == Pattern.SPOTS) for (i in 0 until spots.size / 2) {
                c.drawCircle(bodyCx + spots[2 * i] * bw * 0.42f, bodyCy + spots[2 * i + 1] * bh * 0.25f - 4f, 3f + i % 2, kit.solid(accent.base))
            }
            if (croc) scutes(c, bodyCy - bh / 2f)
            c.restore()
            kit.sheen(c, bodyCx, bodyCy, bw / 2f, bh / 2f)
        }

        if (standing > 0.3f) {
            leg(c, backX, legTop, legH, legSwing, base)
            leg(c, frontX, legTop, legH, -legSwing, base)
        } else {
            kit.ovalC(c, frontX + 10f, GROUND - 4f, lw * 0.9f, lw * 0.45f, kit.solid(base.dark))
        }
        if (shell) shellDome(c, bodyCy)

        val (hx, hy) = headPos(bodyCy)
        if (longNeck) neck(c, bodyCx + bw * 0.32f, bodyCy - bh * 0.15f, hx - hr * 0.4f, hy + hr * 0.3f)
        c.save()
        c.rotate(headTilt, hx - hr * 0.6f, hy + hr * 0.5f)
        head(c, hx, hy)
        c.restore()
    }

    private val headXY = FloatArray(2)

    private fun headPos(bodyCy: Float): FloatArray {
        when {
            longNeck -> {
                headXY[0] = bodyCx + bw * 0.55f + 26f
                headXY[1] = (bodyCy - bh * 0.5f - 62f) * (1f - tuck) + (GROUND - hr * 0.7f) * tuck
            }
            shell -> {
                headXY[0] = bodyCx + bw * 0.5f + hr * 0.4f - tuck * hr * 0.5f
                headXY[1] = bodyCy + bh * 0.05f
            }
            else -> {
                headXY[0] = bodyCx + bw * 0.48f + hr * 0.6f
                headXY[1] = bodyCy - bh * 0.1f + tuck * bh * 0.25f
            }
        }
        return headXY
    }

    private fun leg(c: Canvas, x: Float, top: Float, len: Float, angle: Float, tone: Tone) {
        c.save()
        c.rotate(angle * 0.7f, x, top)
        kit.roundRect(c, x - lw / 2f, top, x + lw / 2f, top + len, lw * 0.45f, kit.shaded(tone, top, top + len))
        kit.ovalC(c, x + 2f, top + len - 2.5f, lw * 0.62f, lw * 0.32f, kit.solid(tone.dark))
        c.restore()
    }

    private fun tail(c: Canvas, x: Float, y: Float) {
        val size = 66f * look.bodyLength * (if (croc) 0.95f else 1f) * (if (shell) 0.25f else 1f)
        if (look.has(CURLED_TAIL)) {
            val st = kit.stroke
            st.strokeWidth = bh * 0.32f; st.color = base.base; st.shader = kit.vgrad(base.light, base.dark, y - 20f, y + 30f)
            val p = kit.path
            p.rewind()
            p.moveTo(x + 6f, y)
            p.quadTo(x - size * 0.4f, y, x - size * 0.5f, y + size * 0.15f)
            c.drawPath(p, st)
            st.strokeWidth = bh * 0.2f
            kit.rect.set(x - size * 0.72f, y + size * 0.02f, x - size * 0.38f, y + size * 0.36f)
            c.drawArc(kit.rect, 0f, 300f, false, st)
            st.shader = null
            return
        }
        Tails.draw(kit, c, Tail.THICK, x, y, size, tailSway, base, accent)
        val tx = x - size * 0.95f
        val ty = y + size * 0.12f + tailSway * size * 0.1f
        if (look.has(TAIL_SPIKES)) {
            val p = kit.path
            p.rewind()
            for (i in 0..1) {
                val sx = tx + size * (0.12f + i * 0.16f)
                p.moveTo(sx - 4f, ty - 2f)
                p.lineTo(sx - 12f, ty - 18f)
                p.lineTo(sx + 3f, ty - 3f)
                p.close()
            }
            c.drawPath(p, kit.solid(horn))
        }
        if (look.has(CLUB_TAIL)) {
            kit.ovalShaded(c, tx + 4f, ty, 11f, 8.5f, accent)
        }
    }

    private fun plates(c: Canvas, top: Float) {
        val p = kit.path
        val paint = kit.shaded(accent, top - 26f, top + 6f)
        for (i in 0 until 6) {
            val f = i / 5f
            val x = bodyCx - bw * 0.42f + f * bw * 0.72f
            val h = 13f + 12f * sin(f * Math.PI.toFloat()) + (if (i % 2 == 0) 0f else -3f)
            val y = top + 6f + 8f * abs(f - 0.45f)
            p.rewind()
            p.moveTo(x - 7f, y + 2f)
            p.quadTo(x - 8f, y - h * 0.7f, x, y - h)
            p.quadTo(x + 8f, y - h * 0.7f, x + 7f, y + 2f)
            p.close()
            c.drawPath(p, paint)
        }
    }

    private fun armor(c: Canvas, cy: Float) {
        val knob = kit.solid(accent.base)
        for (row in 0 until 3) {
            for (i in 0 until 6) {
                val x = bodyCx - bw * 0.4f + i * bw * 0.16f + (row % 2) * bw * 0.08f
                val y = cy - bh * 0.38f + row * bh * 0.22f
                c.drawCircle(x, y, 3.8f - row * 0.6f, knob)
            }
        }
    }

    private fun scutes(c: Canvas, top: Float) {
        val p = kit.solid(base.dark)
        for (i in 0 until 7) {
            val x = bodyCx - bw * 0.42f + i * bw * 0.13f
            kit.roundRect(c, x - 4f, top + 2f, x + 4f, top + 9f, 3f, p)
        }
    }

    private fun shellDome(c: Canvas, cy: Float) {
        val l = bodyCx - bw * 0.55f
        val r = bodyCx + bw * 0.52f
        val bottom = cy + bh * 0.42f
        val top = cy - bh * 1.05f
        val p = kit.path
        p.rewind()
        p.moveTo(l, bottom)
        p.cubicTo(l + bw * 0.05f, top, r - bw * 0.05f, top, r, bottom)
        p.close()
        c.drawPath(p, kit.shaded(accent, top, bottom))
        kit.roundRect(c, l - 2f, bottom - 6f, r + 2f, bottom + 3f, 4f, kit.solid(belly.base))
        val st = kit.stroke
        st.strokeWidth = 1.8f; st.color = accent.deep; st.shader = null
        val midY = (top + bottom) / 2f + 4f
        c.drawLine(bodyCx - bw * 0.22f, midY, bodyCx + bw * 0.2f, midY, st)
        for (i in -1..1) {
            val x = bodyCx + i * bw * 0.22f
            c.drawLine(x - 2f, midY, x - 6f, bottom - 6f, st)
            c.drawLine(x, midY, x + 2f, top + bh * 0.5f, st)
        }
        kit.sheen(c, bodyCx, midY, bw * 0.52f, (bottom - top) * 0.55f)
    }

    private fun neck(c: Canvas, x0: Float, y0: Float, x1: Float, y1: Float) {
        val st = kit.stroke
        st.strokeWidth = hr * 1.05f
        st.color = base.base
        st.shader = kit.vgrad(base.light, base.dark, y1, y0 + 20f)
        val p = kit.path
        p.rewind()
        p.moveTo(x0, y0)
        p.quadTo(x1 - 4f, y0 - 10f, x1, y1)
        c.drawPath(p, st)
        st.shader = null
    }

    private fun head(c: Canvas, cx: Float, cy: Float) {
        if (look.has(FRILL_HORNS)) frill(c, cx - hr * 0.55f, cy - hr * 0.35f)
        if (croc) {
            crocHead(c, cx, cy)
            return
        }
        if (look.has(CREST)) casque(c, cx, cy)
        val l = cx - hr * 0.85f
        val r = cx + hr * 1.05f * look.snout
        kit.roundRect(c, l, cy - hr * 0.72f, r, cy + hr * 0.6f, hr * 0.6f, kit.shaded(base, cy - hr, cy + hr))
        kit.sheen(c, cx, cy - hr * 0.1f, hr, hr * 0.66f)
        if (look.has(FRILL_HORNS)) {
            kit.roundRect(c, r - hr * 0.5f, cy - hr * 0.1f, r + hr * 0.12f, cy + hr * 0.45f, hr * 0.25f, kit.solid(base.deep))
            hornSpike(c, cx + hr * 0.05f, cy - hr * 0.55f, hr * 1.25f, -38f)
            hornSpike(c, r - hr * 0.3f, cy - hr * 0.48f, hr * 0.45f, -60f)
        }
        smile(c, r - hr * 0.2f, cy + hr * 0.22f)
        val ex = cx + hr * 0.15f
        val ey = cy - hr * 0.2f
        if (look.has(EYE_TURRET)) {
            kit.ovalShaded(c, ex, ey, hr * 0.38f, hr * 0.36f, base)
            if (eyeMode == 0 && blink < 0.85f) {
                c.drawCircle(ex + hr * 0.12f, ey - hr * 0.02f, hr * 0.12f, kit.solid(look.palette.eye))
                c.drawCircle(ex + hr * 0.15f, ey - hr * 0.06f, hr * 0.045f, kit.solid(0xFFFFFFFF.toInt()))
                return
            }
        }
        kit.eye(c, ex, ey, hr * 0.15f, blink, eyeMode, look.palette.eye)
        kit.blush(c, ex + hr * 0.05f, cy + hr * 0.2f, hr * 0.16f)
    }

    private fun crocHead(c: Canvas, cx: Float, cy: Float) {
        val l = cx - hr * 0.7f
        val r = cx + hr * 2.3f
        kit.roundRect(c, l, cy - hr * 0.4f, r, cy + hr * 0.42f, hr * 0.32f, kit.shaded(base, cy - hr * 0.4f, cy + hr * 0.42f))
        kit.roundRect(c, l + hr * 0.2f, cy + hr * 0.05f, r - hr * 0.05f, cy + hr * 0.42f, hr * 0.2f, kit.solid(belly.base))
        val p = kit.path
        p.rewind()
        var x = r - hr * 0.25f
        while (x > cx + hr * 0.1f) {
            p.moveTo(x - 3f, cy + hr * 0.04f)
            p.lineTo(x, cy + hr * 0.2f)
            p.lineTo(x + 3f, cy + hr * 0.04f)
            p.close()
            x -= hr * 0.36f
        }
        c.drawPath(p, kit.solid(horn))
        kit.stroke.strokeWidth = hr * 0.06f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawLine(l + hr * 0.3f, cy + hr * 0.04f, r - hr * 0.1f, cy + hr * 0.04f, kit.stroke)
        c.drawCircle(r - hr * 0.2f, cy - hr * 0.3f, hr * 0.06f, kit.solid(base.deep))
        kit.ovalShaded(c, cx - hr * 0.05f, cy - hr * 0.42f, hr * 0.36f, hr * 0.3f, base)
        kit.eye(c, cx, cy - hr * 0.48f, hr * 0.15f, blink, eyeMode, look.palette.eye)
        kit.sheen(c, cx + hr * 0.8f, cy - hr * 0.1f, hr * 1.4f, hr * 0.4f)
    }

    private fun frill(c: Canvas, cx: Float, cy: Float) {
        val r = hr * 1.2f
        val paint = kit.shaded(accent, cy - r, cy + r)
        for (i in 0 until 7) {
            val a = (200f + i * 22f) * (Math.PI.toFloat() / 180f)
            c.drawCircle(cx + cos(a) * r, cy + sin(a) * r, hr * 0.2f, paint)
        }
        c.drawCircle(cx, cy, r, paint)
        c.drawCircle(cx + r * 0.1f, cy - r * 0.05f, r * 0.55f, kit.solid(accent.light))
    }

    private fun hornSpike(c: Canvas, x: Float, y: Float, len: Float, angle: Float) {
        c.save()
        c.rotate(angle, x, y)
        val p = kit.path
        p.rewind()
        p.moveTo(x - len * 0.12f, y)
        p.quadTo(x + len * 0.5f, y - len * 0.08f, x + len, y - len * 0.02f)
        p.quadTo(x + len * 0.5f, y + len * 0.12f, x + len * 0.12f, y + len * 0.14f)
        p.close()
        c.drawPath(p, kit.solid(horn))
        c.restore()
    }

    private fun casque(c: Canvas, cx: Float, cy: Float) {
        val p = kit.path
        p.rewind()
        p.moveTo(cx - hr * 0.8f, cy - hr * 0.2f)
        p.quadTo(cx - hr * 0.6f, cy - hr * 1.3f, cx + hr * 0.3f, cy - hr * 0.55f)
        p.close()
        c.drawPath(p, kit.solid(accent.base))
    }

    private fun smile(c: Canvas, x: Float, y: Float) {
        val p = kit.path
        p.rewind()
        p.moveTo(x, y)
        p.quadTo(x - hr * 0.25f, y + hr * 0.18f, x - hr * 0.55f, y + hr * 0.05f)
        kit.stroke.strokeWidth = hr * 0.06f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
    }
}
