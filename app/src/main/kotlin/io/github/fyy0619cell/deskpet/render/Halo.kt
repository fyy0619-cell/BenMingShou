package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter

/**
 * A faint enlarged silhouette behind the pet so it stays legible on any wallpaper:
 * dark on light wallpapers, light on dark ones.
 */
class Halo(private val grow: Float = 1.045f) {
    private val paint = Paint()

    var onDarkWallpaper: Boolean = false
        set(v) {
            field = v
            paint.colorFilter = PorterDuffColorFilter(if (v) LIGHT else DARK, PorterDuff.Mode.SRC_IN)
            paint.alpha = if (v) 70 else 34
        }

    init {
        onDarkWallpaper = false
    }

    fun draw(c: Canvas, renderer: PetRenderer, pose: Pose) {
        c.saveLayer(0f, 0f, 200f, 200f, paint)
        c.scale(grow, grow, 100f, 112f)
        renderer.draw(c, pose)
        c.restore()
    }

    private companion object {
        const val DARK = 0xFF1E1A24.toInt()
        const val LIGHT = 0xFFF4F1EC.toInt()
    }
}
