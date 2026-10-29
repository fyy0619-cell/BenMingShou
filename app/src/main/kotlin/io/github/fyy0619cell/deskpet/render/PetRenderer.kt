package io.github.fyy0619cell.deskpet.render

import android.graphics.Canvas
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws one pet into a 200x200 unit box, facing right, feet on [GROUND].
 * Callers scale/flip the canvas; subclasses only implement anatomy.
 */
abstract class PetRenderer(val look: Look) {
    protected val kit = Kit()
    protected val base = Tone(look.palette.base)
    protected val belly = Tone(look.palette.belly)
    protected val accent = Tone(look.palette.accent)
    protected val dark get() = look.palette.dark

    protected var breathe = 0f
    protected var legSwing = 0f
    protected var tuck = 0f
    protected var headTilt = 0f
    protected var tailSway = 0f
    protected var jump = 0f
    protected var bob = 0f
    protected var eyeMode = 0
    protected var blink = 0f
    protected var flap = 0f
    protected var curl = 0f
    protected var t = 0f

    /** Half-width of the ground shadow; subclasses size it to their body. */
    protected open val shadowHalfWidth = 62f
    protected open val shadowCenterX = 100f

    fun draw(c: Canvas, pose: Pose) {
        prepare(pose)
        val lift = (jump / 40f).coerceIn(0f, 0.5f)
        kit.groundShadow(c, shadowCenterX, GROUND, shadowHalfWidth, 1f - lift)
        c.save()
        c.translate(0f, bob - jump)
        drawAnatomy(c, pose)
        c.restore()
    }

    protected abstract fun drawAnatomy(c: Canvas, pose: Pose)

    private fun prepare(p: Pose) {
        t = p.time
        breathe = 0f; legSwing = 0f; tuck = 0f; headTilt = 0f
        jump = 0f; bob = 0f; eyeMode = 0; blink = p.blink; curl = 0f
        tailSway = sin(t * TAU / 2.6f)
        flap = sin(t * TAU * 9f)
        when (p.action) {
            Action.IDLE -> breathe = sin(t * TAU / 3.4f)
            Action.WALK -> {
                val a = p.walkPhase * TAU
                legSwing = sin(a) * 24f
                bob = -abs(cos(a)) * 2.2f
                tailSway = sin(a)
            }
            Action.SLEEP -> {
                tuck = 1f
                curl = 1f
                breathe = sin(t * TAU / 5f)
                eyeMode = 2
                headTilt = 9f
                tailSway = 0.2f * sin(t * TAU / 6f)
            }
            Action.HAPPY -> {
                val s = sin(p.progress * PI.toFloat())
                jump = s * 22f
                tuck = 0.45f * s
                eyeMode = 1
                tailSway = sin(t * TAU * 2f)
            }
            Action.EAT -> {
                headTilt = 16f * abs(sin(p.progress * PI.toFloat() * 3f))
                eyeMode = if (headTilt > 8f) 1 else 0
                breathe = sin(t * TAU / 3.4f)
            }
        }
        if (eyeMode != 0) blink = 0f
    }

    companion object {
        const val GROUND = 178f
        const val TAU = (2 * PI).toFloat()
    }
}
