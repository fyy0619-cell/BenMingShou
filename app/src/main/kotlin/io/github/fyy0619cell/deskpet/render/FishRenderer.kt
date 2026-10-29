package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import io.github.fyy0619cell.deskpet.render.Feature.BEAK_SNOUT
import io.github.fyy0619cell.deskpet.render.Feature.FLUKES
import io.github.fyy0619cell.deskpet.render.Feature.HEAD_ARMOR
import io.github.fyy0619cell.deskpet.render.Feature.LOBE_FINS
import io.github.fyy0619cell.deskpet.render.Feature.LONG_TAIL
import io.github.fyy0619cell.deskpet.render.Feature.LURE
import io.github.fyy0619cell.deskpet.render.Feature.ROUND_BODY
import io.github.fyy0619cell.deskpet.render.Feature.SPINES
import io.github.fyy0619cell.deskpet.render.Feature.TALL_FIN
import io.github.fyy0619cell.deskpet.render.Feature.TEETH
import kotlin.math.cos
import kotlin.math.sin

/** Fish and whales hover and swim in the air. */
class FishRenderer(look: Look) : PetRenderer(look) {
    private val round = look.has(ROUND_BODY)
    private val mammal = look.has(FLUKES)
    private val len = (if (round) 70f else 112f) * look.bodyLength
    private val ht = (if (round) 66f else 52f) * look.bodyHeight
    private val tailS = (if (look.has(LONG_TAIL)) 44f else 30f) * (if (round) 0.8f else 1f) * look.bodyHeight.coerceAtLeast(0.8f)
    private val tailExtent = 4f + tailS * when {
        mammal -> 1.3f
        look.has(LONG_TAIL) -> 1.35f
        else -> 0.95f
    }
    private val cx = 100f + (tailExtent - (if (look.has(BEAK_SNOUT)) 16f else 0f)) / 2f
    private val bodyPath = Path()
    private val finTone = Tone(look.palette.accent)
    private val bubble = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1.8f; color = Color.argb(150, 140, 190, 220)
    }
    private val spots = Patterns.spotOffsets(look.patternSeed, 8)

    override val shadowHalfWidth = len * 0.45f
    override val shadowCenterX = cx

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        val swimming = pose.action == Action.WALK
        val beat = if (swimming) sin(pose.walkPhase * TAU * 2f) else sin(t * TAU / 1.6f) * 0.5f
        val cy = 100f + sin(t * TAU / 2.6f) * 4f + tuck * 34f - jump * 0.5f
        val nose = cx + len / 2f
        val tailBase = cx - len / 2f
        val tilt = if (swimming) beat * 2f else 0f

        c.save()
        c.rotate(tilt + tuck * 4f, cx, cy)
        tailFin(c, tailBase + (if (round) 6f else 4f), cy, beat)
        dorsal(c, cy)
        if (look.has(LOBE_FINS)) lobeFin(c, cx - len * 0.05f, cy + ht * 0.42f, beat, far = true)

        bodyPath.rewind()
        if (round) {
            bodyPath.addCircle(cx, cy, ht / 2f, Path.Direction.CW)
        } else {
            bodyPath.moveTo(nose, cy + ht * 0.04f)
            bodyPath.cubicTo(nose - len * 0.05f, cy - ht * 0.62f, tailBase + len * 0.3f, cy - ht * 0.5f, tailBase, cy - ht * 0.1f)
            bodyPath.lineTo(tailBase, cy + ht * 0.1f)
            bodyPath.cubicTo(tailBase + len * 0.3f, cy + ht * 0.5f, nose - len * 0.08f, cy + ht * 0.6f, nose, cy + ht * 0.04f)
            bodyPath.close()
        }
        if (look.has(SPINES)) spines(c, cy)
        c.drawPath(bodyPath, kit.shaded(base, cy - ht / 2f, cy + ht / 2f))
        c.save()
        c.clipPath(bodyPath)
        kit.ovalC(c, cx + len * 0.05f, cy + ht * 0.5f, len * 0.55f, ht * 0.32f, kit.solid(belly.base))
        bodyMarks(c, cy)
        if (look.has(HEAD_ARMOR)) headArmor(c, nose, cy)
        c.restore()
        kit.sheen(c, cx + len * 0.05f, cy, len * 0.45f, ht * 0.5f)

        if (look.has(BEAK_SNOUT)) {
            kit.roundRect(c, nose - 6f, cy - ht * 0.02f, nose + 16f, cy + ht * 0.16f, ht * 0.08f, kit.shaded(base, cy, cy + ht * 0.16f))
        }
        pectoral(c, cx + len * 0.12f, cy + ht * 0.14f, beat)
        if (look.has(LOBE_FINS)) lobeFin(c, cx + len * 0.02f, cy + ht * 0.4f, beat, far = false)
        if (look.has(LURE)) lure(c, nose - len * 0.18f, cy - ht * 0.42f)
        face(c, nose, cy)
        c.restore()

        if (pose.action == Action.IDLE) bubbles(c, nose + 6f, cy - ht * 0.2f)
    }

    private fun tailFin(c: Canvas, x: Float, y: Float, beat: Float) {
        val p = kit.path
        val s = tailS
        c.save()
        c.rotate(beat * 14f, x, y)
        p.rewind()
        if (mammal) {
            kit.roundRect(c, x - s * 0.5f, y - ht * 0.09f, x + 4f, y + ht * 0.09f, ht * 0.08f, kit.solid(base.dark))
            p.moveTo(x - s * 0.45f, y)
            p.cubicTo(x - s * 0.8f, y - s * 0.15f, x - s * 1.3f, y - s * 0.45f, x - s * 1.25f, y - s * 0.12f)
            p.lineTo(x - s * 1.3f, y + s * 0.12f)
            p.cubicTo(x - s * 1.3f, y + s * 0.45f, x - s * 0.8f, y + s * 0.15f, x - s * 0.45f, y)
            p.close()
            c.drawPath(p, kit.shaded(base, y - s * 0.4f, y + s * 0.4f))
        } else if (look.has(LONG_TAIL)) {
            p.moveTo(x + 4f, y)
            p.cubicTo(x - s * 0.6f, y - s * 1.0f, x - s * 1.3f, y - s * 0.5f, x - s * 1.25f, y - s * 0.05f)
            p.cubicTo(x - s * 1.1f, y + s * 0.1f, x - s * 1.35f, y + s * 0.6f, x - s * 1.0f, y + s * 0.8f)
            p.cubicTo(x - s * 0.6f, y + s * 0.6f, x - s * 0.3f, y + s * 0.3f, x + 4f, y)
            p.close()
            c.drawPath(p, kit.solid(ColorX.withAlpha(finTone.base, 220)))
            kit.stroke.strokeWidth = 1.2f; kit.stroke.color = finTone.deep; kit.stroke.shader = null
            for (i in -1..1) c.drawLine(x, y, x - s * 1.1f, y + i * s * 0.4f, kit.stroke)
        } else {
            p.moveTo(x + 4f, y)
            p.lineTo(x - s * 0.95f, y - s * 0.75f)
            p.quadTo(x - s * 0.6f, y, x - s * 0.95f, y + s * 0.75f)
            p.close()
            c.drawPath(p, kit.shaded(finTone, y - s, y + s))
        }
        c.restore()
    }

    private fun dorsal(c: Canvas, cy: Float) {
        val p = kit.path
        p.rewind()
        val top = cy - ht * (if (round) 0.45f else 0.4f)
        if (look.has(TALL_FIN)) {
            val x = cx + (if (mammal) 0f else -len * 0.02f)
            val h = if (mammal) ht * 0.45f else ht * 0.7f
            p.moveTo(x - len * 0.14f, top + 6f)
            p.quadTo(x - len * 0.02f, top - h * 0.4f, x - len * 0.08f, top - h)
            p.quadTo(x + len * 0.04f, top - h * 0.3f, x + len * 0.1f, top + 6f)
            p.close()
            c.drawPath(p, kit.shaded(base, top - h, top + 6f))
        } else if (!mammal) {
            p.moveTo(cx - len * 0.22f, top + 6f)
            p.quadTo(cx - len * 0.15f, top - ht * 0.32f, cx + len * 0.12f, top + 4f)
            p.close()
            c.drawPath(p, kit.solid(finTone.base))
        }
    }

    private fun pectoral(c: Canvas, x: Float, y: Float, beat: Float) {
        c.save()
        c.rotate(20f + beat * 18f, x, y)
        kit.oval(c, x - (if (mammal) 22f else 16f), y - 5f, x + 2f, y + 5f, kit.solid(if (mammal) base.dark else finTone.base))
        c.restore()
    }

    private fun lobeFin(c: Canvas, x: Float, y: Float, beat: Float, far: Boolean) {
        c.save()
        c.rotate(10f + beat * 15f + (if (far) -12f else 0f), x, y)
        kit.roundRect(c, x - 4f, y - 3f, x + 4f, y + 12f, 4f, kit.solid(if (far) base.dark else base.base))
        kit.ovalC(c, x, y + 16f, 7f, 9f, kit.solid(if (far) finTone.dark else finTone.base))
        c.restore()
    }

    private fun bodyMarks(c: Canvas, cy: Float) {
        when (look.pattern) {
            Pattern.BANDS -> for (i in 0 until 3) {
                val x = cx + len * 0.3f - i * len * 0.32f
                kit.roundRect(c, x - 8f, cy - ht, x + 8f, cy + ht, 6f, kit.solid(look.palette.dark))
                kit.roundRect(c, x - 5.5f, cy - ht, x + 5.5f, cy + ht, 5f, kit.solid(Color.WHITE))
            }
            Pattern.SPOTS -> for (i in 0 until spots.size / 2) {
                c.drawCircle(cx + spots[2 * i] * len * 0.38f, cy + spots[2 * i + 1] * ht * 0.3f, 2.6f + i % 2, kit.solid(finTone.light))
            }
            Pattern.STRIPES -> for (i in 0 until 4) {
                val x = cx - len * 0.25f + i * len * 0.14f
                kit.roundRect(c, x - 3f, cy - ht, x + 3f, cy + ht * 0.1f, 3f, kit.solid(finTone.base))
            }
            else -> Unit
        }
        if (mammal && look.bodyLength > 1.1f) {
            kit.stroke.strokeWidth = 1.6f; kit.stroke.color = belly.dark; kit.stroke.shader = null
            for (i in 0 until 4) {
                val y = cy + ht * (0.2f + i * 0.07f)
                c.drawLine(cx + len * 0.05f, y, cx + len * 0.4f, y - 3f, kit.stroke)
            }
        }
    }

    private fun headArmor(c: Canvas, nose: Float, cy: Float) {
        kit.rect.set(nose - len * 0.38f, cy - ht, nose + 4f, cy + ht)
        c.drawRect(kit.rect, kit.shaded(finTone, cy - ht / 2f, cy + ht / 2f))
        kit.stroke.strokeWidth = 1.8f; kit.stroke.color = finTone.deep; kit.stroke.shader = null
        c.drawLine(nose - len * 0.38f, cy - ht, nose - len * 0.36f, cy + ht, kit.stroke)
        c.drawLine(nose - len * 0.2f, cy - ht * 0.5f, nose - len * 0.32f, cy + ht * 0.2f, kit.stroke)
    }

    private fun spines(c: Canvas, cy: Float) {
        val p = kit.path
        p.rewind()
        val r = ht / 2f
        for (i in 0 until 14) {
            val a = i / 14f * TAU
            val bx = cx + cos(a) * r * 0.9f
            val by = cy + sin(a) * r * 0.9f
            p.moveTo(bx + cos(a + 0.25f) * 3f, by + sin(a + 0.25f) * 3f)
            p.lineTo(cx + cos(a) * (r + 8f), cy + sin(a) * (r + 8f))
            p.lineTo(bx + cos(a - 0.25f) * 3f, by + sin(a - 0.25f) * 3f)
            p.close()
        }
        c.drawPath(p, kit.solid(base.dark))
    }

    private fun lure(c: Canvas, x: Float, y: Float) {
        val st = kit.stroke
        st.strokeWidth = 2.2f; st.color = base.dark; st.shader = null
        val bx = x + 26f + sin(t * 2f) * 2f
        val by = y - 18f
        val p = kit.path
        p.rewind()
        p.moveTo(x, y + 4f)
        p.quadTo(x + 6f, y - 26f, bx, by)
        c.drawPath(p, st)
        c.drawCircle(bx, by, 9f, kit.solid(Color.argb(70, 255, 236, 140)))
        c.drawCircle(bx, by, 4.5f, kit.solid(0xFFFFE07A.toInt()))
    }

    private fun face(c: Canvas, nose: Float, cy: Float) {
        val ex = if (round) cx + ht * 0.2f else nose - len * (if (mammal) 0.2f else 0.16f)
        val ey = cy - ht * (if (mammal) -0.02f else 0.12f)
        val r = if (round) 6.5f else 5.5f * look.headSize
        if (eyeMode == 0 && blink < 0.85f && !mammal) c.drawCircle(ex, ey, r * 1.35f, kit.solid(Color.WHITE))
        kit.eye(c, ex, ey, r, blink, eyeMode, look.palette.eye)
        val mx = if (round) cx + ht * 0.46f else nose - 4f
        val my = cy + ht * 0.12f
        val p = kit.path
        p.rewind()
        p.moveTo(mx, my)
        p.quadTo(mx - 6f, my + 6f, mx - 13f, my + 2f)
        kit.stroke.strokeWidth = 2f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
        if (look.has(TEETH)) {
            p.rewind()
            for (i in 0 until 3) {
                val tx = mx - 3f - i * 4.5f
                p.moveTo(tx - 1.8f, my + 1.5f + i * 0.6f)
                p.lineTo(tx, my + 6f)
                p.lineTo(tx + 1.8f, my + 1.5f + i * 0.6f)
                p.close()
            }
            c.drawPath(p, kit.solid(0xFFFBF6EC.toInt()))
        }
        kit.blush(c, ex - 2f, ey + r * 2.2f, r * 0.9f)
    }

    private fun bubbles(c: Canvas, x: Float, y: Float) {
        val phase = (t % 2.4f) / 2.4f
        c.drawCircle(x + 4f, y - phase * 30f, 3.2f + phase * 1.5f, bubble)
        val p2 = ((t + 1.2f) % 2.4f) / 2.4f
        c.drawCircle(x + 10f, y - p2 * 34f, 2.2f + p2, bubble)
    }
}
