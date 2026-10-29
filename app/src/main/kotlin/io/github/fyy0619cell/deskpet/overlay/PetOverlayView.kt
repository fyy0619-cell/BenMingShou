package io.github.fyy0619cell.deskpet.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.os.SystemClock
import android.view.Choreographer
import android.view.View
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.ContentBounds
import io.github.fyy0619cell.deskpet.render.Halo
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.render.Pose
import io.github.fyy0619cell.deskpet.render.Renderers

/**
 * Draws the pet and paces its own frames: [tick] runs before each frame, then the view
 * redraws and schedules the next frame at the rate the current action needs.
 */
@SuppressLint("ViewConstructor")
class PetOverlayView(context: Context, look: Look) : View(context) {
    private val renderer = Renderers.create(look)

    /** The part of the 200-unit box this pet ever paints; the window shows only this. */
    val content: RectF = ContentBounds.measure(renderer)
    val pose = Pose()
    var facingRight = true
    var tick: ((Long) -> Unit)? = null
    var halo = true
    val haloPainter = Halo()
    private val choreographer = Choreographer.getInstance()
    private var running = false
    private val frame: Choreographer.FrameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            tick?.invoke(SystemClock.uptimeMillis())
            invalidate()
            choreographer.postFrameCallbackDelayed(this, frameDelay())
        }
    }

    fun start() {
        if (running) return
        running = true
        choreographer.postFrameCallback(frame)
    }

    fun stop() {
        running = false
        choreographer.removeFrameCallback(frame)
    }

    /** Delay before the next vsync we care about; Choreographer then waits for that vsync. */
    private fun frameDelay(): Long = when (pose.action) {
        Action.SLEEP -> 160L
        Action.IDLE -> 75L
        else -> 25L
    }

    override fun onDraw(canvas: Canvas) {
        val s = width / content.width()
        canvas.save()
        if (!facingRight) canvas.scale(-1f, 1f, width / 2f, height / 2f)
        canvas.scale(s, s)
        canvas.translate(-content.left, -content.top)
        if (halo) haloPainter.draw(canvas, renderer, pose)
        renderer.draw(canvas, pose)
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }
}
