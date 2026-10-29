package io.github.fyy0619cell.deskpet.ui

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.render.Pose
import io.github.fyy0619cell.deskpet.render.Renderers
import io.github.fyy0619cell.deskpet.ui.theme.Palette

/**
 * Animated pet at up to 30 fps. [silhouette] 1 draws a solid ink shape, 0 the real colours.
 * [animate] false freezes time (used by screenshot tests).
 */
@Composable
fun PetCanvas(
    look: Look,
    modifier: Modifier = Modifier,
    action: Action = Action.IDLE,
    silhouette: Float = 0f,
    animate: Boolean = true,
) {
    val renderer = remember(look) { Renderers.create(look) }
    val pose = remember { Pose() }
    val inkPaint = remember { Paint().apply { colorFilter = PorterDuffColorFilter(Palette.Ink.toArgb(), PorterDuff.Mode.SRC_IN) } }
    var time by remember { mutableFloatStateOf(0.6f) }
    if (animate) {
        LaunchedEffect(Unit) {
            val start = withFrameMillis { it }
            var last = 0L
            while (true) {
                withFrameMillis { now ->
                    if (now - last >= FRAME_MS) {
                        last = now
                        time = (now - start) / 1000f
                    }
                }
            }
        }
    }
    Canvas(modifier) {
        pose.time = time
        pose.action = action
        pose.walkPhase = (time * 1.2f) % 1f
        pose.progress = (time % 1.2f) / 1.2f
        pose.blink = if (time % 4.2f < 0.14f) 1f else 0f
        val s = minOf(size.width, size.height) / 200f
        drawIntoCanvas { c ->
            val nc = c.nativeCanvas
            nc.save()
            nc.translate((size.width - 200f * s) / 2f, (size.height - 200f * s) / 2f)
            nc.scale(s, s)
            val sil = silhouette.coerceIn(0f, 1f)
            if (sil < 1f) {
                if (sil > 0f) nc.saveLayerAlpha(0f, 0f, 200f, 200f, ((1f - sil) * 255).toInt())
                renderer.draw(nc, pose)
                if (sil > 0f) nc.restore()
            }
            if (sil > 0f) {
                inkPaint.alpha = (sil * 255).toInt()
                nc.saveLayer(0f, 0f, 200f, 200f, inkPaint)
                renderer.draw(nc, pose)
                nc.restore()
            }
            nc.restore()
        }
    }
}

private const val FRAME_MS = 33L
