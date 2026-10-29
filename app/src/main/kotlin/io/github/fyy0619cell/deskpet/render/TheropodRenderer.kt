package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Path
import io.github.fyy0619cell.deskpet.render.Feature.ARMS_TINY
import io.github.fyy0619cell.deskpet.render.Feature.BACK_SPIKES
import io.github.fyy0619cell.deskpet.render.Feature.FEATHERS
import io.github.fyy0619cell.deskpet.render.Feature.TEETH

class TheropodRenderer(look: Look) : PetRenderer(look) {
    private val bw = 66f * look.bodyLength
    private val bh = 50f * look.bodyHeight
    private val hr = 29f * look.headSize
    private val legLen = 30f * look.legLength
    private val bodyCx = 94f
    private val far = Tone(base.dark)
    private val bodyPath = Path()
    private val stripes = Patterns.stripeOffsets(look.patternSeed, 5)

    override val shadowHalfWidth = bw * 0.75f + 18f
    override val shadowCenterX = bodyCx + 6f

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        val standing = 1f - tuck * 0.9f
        val bodyBottom = GROUND - legLen * standing - 6f * standing + 2f
        val bodyCy = bodyBottom - bh / 2f - breathe * 0.5f
        val hipX = bodyCx - bw * 0.08f
        val hipY = bodyCy + bh * 0.1f
        val headCx = bodyCx + bw * 0.46f + hr * 0.5f
        val headCy = bodyCy - bh * 0.62f - hr * 0.3f + tuck * hr * 1.1f

        val tailX = bodyCx - bw * 0.38f
        val tailY = bodyCy - bh * 0.02f
        val tailSize = 72f * look.bodyLength
        Tails.draw(kit, c, Tail.THICK, tailX, tailY, tailSize, tailSway, base, accent)
        if (look.has(FEATHERS)) {
            feathers(c, tailX - tailSize * 0.9f, tailY + tailSize * (0.12f + tailSway * 0.1f), 4, 175f)
        }
        if (standing > 0.3f) hindLeg(c, hipX + 8f, hipY, -legSwing, far)

        c.save()
        c.rotate(-14f + tuck * 10f, bodyCx, bodyCy)
        bodyPath.rewind()
        kit.rect.set(bodyCx - bw / 2f, bodyCy - bh / 2f, bodyCx + bw / 2f, bodyCy + bh / 2f)
        bodyPath.addOval(kit.rect, Path.Direction.CW)
        if (look.has(BACK_SPIKES)) spikes(c, bodyCx, bodyCy - bh / 2f)
        c.drawPath(bodyPath, kit.shaded(base, bodyCy - bh / 2f, bodyCy + bh / 2f))
        c.save()
        c.clipPath(bodyPath)
        kit.ovalC(c, bodyCx + bw * 0.18f, bodyCy + bh * 0.42f, bw * 0.42f, bh * 0.32f, kit.solid(belly.base))
        if (look.pattern == Pattern.STRIPES) for (i in stripes.indices) {
            Patterns.stripe(kit, c, bodyCx - bw * 0.36f + i * bw * 0.17f, bodyCy - bh / 2f - 2f, bh * (0.42f + 0.12f * stripes[i]), 3f, accent.base)
        }
        c.restore()
        kit.sheen(c, bodyCx, bodyCy, bw / 2f, bh / 2f)
        c.restore()

        // neck
        kit.ovalC(c, bodyCx + bw * 0.38f, bodyCy - bh * 0.32f + tuck * 10f, hr * 0.52f, hr * 0.7f, kit.shaded(base, bodyCy - bh, bodyCy))
        if (standing > 0.3f) hindLeg(c, hipX, hipY, legSwing, base)
        else kit.ovalC(c, hipX + 14f, GROUND - 5f, 14f, 6f, kit.solid(base.dark))
        if (look.has(ARMS_TINY)) arm(c, bodyCx + bw * 0.4f, bodyCy + bh * 0.08f + tuck * 6f)

        c.save()
        c.rotate(headTilt, headCx - hr * 0.6f, headCy + hr * 0.5f)
        head(c, headCx, headCy)
        c.restore()
    }

    private fun hindLeg(c: Canvas, x: Float, y: Float, angle: Float, tone: Tone) {
        c.save()
        c.rotate(angle * 0.8f, x, y)
        kit.ovalC(c, x, y, bh * 0.3f, bh * 0.42f, kit.shaded(tone, y - bh * 0.42f, y + bh * 0.42f))
        val kneeY = y + bh * 0.3f
        val shinW = 11f
        kit.roundRect(c, x - shinW / 2f, kneeY, x + shinW / 2f, GROUND - 3f, shinW / 2f, kit.shaded(tone, kneeY, GROUND))
        kit.ovalC(c, x + 6f, GROUND - 3.5f, 11f, 4.5f, kit.solid(tone.dark))
        c.restore()
    }

    private fun arm(c: Canvas, x: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = 5.5f; st.color = base.dark; st.shader = null
        val p = kit.path
        p.rewind()
        p.moveTo(x, y)
        p.quadTo(x + 8f, y + 2f + legSwing * 0.1f, x + 10f, y + 9f)
        c.drawPath(p, st)
        if (look.has(FEATHERS)) feathers(c, x + 2f, y + 2f, 3, 110f)
    }

    private fun head(c: Canvas, cx: Float, cy: Float) {
        if (look.has(FEATHERS)) feathers(c, cx - hr * 0.7f, cy - hr * 0.55f, 3, 200f)
        val l = cx - hr * 0.9f
        val r = cx + hr * 1.15f * look.snout
        kit.roundRect(c, l, cy - hr * 0.78f, r, cy + hr * 0.62f, hr * 0.62f, kit.shaded(base, cy - hr, cy + hr))
        // lower jaw, a lighter band under the mouth line
        kit.roundRect(c, l + hr * 0.3f, cy + hr * 0.12f, r - hr * 0.08f, cy + hr * 0.62f, hr * 0.32f, kit.solid(belly.base))
        val st = kit.stroke
        st.strokeWidth = hr * 0.05f; st.color = base.deep; st.shader = null
        val p = kit.path
        p.rewind()
        p.moveTo(r - hr * 0.1f, cy + hr * 0.14f)
        p.quadTo(cx + hr * 0.3f, cy + hr * 0.24f, cx - hr * 0.25f, cy + hr * 0.12f)
        c.drawPath(p, st)
        if (look.has(TEETH)) {
            val tooth = kit.solid(0xFFFBF6EC.toInt())
            for (i in 0 until 2) {
                val tx = r - hr * (0.32f + i * 0.34f)
                p.rewind()
                p.moveTo(tx - hr * 0.07f, cy + hr * 0.16f)
                p.lineTo(tx, cy + hr * 0.32f)
                p.lineTo(tx + hr * 0.07f, cy + hr * 0.16f)
                p.close()
                c.drawPath(p, tooth)
            }
        }
        c.drawCircle(r - hr * 0.2f, cy - hr * 0.28f, hr * 0.05f, kit.solid(base.deep))
        kit.sheen(c, cx, cy - hr * 0.1f, hr * 1.05f, hr * 0.7f)
        kit.eye(c, cx + hr * 0.12f, cy - hr * 0.3f, hr * 0.12f, blink, eyeMode, look.palette.eye)
        kit.blush(c, cx + hr * 0.02f, cy - hr * 0.02f, hr * 0.15f)
    }

    private fun spikes(c: Canvas, cx: Float, top: Float) {
        val p = kit.path
        val paint = kit.solid(accent.base)
        for (i in -2..2) {
            val x = cx + i * bw * 0.16f
            val h = 9f - kotlin.math.abs(i) * 1.5f
            p.rewind()
            p.moveTo(x - 5f, top + 4f)
            p.quadTo(x, top - h * 1.2f, x + 5f, top + 4f)
            p.close()
            c.drawPath(p, paint)
        }
    }

    private fun feathers(c: Canvas, x: Float, y: Float, n: Int, dirDeg: Float) {
        val paint = kit.solid(accent.base)
        for (i in 0 until n) {
            c.save()
            c.rotate(dirDeg + (i - (n - 1) / 2f) * 22f + tailSway * 4f, x, y)
            kit.oval(c, x, y - 3f, x + 15f, y + 3f, paint)
            c.restore()
        }
    }
}
