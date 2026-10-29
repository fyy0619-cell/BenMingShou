package io.github.fyy0619cell.deskpet.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.RectF

/**
 * The part of the 200×200 box a pet can ever paint, found by drawing every pose once at
 * half resolution. The overlay window is cropped to this so empty corners don't swallow
 * taps meant for the app underneath.
 */
object ContentBounds {
    private const val SCALE = 0.5f
    private const val PAD = 6f

    private val samples: List<(Pose) -> Unit> = buildList {
        for (t in floatArrayOf(0f, 0.7f, 1.3f, 2.1f, 3.4f)) add { p -> p.action = Action.IDLE; p.time = t }
        for (w in floatArrayOf(0f, 0.25f, 0.5f, 0.75f)) for (t in floatArrayOf(0.05f, 0.33f, 1.1f)) {
            add { p -> p.action = Action.WALK; p.walkPhase = w; p.time = t }
        }
        for (pr in floatArrayOf(0.25f, 0.5f, 0.75f)) add { p -> p.action = Action.HAPPY; p.progress = pr; p.time = pr }
        for (pr in floatArrayOf(0.2f, 0.5f)) add { p -> p.action = Action.EAT; p.progress = pr; p.time = pr }
        for (t in floatArrayOf(0f, 2.5f)) add { p -> p.action = Action.SLEEP; p.time = t }
    }

    fun measure(renderer: PetRenderer): RectF {
        val n = (200 * SCALE).toInt()
        val bmp = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val pose = Pose()
        val pixels = IntArray(n * n)
        var minX = n; var minY = n; var maxX = -1; var maxY = -1
        for (sample in samples) {
            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            pose.walkPhase = 0f; pose.progress = 0f; pose.blink = 0f
            sample(pose)
            c.save()
            c.scale(SCALE, SCALE)
            renderer.draw(c, pose)
            c.restore()
            bmp.getPixels(pixels, 0, n, 0, 0, n, n)
            for (y in 0 until n) {
                val row = y * n
                for (x in 0 until n) {
                    if (pixels[row + x] ushr 24 > 10) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }
        }
        bmp.recycle()
        if (maxX < 0) return RectF(0f, 0f, 200f, 200f)
        return RectF(
            (minX / SCALE - PAD).coerceAtLeast(0f),
            (minY / SCALE - PAD).coerceAtLeast(0f),
            ((maxX + 1) / SCALE + PAD).coerceAtMost(200f),
            ((maxY + 1) / SCALE + PAD).coerceAtMost(200f),
        )
    }
}
