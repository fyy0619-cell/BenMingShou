package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import io.github.fyy0619cell.deskpet.render.Feature.BIG_HIND_LEGS
import io.github.fyy0619cell.deskpet.render.Feature.GILLS
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** Frogs (sit and hop) and axolotls/salamanders (low walkers with a finned tail). */
class AmphibianRenderer(look: Look) : PetRenderer(look) {
    private val frog = look.has(BIG_HIND_LEGS)
    private val s = look.bodyLength
    private val far = Tone(ColorX.darker(look.palette.base, 0.09f))
    private val gillColor = ColorX.withAlpha(look.palette.accent, 235)

    override val shadowHalfWidth = 52f * s
    override val shadowCenterX = 100f

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        if (frog) frog(c, pose) else axolotl(c, pose)
    }

    private fun frog(c: Canvas, pose: Pose) {
        val hopping = pose.action == Action.WALK
        val arc = if (hopping) abs(sin(pose.walkPhase * PI.toFloat())) else 0f
        val lift = arc * 34f
        val stretch = arc
        c.save()
        c.translate(0f, -lift)
        val cx = 98f
        val cy = GROUND - 30f * look.bodyHeight + tuck * 6f - breathe * 0.6f
        val rx = 36f * s
        val ry = 26f * look.bodyHeight

        // far hind leg
        hindLeg(c, cx - rx * 0.35f + 6f, cy + ry * 0.3f, stretch, far)
        kit.ovalShaded(c, cx, cy, rx, ry, base)
        kit.ovalC(c, cx + rx * 0.2f, cy + ry * 0.5f, rx * 0.65f, ry * 0.42f, kit.solid(belly.base))
        if (look.pattern == Pattern.SPOTS) {
            val dot = kit.solid(accent.base)
            c.drawCircle(cx - rx * 0.4f, cy - ry * 0.4f, 4f, dot)
            c.drawCircle(cx - rx * 0.05f, cy - ry * 0.65f, 3.2f, dot)
        }
        kit.sheen(c, cx, cy, rx, ry)
        hindLeg(c, cx - rx * 0.35f, cy + ry * 0.3f, stretch, base)
        // front leg
        kit.line(c, cx + rx * 0.55f, cy + ry * 0.3f, cx + rx * 0.62f + stretch * 8f, GROUND - 3f + (if (hopping) -stretch * 6f else 0f), 7f, base.base)
        kit.ovalC(c, cx + rx * 0.7f + stretch * 8f, GROUND - 3f, 6f, 2.6f, kit.solid(base.dark))
        // eye bulges on top
        val ex = cx + rx * 0.45f
        val ey = cy - ry * 0.85f
        c.drawCircle(ex - 13f, ey + 2f, 9f, kit.solid(base.dark))
        c.drawCircle(ex, ey, 11f, kit.shaded(base, ey - 11f, ey + 11f))
        if (eyeMode == 0 && blink < 0.85f) c.drawCircle(ex + 1f, ey, 7.5f, kit.solid(Color.WHITE))
        kit.eye(c, ex + 2f, ey, 5.2f, blink, eyeMode, look.palette.eye)
        val p = kit.path
        p.rewind()
        p.moveTo(cx + rx * 0.95f, cy - ry * 0.05f)
        p.quadTo(cx + rx * 0.6f, cy + ry * 0.35f, cx + rx * 0.15f, cy + ry * 0.05f)
        kit.stroke.strokeWidth = 2.2f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
        kit.blush(c, cx + rx * 0.6f, cy + ry * 0.15f, 5f)
        c.restore()
    }

    private fun hindLeg(c: Canvas, x: Float, y: Float, stretch: Float, tone: Tone) {
        c.save()
        c.rotate(-stretch * 40f, x, y)
        kit.ovalShaded(c, x, y, 18f, 13f, tone)
        val footX = x + 10f - stretch * 28f
        val footY = GROUND - 3f + stretch * 10f
        kit.line(c, x + 4f, y + 8f, footX, footY - 2f, 7f, tone.base)
        kit.ovalC(c, footX + 6f, footY, 11f, 3.4f, kit.solid(tone.dark))
        c.restore()
    }

    private fun axolotl(c: Canvas, pose: Pose) {
        val cx = 92f
        val cy = GROUND - 20f * look.legLength - 14f * look.bodyHeight + tuck * 8f - breathe * 0.4f
        val bw = 80f * s
        val bh = 28f * look.bodyHeight
        val wave = if (pose.action == Action.WALK) sin(pose.walkPhase * TAU) else sin(t * TAU / 3f) * 0.4f
        // finned tail
        val p = kit.path
        p.rewind()
        val tx = cx - bw * 0.4f
        p.moveTo(tx, cy - bh * 0.35f)
        p.cubicTo(tx - 24f, cy - bh * 0.9f, tx - 46f, cy - bh * 0.3f + wave * 6f, tx - 58f, cy + wave * 8f)
        p.cubicTo(tx - 40f, cy + bh * 0.4f + wave * 4f, tx - 20f, cy + bh * 0.6f, tx, cy + bh * 0.35f)
        p.close()
        c.drawPath(p, kit.shaded(base, cy - bh, cy + bh))
        legPair(c, cx - bw * 0.25f, cy + bh * 0.3f, -legSwing, far)
        legPair(c, cx + bw * 0.25f, cy + bh * 0.3f, legSwing, far)
        kit.ovalShaded(c, cx, cy, bw / 2f, bh / 2f, base)
        kit.sheen(c, cx, cy, bw / 2f, bh / 2f)
        legPair(c, cx - bw * 0.25f + 6f, cy + bh * 0.35f, legSwing, base)
        legPair(c, cx + bw * 0.25f + 6f, cy + bh * 0.35f, -legSwing, base)
        // head with gills
        val hx = cx + bw * 0.5f + 8f
        val hy = cy - bh * 0.25f + tuck * 6f
        if (look.has(GILLS)) gills(c, hx - 12f, hy - 4f)
        kit.ovalShaded(c, hx, hy, 24f * look.headSize, 18f * look.headSize, base)
        kit.sheen(c, hx, hy, 24f, 18f)
        kit.eye(c, hx + 8f, hy - 5f, 3.6f, blink, eyeMode, look.palette.eye)
        val m = kit.path
        m.rewind()
        m.moveTo(hx + 21f, hy + 5f)
        m.quadTo(hx + 12f, hy + 12f, hx + 2f, hy + 7f)
        kit.stroke.strokeWidth = 2f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawPath(m, kit.stroke)
        kit.blush(c, hx + 8f, hy + 6f, 4f)
    }

    private fun legPair(c: Canvas, x: Float, y: Float, swing: Float, tone: Tone) {
        c.save()
        c.rotate(swing * 0.6f, x, y)
        kit.roundRect(c, x - 4f, y, x + 4f, GROUND - 2f, 4f, kit.solid(tone.base))
        kit.ovalC(c, x + 3f, GROUND - 3f, 6f, 3f, kit.solid(tone.dark))
        c.restore()
    }

    private fun gills(c: Canvas, x: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = 5f; st.color = gillColor; st.shader = null
        for (i in 0 until 3) {
            val a = (-150f + i * 32f) * (PI.toFloat() / 180f)
            val sway = sin(t * 2.5f + i) * 2f
            val ex = x + kotlin.math.cos(a) * 24f
            val ey = y + sin(a) * 24f + sway
            c.drawLine(x, y, ex, ey, st)
            c.drawCircle(ex, ey, 4f, kit.solid(gillColor))
        }
    }
}
