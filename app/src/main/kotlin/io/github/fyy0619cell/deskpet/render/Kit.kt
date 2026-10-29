package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.LongSparseArray

/** Three precomputed shades so per-frame drawing never converts colors. */
class Tone(val base: Int) {
    val light = ColorX.lighter(base, 0.07f)
    val dark = ColorX.darker(base, 0.09f)
    val deep = ColorX.darker(base, 0.18f)
}

class Kit {
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    val path = Path()
    val path2 = Path()
    val rect = RectF()
    private val shaders = LongSparseArray<Shader>()

    private fun mixIn(h: Long, v: Int) = (h xor v.toLong()) * 1099511628211L

    private fun key(a: Int, b: Int, y0: Float, y1: Float, kind: Int): Long {
        var h = 1469598103934665603L
        h = mixIn(h, a); h = mixIn(h, b)
        h = mixIn(h, y0.toRawBits()); h = mixIn(h, y1.toRawBits())
        return mixIn(h, kind)
    }

    fun vgrad(top: Int, bottom: Int, y0: Float, y1: Float): Shader {
        val k = key(top, bottom, y0, y1, 0)
        return shaders.get(k) ?: LinearGradient(0f, y0, 0f, y1, top, bottom, Shader.TileMode.CLAMP)
            .also { shaders.put(k, it) }
    }

    private fun shadowShader(cx: Float, r: Float): Shader {
        val k = key(cx.toRawBits(), r.toRawBits(), 0f, 0f, 1)
        return shaders.get(k) ?: RadialGradient(
            cx, 0f, r, Color.argb(46, 40, 26, 60), Color.argb(0, 40, 26, 60), Shader.TileMode.CLAMP,
        ).also { shaders.put(k, it) }
    }

    fun solid(color: Int): Paint = fill.apply { shader = null; this.color = color }

    fun shaded(tone: Tone, top: Float, bottom: Float): Paint = fill.apply {
        color = tone.base
        shader = vgrad(tone.light, tone.dark, top, bottom)
    }

    fun oval(c: Canvas, l: Float, t: Float, r: Float, b: Float, p: Paint) {
        rect.set(l, t, r, b); c.drawOval(rect, p)
    }

    fun ovalC(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, p: Paint) =
        oval(c, cx - rx, cy - ry, cx + rx, cy + ry, p)

    fun ovalShaded(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, tone: Tone) =
        ovalC(c, cx, cy, rx, ry, shaded(tone, cy - ry, cy + ry))

    fun roundRect(c: Canvas, l: Float, t: Float, r: Float, b: Float, rad: Float, p: Paint) {
        rect.set(l, t, r, b); c.drawRoundRect(rect, rad, rad, p)
    }

    fun line(c: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, w: Float, color: Int) {
        stroke.strokeWidth = w; stroke.color = color; stroke.shader = null
        c.drawLine(x0, y0, x1, y1, stroke)
    }

    fun groundShadow(c: Canvas, cx: Float, groundY: Float, halfW: Float, squash: Float = 1f) {
        c.save()
        c.translate(0f, groundY)
        c.scale(1f, 0.16f)
        fill.color = Color.BLACK
        fill.shader = shadowShader(cx, halfW * squash)
        c.drawCircle(cx, 0f, halfW * squash, fill)
        fill.shader = null
        c.restore()
    }

    /** blink: 0 open .. 1 closed. mode: 0 normal, 1 happy arcs, 2 asleep. */
    fun eye(c: Canvas, cx: Float, cy: Float, r: Float, blink: Float, mode: Int, color: Int) {
        if (mode == 1) {
            stroke.strokeWidth = r * 0.55f; stroke.color = color; stroke.shader = null
            rect.set(cx - r, cy - r * 0.6f, cx + r, cy + r * 1.1f)
            c.drawArc(rect, 200f, 140f, false, stroke)
            return
        }
        if (mode == 2 || blink > 0.85f) {
            stroke.strokeWidth = r * 0.5f; stroke.color = color; stroke.shader = null
            rect.set(cx - r, cy - r * 1.0f, cx + r, cy + r * 0.6f)
            c.drawArc(rect, 20f, 140f, false, stroke)
            return
        }
        val ry = r * 1.12f * (1f - blink)
        ovalC(c, cx, cy, r, ry, solid(color))
        if (blink < 0.4f) {
            c.drawCircle(cx + r * 0.32f, cy - r * 0.38f, r * 0.34f, solid(Color.WHITE))
            c.drawCircle(cx - r * 0.3f, cy + r * 0.35f, r * 0.14f, solid(ColorX.withAlpha(Color.WHITE, 170)))
        }
    }

    /** Soft top-left highlight that gives flat shapes a little volume. */
    fun sheen(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float) {
        c.save()
        c.rotate(-24f, cx, cy)
        ovalC(c, cx - rx * 0.22f, cy - ry * 0.42f, rx * 0.5f, ry * 0.26f, solid(Color.argb(46, 255, 255, 255)))
        c.restore()
    }

    fun blush(c: Canvas, cx: Float, cy: Float, r: Float) =
        ovalC(c, cx, cy, r, r * 0.62f, solid(Color.argb(70, 240, 120, 140)))
}
