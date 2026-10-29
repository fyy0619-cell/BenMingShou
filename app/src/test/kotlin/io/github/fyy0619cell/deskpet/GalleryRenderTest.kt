package io.github.fyy0619cell.deskpet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.Pose
import io.github.fyy0619cell.deskpet.render.Rarity
import io.github.fyy0619cell.deskpet.render.Renderers
import io.github.fyy0619cell.deskpet.render.Variation
import io.github.fyy0619cell.deskpet.species.Catalog
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class GalleryRenderTest {
    private data class Frame(val label: String, val action: Action, val walk: Float = 0f, val progress: Float = 0f, val time: Float = 0.6f)

    private val frames = listOf(
        Frame("idle", Action.IDLE),
        Frame("walk A", Action.WALK, walk = 0.25f),
        Frame("walk B", Action.WALK, walk = 0.75f),
        Frame("sleep", Action.SLEEP),
        Frame("happy", Action.HAPPY, progress = 0.5f),
    )

    private val cell = 360
    private val scale = cell / 200f
    private val labelH = 44

    @Test
    fun renderGallery() {
        val dir = File(System.getProperty("gallery.dir")!!).apply { mkdirs() }
        val sheet = Bitmap.createBitmap(cell * frames.size, (cell + labelH) * Catalog.all.size, Bitmap.Config.ARGB_8888)
        val sc = Canvas(sheet)
        sc.drawColor(BG)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF5A5262.toInt(); textSize = 24f }
        val pose = Pose()

        Catalog.all.forEachIndexed { row, sp ->
            val renderer = Renderers.create(sp.look)
            val rowBmp = Bitmap.createBitmap(cell * frames.size, cell + labelH, Bitmap.Config.ARGB_8888)
            val rc = Canvas(rowBmp)
            rc.drawColor(BG)
            frames.forEachIndexed { i, f ->
                pose.action = f.action; pose.walkPhase = f.walk; pose.progress = f.progress
                pose.time = f.time; pose.blink = 0f
                rc.save()
                rc.translate(i * cell.toFloat(), labelH.toFloat())
                rc.scale(scale, scale)
                renderer.draw(rc, pose)
                rc.restore()
                rc.drawText(if (i == 0) "${sp.nameZh}  ${sp.latin}" else f.label, i * cell + 14f, 32f, text)
            }
            File(dir, "${sp.id}.png").outputStream().use { rowBmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            sc.drawBitmap(rowBmp, 0f, row * (cell + labelH).toFloat(), null)
        }
        File(dir, "_all.png").outputStream().use { sheet.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun renderVariants() {
        val dir = File(System.getProperty("gallery.dir")!!).apply { mkdirs() }
        val ids = listOf("tiger", "panda", "bee", "trex")
        val cols = listOf<Pair<String, Rarity?>>(
            "seed 1" to Rarity.NORMAL, "seed 2" to Rarity.NORMAL, "seed 3" to Rarity.NORMAL,
            "白化" to Rarity.ALBINO, "金色" to Rarity.GOLDEN, "黑化" to Rarity.MELANISTIC,
        )
        val sheet = Bitmap.createBitmap(cell * cols.size, (cell + labelH) * ids.size, Bitmap.Config.ARGB_8888)
        val sc = Canvas(sheet)
        sc.drawColor(BG)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF5A5262.toInt(); textSize = 24f }
        val pose = Pose().apply { time = 0.6f }
        ids.forEachIndexed { row, id ->
            val sp = Catalog.all.first { it.id == id }
            cols.forEachIndexed { i, (label, rarity) ->
                val look = Variation.individualize(sp.look, (row * 10 + i + 1).toLong() * 7919L, rarity)
                sc.save()
                sc.translate(i * cell.toFloat(), row * (cell + labelH) + labelH.toFloat())
                sc.scale(scale, scale)
                Renderers.create(look).draw(sc, pose)
                sc.restore()
                sc.drawText("${sp.nameZh} · $label", i * cell + 14f, row * (cell + labelH) + 32f, text)
            }
        }
        File(dir, "_variants.png").outputStream().use { sheet.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    companion object {
        val BG = Color.rgb(246, 244, 241)
    }
}
