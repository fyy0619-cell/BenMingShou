package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import io.github.fyy0619cell.deskpet.render.Feature.ANTENNAE
import io.github.fyy0619cell.deskpet.render.Feature.PROLEGS
import io.github.fyy0619cell.deskpet.render.Feature.SEGMENTS
import io.github.fyy0619cell.deskpet.render.Feature.TONGUE
import kotlin.math.sin

/** Snakes, earthworms and caterpillars: a tube built from overlapping discs. */
class SerpentRenderer(look: Look) : PetRenderer(look) {
    private val caterpillar = look.has(PROLEGS)
    private val worm = look.has(SEGMENTS) && !caterpillar
    private val n = if (caterpillar) 9 else 34
    private val xs = FloatArray(n)
    private val ys = FloatArray(n)
    private val rs = FloatArray(n)
    private val len = 132f * look.bodyLength
    private val thick = 13f * look.bodyHeight
    private val tongue = 0xFFE0525C.toInt()
    private val bandColor = look.palette.accent

    override val shadowHalfWidth = len * 0.55f
    override val shadowCenterX = 96f

    override fun drawAnatomy(c: Canvas, pose: Pose) {
        val moving = pose.action == Action.WALK
        val phase = if (moving) pose.walkPhase * TAU * 2f else t * 0.8f
        val amp = if (moving) 1f else 0.35f
        val x0 = 100f - len / 2f
        val raise = if (worm || caterpillar) 0f else 26f * (1f - tuck)
        for (i in 0 until n) {
            val f = i / (n - 1f)
            xs[i] = x0 + f * len
            val ground = GROUND - thick
            val wave = sin(phase - f * 9f) * (if (worm || caterpillar) 5f else 9f) * amp * (if (caterpillar) 0.6f else 1f)
            val hump = if (caterpillar && moving) (sin(phase - f * 6f).coerceAtLeast(0f)) * 10f else 0f
            val neck = if (f > 0.78f) ((f - 0.78f) / 0.22f) * raise else 0f
            ys[i] = ground + wave * 0.4f - hump - neck - breathe * 0.5f
            rs[i] = when {
                caterpillar -> thick * 1.15f
                worm -> thick * 0.9f * (if (f < 0.06f) 0.8f else 1f)
                f < 0.22f -> thick * (0.28f + 0.6f * f / 0.22f)
                f > 0.85f -> thick * 0.78f
                else -> thick * 0.88f
            }
        }

        if (caterpillar) prolegs(c)
        val shade = kit.vgrad(base.light, base.dark, GROUND - thick * 2.5f - raise, GROUND)
        for (i in 0 until n) {
            val f = i / (n - 1f)
            val p = kit.fill
            p.shader = shade
            if (look.pattern == Pattern.BANDS && i % 6 in 2..3 && f < 0.85f) {
                p.shader = null; p.color = bandColor
            }
            if (worm && i in (n * 0.62f).toInt()..(n * 0.68f).toInt()) {
                p.shader = null; p.color = accent.base
            }
            c.drawCircle(xs[i], ys[i], rs[i], p)
            if (caterpillar) {
                kit.stroke.strokeWidth = 1.6f; kit.stroke.color = base.deep; kit.stroke.shader = null
                c.drawCircle(xs[i], ys[i], rs[i], kit.stroke)
                if (look.pattern == Pattern.SPOTS) c.drawCircle(xs[i] - 2f, ys[i] - rs[i] * 0.45f, 2.8f, kit.solid(accent.base))
            }
        }
        kit.fill.shader = null
        if (worm) rings(c)
        sheenTube(c)
        head(c, xs[n - 1], ys[n - 1], rs[n - 1])
    }

    private fun rings(c: Canvas) {
        val st = kit.stroke
        st.strokeWidth = 1.4f; st.color = ColorX.withAlpha(base.deep, 140); st.shader = null
        for (i in 3 until n - 3 step 3) {
            c.drawLine(xs[i], ys[i] - rs[i] * 0.85f, xs[i] - 1.5f, ys[i] + rs[i] * 0.85f, st)
        }
    }

    private fun sheenTube(c: Canvas) {
        val p = kit.solid(Color.argb(40, 255, 255, 255))
        for (i in 2 until n - 2 step 2) c.drawCircle(xs[i] - 1f, ys[i] - rs[i] * 0.5f, rs[i] * 0.32f, p)
    }

    private fun prolegs(c: Canvas) {
        val st = kit.stroke
        st.strokeWidth = 4f; st.color = base.deep; st.shader = null
        for (i in 0 until n - 1) c.drawLine(xs[i], ys[i] + rs[i] * 0.6f, xs[i] + 1f, GROUND - 1f, st)
    }

    private fun head(c: Canvas, x: Float, y: Float, r: Float) {
        val hx = x + r * (if (caterpillar) 0.9f else 0.6f)
        val hr = if (caterpillar) r * 1.1f else if (worm) r * 1.05f else thick * 1.3f
        if (look.has(ANTENNAE)) {
            kit.line(c, hx, y - hr * 0.8f, hx + 5f, y - hr * 1.6f, 2.2f, look.palette.dark)
            c.drawCircle(hx + 5f, y - hr * 1.6f, 2.6f, kit.solid(look.palette.dark))
        }
        if (look.has(TONGUE) && (t % 3.2f) < 0.5f && eyeMode != 2) {
            val tx = hx + hr * 1.1f
            kit.line(c, tx - 2f, y + hr * 0.25f, tx + 10f, y + hr * 0.25f, 1.8f, tongue)
            kit.line(c, tx + 10f, y + hr * 0.25f, tx + 14f, y + hr * 0.05f, 1.6f, tongue)
            kit.line(c, tx + 10f, y + hr * 0.25f, tx + 14f, y + hr * 0.45f, 1.6f, tongue)
        }
        kit.ovalShaded(c, hx, y, hr * 1.15f, hr * 0.92f, base)
        kit.sheen(c, hx, y, hr, hr * 0.9f)
        if (eyeMode == 0 && blink < 0.85f && !worm) c.drawCircle(hx + hr * 0.35f, y - hr * 0.2f, hr * 0.34f, kit.solid(Color.WHITE))
        kit.eye(c, hx + hr * 0.4f, y - hr * 0.2f, hr * 0.22f, blink, eyeMode, look.palette.eye)
        val p = kit.path
        p.rewind()
        p.moveTo(hx + hr * 1.0f, y + hr * 0.3f)
        p.quadTo(hx + hr * 0.7f, y + hr * 0.6f, hx + hr * 0.35f, y + hr * 0.4f)
        kit.stroke.strokeWidth = 1.8f; kit.stroke.color = base.deep; kit.stroke.shader = null
        c.drawPath(p, kit.stroke)
        kit.blush(c, hx + hr * 0.3f, y + hr * 0.4f, hr * 0.22f)
    }
}
