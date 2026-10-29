package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import io.github.fyy0619cell.deskpet.render.Feature.ANTLERS
import io.github.fyy0619cell.deskpet.render.Feature.HORNS
import io.github.fyy0619cell.deskpet.render.Feature.LONG_NECK
import io.github.fyy0619cell.deskpet.render.Feature.MANE
import io.github.fyy0619cell.deskpet.render.Feature.MASK
import io.github.fyy0619cell.deskpet.render.Feature.PIG_SNOUT
import io.github.fyy0619cell.deskpet.render.Feature.POUCH_STRIPES
import io.github.fyy0619cell.deskpet.render.Feature.SHAGGY_FUR
import io.github.fyy0619cell.deskpet.render.Feature.TAIL_RINGS
import io.github.fyy0619cell.deskpet.render.Feature.TEETH
import io.github.fyy0619cell.deskpet.render.Feature.TRUNK
import io.github.fyy0619cell.deskpet.render.Feature.TUSKS
import kotlin.math.cos
import kotlin.math.sin

class QuadrupedRenderer(look: Look) : PetRenderer(look) {
    private val bw = 84f * look.bodyLength
    private val bh = 50f * look.bodyHeight
    private val legLen = 24f * look.legLength
    private val hr = 31f * look.headSize
    private val lw = 12.5f * (0.8f + 0.2f * look.bodyHeight)
    private val longNeck = look.has(LONG_NECK)
    private val neckLen = if (longNeck) 52f else 0f
    private val bodyCx = 90f - (bw - 84f) * 0.25f - (if (longNeck) 8f else 0f)
    private val panda = look.pattern == Pattern.PANDA
    private val limbTone = if (panda) accent else base
    private val farLimb = Tone(limbTone.dark)
    private val bodyPath = Path()
    private val headPath = Path()
    private val stripes = Patterns.stripeOffsets(look.patternSeed, 6)
    private val spots = Patterns.spotOffsets(look.patternSeed, 12)
    private val earInner = ColorX.mix(look.palette.belly, 0xFFF0A8B8.toInt(), 0.45f)
    private val frontEar = if (panda) accent else base
    private val backEar = Tone(frontEar.dark)
    private val snoutTone = Tone(ColorX.darker(look.palette.base, 0.04f))
    private val hornColor = 0xFFEADCC4.toInt()
    private val antlerColor = 0xFFB98F63.toInt()
    private val noseR = hr * (0.1f + (0.85f - look.snout).coerceAtLeast(0f) * 0.55f)

    override val shadowHalfWidth = bw * 0.62f + 10f
    override val shadowCenterX = bodyCx + 10f

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        val standing = 1f - tuck * 0.88f
        val bodyBottom = GROUND - legLen * standing + 3f + tuck * 3f
        val bodyCy = bodyBottom - bh / 2f - breathe * 0.5f
        val neckDrop = tuck * (hr * 0.55f + neckLen * 0.8f)
        val headCx = bodyCx + bw * 0.42f + hr * 0.32f + (if (longNeck) 10f + tuck * 12f else 0f)
        val headCy = bodyCy - bh * 0.36f - hr * 0.22f - neckLen + neckDrop
        val legTop = bodyCy + bh * 0.12f
        val legH = GROUND - legTop
        val backX = bodyCx - bw * 0.3f
        val frontX = bodyCx + bw * 0.27f

        Tails.draw(
            kit, c, look.tail, bodyCx - bw * 0.46f, bodyCy - bh * 0.12f, if (longNeck) 52f else 70f, tailSway, base, accent,
            curl = if (longNeck) maxOf(curl, 0.75f) else curl, groundY = if (longNeck) minOf(bodyCy + bh * 0.9f, GROUND - 3f) else GROUND - 3f,
            bands = look.has(TAIL_RINGS) || (look.pattern == Pattern.STRIPES && !look.has(POUCH_STRIPES)),
        )
        if (standing > 0.3f) {
            leg(c, backX + 7f, legTop, legH, -legSwing, farLimb)
            leg(c, frontX + 7f, legTop, legH, legSwing, farLimb)
            leg(c, backX, legTop, legH, legSwing, limbTone)
            leg(c, frontX, legTop, legH, -legSwing, limbTone)
        }

        bodyPath.rewind()
        kit.rect.set(bodyCx - bw / 2f, bodyCy - bh / 2f, bodyCx + bw / 2f, bodyCy + bh / 2f)
        bodyPath.addOval(kit.rect, Path.Direction.CW)
        c.drawPath(bodyPath, kit.shaded(base, bodyCy - bh / 2f, bodyCy + bh / 2f))
        c.save()
        c.clipPath(bodyPath)
        kit.ovalC(c, bodyCx + bw * 0.08f, bodyCy + bh * 0.42f, bw * 0.4f, bh * 0.3f, kit.solid(belly.base))
        bodyPattern(c, bodyCy)
        c.restore()
        kit.sheen(c, bodyCx, bodyCy, bw / 2f, bh / 2f)
        if (look.has(SHAGGY_FUR)) fringe(c, bodyCx - bw * 0.47f, bodyCx + bw * 0.42f, bodyCy + bh * 0.28f, 6.5f)
        if (standing <= 0.3f) {
            kit.ovalC(c, frontX + 10f, GROUND - 4f, lw * 0.9f, lw * 0.45f, kit.solid(limbTone.base))
        }
        if (longNeck) neck(c, bodyCx + bw * 0.3f, bodyCy - bh * 0.15f, headCx - hr * 0.35f, headCy + hr * 0.35f)

        c.save()
        c.rotate(headTilt, headCx - hr * 0.5f, headCy + hr * 0.6f)
        head(c, headCx, headCy)
        c.restore()
    }

    private fun neck(c: Canvas, x0: Float, y0: Float, x1: Float, y1: Float) {
        val st = kit.stroke
        st.strokeWidth = hr * 0.72f
        st.color = base.base
        st.shader = kit.vgrad(base.light, base.dark, y1, y0 + 20f)
        val p = kit.path
        p.rewind()
        p.moveTo(x0, y0)
        p.quadTo(x1 - 6f, (y0 + y1) / 2f, x1, y1)
        c.drawPath(p, st)
        st.shader = null
        if (look.pattern == Pattern.PATCHES) {
            val dot = kit.solid(accent.base)
            for (i in 1..3) {
                val f = i / 4f
                c.drawCircle(x0 + (x1 - x0) * f - 2f, y0 + (y1 - y0) * f, hr * 0.15f, dot)
            }
        }
    }

    private fun leg(c: Canvas, x: Float, top: Float, len: Float, angle: Float, tone: Tone) {
        c.save()
        c.rotate(angle, x, top)
        kit.roundRect(c, x - lw / 2f, top, x + lw / 2f, top + len, lw / 2f, kit.shaded(tone, top, top + len))
        val paw = if (panda) tone.deep else belly.base
        kit.ovalC(c, x + 1.5f, top + len - 2.5f, lw * 0.6f, lw * 0.34f, kit.solid(paw))
        c.restore()
    }

    private fun head(c: Canvas, cx: Float, cy: Float) {
        if (look.has(MANE)) mane(c, cx - hr * 0.1f, cy)
        if (look.has(ANTLERS)) antlers(c, cx - hr * 0.25f, cy - hr * 0.7f)
        if (look.has(HORNS)) {
            if (longNeck) ossicones(c, cx - hr * 0.15f, cy - hr * 0.7f) else horns(c, cx - hr * 0.2f, cy - hr * 0.72f)
        }
        Ears.draw(kit, c, look.ear, cx - hr * 0.42f, cy - hr * 0.66f, hr, -18f, backEar, earInner)
        if (look.ear == Ear.BIG_FAN) Ears.draw(kit, c, look.ear, cx - hr * 0.15f, cy - hr * 0.35f, hr, 0f, base, earInner)

        headPath.rewind()
        kit.rect.set(cx - hr * 1.02f, cy - hr * 0.92f, cx + hr * 1.02f, cy + hr * 0.92f)
        headPath.addOval(kit.rect, Path.Direction.CW)
        c.drawPath(headPath, kit.shaded(base, cy - hr, cy + hr))
        c.save()
        c.clipPath(headPath)
        headPattern(c, cx, cy)
        c.restore()
        kit.sheen(c, cx, cy, hr, hr * 0.92f)
        if (look.has(SHAGGY_FUR)) hairTuft(c, cx - hr * 0.1f, cy - hr * 0.86f)
        if (look.ear != Ear.BIG_FAN) {
            Ears.draw(kit, c, look.ear, cx + hr * 0.08f, cy - hr * 0.78f, hr, 12f, frontEar, earInner)
        }

        val eyeX = cx + hr * 0.3f
        val eyeY = cy - hr * 0.1f
        when {
            look.has(TRUNK) -> trunk(c, cx, cy)
            look.has(PIG_SNOUT) -> pigSnout(c, cx, cy)
            else -> muzzle(c, cx, cy)
        }
        if (look.has(TUSKS)) tusk(c, cx, cy)
        val ringed = panda || look.has(MASK)
        if (panda) {
            c.save(); c.rotate(-20f, eyeX, eyeY)
            kit.ovalC(c, eyeX - hr * 0.02f, eyeY + hr * 0.04f, hr * 0.24f, hr * 0.3f, kit.solid(accent.base))
            c.restore()
        }
        if (ringed && eyeMode == 0 && blink < 0.85f) c.drawCircle(eyeX, eyeY, hr * 0.15f, kit.solid(Color.WHITE))
        val eyeInk = if (ringed && eyeMode != 0) 0xFFF4F1EC.toInt() else look.palette.eye
        kit.eye(c, eyeX, eyeY, hr * 0.12f, blink, eyeMode, eyeInk)
        kit.blush(c, cx + hr * 0.2f, cy + hr * 0.36f, hr * 0.16f)
    }

    private fun muzzle(c: Canvas, cx: Float, cy: Float) {
        val s = look.snout
        val mx = cx + hr * (0.52f + 0.22f * (s - 1f))
        val my = cy + hr * 0.3f
        kit.ovalC(c, mx, my, hr * 0.4f * (0.75f + 0.25f * s), hr * 0.28f, kit.solid(belly.base))
        val nx = mx + hr * 0.28f * s
        val ny = my - hr * 0.1f
        kit.ovalC(c, nx, ny, noseR, noseR * 0.75f, kit.solid(dark))
        smile(c, nx, ny + noseR * 0.5f)
        if (look.has(TEETH)) {
            val p = kit.path
            p.rewind()
            p.moveTo(nx - hr * 0.13f, ny + hr * 0.15f)
            p.lineTo(nx - hr * 0.08f, ny + hr * 0.5f)
            p.lineTo(nx - hr * 0.02f, ny + hr * 0.14f)
            p.close()
            c.drawPath(p, kit.solid(0xFFFBF6EC.toInt()))
        }
    }

    private fun smile(c: Canvas, x: Float, y: Float) {
        val p = kit.path
        p.rewind()
        p.moveTo(x - hr * 0.02f, y)
        p.quadTo(x - hr * 0.04f, y + hr * 0.14f, x - hr * 0.16f, y + hr * 0.12f)
        kit.stroke.strokeWidth = hr * 0.045f; kit.stroke.color = dark; kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
    }

    private fun pigSnout(c: Canvas, cx: Float, cy: Float) {
        val sx = cx + hr * 0.86f
        val sy = cy + hr * 0.2f
        kit.ovalC(c, sx, sy, hr * 0.2f, hr * 0.27f, kit.shaded(snoutTone, sy - hr * 0.3f, sy + hr * 0.3f))
        kit.ovalC(c, sx + hr * 0.04f, sy - hr * 0.09f, hr * 0.04f, hr * 0.065f, kit.solid(base.deep))
        kit.ovalC(c, sx + hr * 0.04f, sy + hr * 0.09f, hr * 0.04f, hr * 0.065f, kit.solid(base.deep))
        smile(c, sx - hr * 0.2f, sy + hr * 0.26f)
    }

    private fun trunk(c: Canvas, cx: Float, cy: Float) {
        val p = kit.path
        val sx = cx + hr * 0.7f
        val sy = cy + hr * 0.05f
        p.rewind()
        p.moveTo(sx, sy)
        p.cubicTo(sx + hr * 0.45f, sy + hr * 0.2f, sx + hr * 0.4f + tailSway * 3f, sy + hr * 0.9f, sx + hr * 0.62f + tailSway * 4f, sy + hr * 1.25f)
        kit.stroke.strokeWidth = hr * 0.3f
        kit.stroke.color = base.base
        kit.stroke.shader = kit.vgrad(base.light, base.dark, sy, sy + hr * 1.3f)
        c.drawPath(p, kit.stroke)
        kit.stroke.shader = null
    }

    private fun tusk(c: Canvas, cx: Float, cy: Float) {
        val p = kit.path
        val sx = cx + hr * 0.62f
        val sy = cy + hr * 0.42f
        p.rewind()
        p.moveTo(sx, sy)
        p.quadTo(sx + hr * 0.75f, sy + hr * 0.55f, sx + hr * 0.95f, sy - hr * 0.12f)
        kit.stroke.strokeWidth = hr * 0.13f; kit.stroke.color = 0xFFF4ECDA.toInt(); kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
    }

    private fun horns(c: Canvas, x: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = hr * 0.16f; st.color = hornColor; st.shader = null
        val p = kit.path
        p.rewind()
        for (i in 0..1) {
            val ox = x + i * hr * 0.42f
            p.moveTo(ox, y + hr * 0.15f)
            p.quadTo(ox - hr * 0.05f, y - hr * 0.35f, ox + hr * 0.25f, y - hr * 0.42f)
        }
        c.drawPath(p, st)
    }

    private fun ossicones(c: Canvas, x: Float, y: Float) {
        for (i in 0..1) {
            val ox = x + i * hr * 0.38f
            kit.line(c, ox, y + hr * 0.15f, ox + hr * 0.05f, y - hr * 0.42f, hr * 0.15f, base.dark)
            c.drawCircle(ox + hr * 0.05f, y - hr * 0.45f, hr * 0.11f, kit.solid(accent.deep))
        }
    }

    private fun antlers(c: Canvas, x: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = hr * 0.1f; st.color = antlerColor; st.shader = null
        val p = kit.path
        p.rewind()
        for (i in 0..1) {
            val ox = x + i * hr * 0.4f
            val k = 1f - i * 0.15f
            p.moveTo(ox, y + hr * 0.1f)
            p.quadTo(ox - hr * 0.2f * k, y - hr * 0.5f * k, ox - hr * 0.05f, y - hr * 1.05f * k)
            p.moveTo(ox - hr * 0.12f * k, y - hr * 0.35f * k)
            p.lineTo(ox + hr * 0.22f * k, y - hr * 0.6f * k)
            p.moveTo(ox - hr * 0.1f * k, y - hr * 0.72f * k)
            p.lineTo(ox - hr * 0.42f * k, y - hr * 0.9f * k)
        }
        c.drawPath(p, st)
    }

    private fun mane(c: Canvas, cx: Float, cy: Float) {
        val r = hr * 1.32f
        val p = kit.solid(accent.base)
        for (i in 0 until 12) {
            val a = i / 12f * TAU
            c.drawCircle(cx + cos(a) * r * 0.82f, cy + sin(a) * r * 0.82f, r * 0.3f, p)
        }
        c.drawCircle(cx, cy, r * 0.88f, p)
    }

    /** Hanging locks of long fur: rounded drops of uneven length. */
    private fun fringe(c: Canvas, x0: Float, x1: Float, y: Float, depth: Float) {
        val p = kit.path
        p.rewind()
        p.moveTo(x0, y - depth)
        var x = x0
        var i = 0
        val step = depth * 1.5f
        while (x < x1) {
            val len = depth * (1.1f + 0.5f * ((i * 7 + 3) % 5) / 4f)
            p.quadTo(x + step * 0.5f, y + len * 1.6f, x + step, y)
            x += step
            i++
        }
        p.lineTo(x, y - depth)
        p.close()
        c.drawPath(p, kit.shaded(base, y - depth, y + depth * 2f))
    }

    private fun hairTuft(c: Canvas, x: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = hr * 0.09f; st.color = base.dark; st.shader = null
        val p = kit.path
        p.rewind()
        for (i in -1..1) {
            p.moveTo(x + i * hr * 0.14f, y + hr * 0.1f)
            p.quadTo(x + i * hr * 0.2f - hr * 0.05f, y - hr * 0.2f, x + i * hr * 0.3f - hr * 0.12f, y - hr * 0.22f)
        }
        c.drawPath(p, st)
    }

    private fun bodyPattern(c: Canvas, cy: Float) {
        val top = cy - bh / 2f
        when (look.pattern) {
            Pattern.STRIPES -> {
                val rearOnly = look.has(POUCH_STRIPES)
                val x0 = bodyCx - bw * 0.42f
                val span = if (rearOnly) bw * 0.38f else bw * 0.8f
                for (i in stripes.indices) {
                    val x = x0 + span * (i + 0.5f + stripes[i] * 0.3f) / stripes.size
                    Patterns.stripe(kit, c, x, top - 2f, bh * (0.5f + 0.15f * stripes[i]), 3.2f, accent.base)
                }
            }
            Pattern.SPOTS -> for (i in 0 until spots.size / 2) {
                c.drawCircle(bodyCx + spots[2 * i] * bw * 0.45f, cy + spots[2 * i + 1] * bh * 0.3f - 4f, 3.2f + (i % 3) * 0.9f, kit.solid(accent.base))
            }
            Pattern.PATCHES -> {
                val p = kit.solid(accent.base)
                for (i in 0 until 7) {
                    val px = bodyCx - bw * 0.4f + (i % 4) * bw * 0.25f + spots[2 * i] * 4f
                    val py = cy - bh * 0.22f + (i / 4) * bh * 0.38f + spots[2 * i + 1] * 3f
                    kit.ovalC(c, px, py, bw * 0.09f, bh * 0.14f, p)
                }
            }
            Pattern.PANDA -> {
                c.save(); c.rotate(-8f, bodyCx, cy)
                kit.rect.set(bodyCx + bw * 0.06f, top - 5f, bodyCx + bw * 0.3f, cy + bh)
                c.drawRect(kit.rect, kit.solid(accent.base))
                c.restore()
            }
            Pattern.NONE, Pattern.BANDS -> Unit
        }
    }

    private fun headPattern(c: Canvas, cx: Float, cy: Float) {
        if (look.pattern == Pattern.STRIPES && !look.has(POUCH_STRIPES)) {
            for (i in 0 until 3) {
                Patterns.stripe(kit, c, cx - hr * 0.3f + i * hr * 0.24f, cy - hr * 0.98f, hr * (0.42f - i * 0.06f), 2.4f, accent.base)
            }
        }
        if (look.has(MASK)) {
            c.save(); c.rotate(-8f, cx, cy)
            kit.ovalC(c, cx + hr * 0.15f, cy - hr * 0.08f, hr * 0.85f, hr * 0.24f, kit.solid(accent.base))
            c.restore()
        }
    }
}
