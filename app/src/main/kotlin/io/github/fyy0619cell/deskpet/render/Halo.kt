package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter

/** A faint enlarged silhouette behind the pet so pale animals stay legible on pale wallpapers. */
class Halo(alpha: Int = 34, private val grow: Float = 1.045f) {
    private val paint = Paint().apply {
        colorFilter = PorterDuffColorFilter(0xFF1E1A24.toInt(), PorterDuff.Mode.SRC_IN)
        this.alpha = alpha
    }

    fun draw(c: Canvas, renderer: PetRenderer, pose: Pose) {
        c.saveLayer(0f, 0f, 200f, 200f, paint)
        c.scale(grow, grow, 100f, 112f)
        renderer.draw(c, pose)
        c.restore()
    }
}
