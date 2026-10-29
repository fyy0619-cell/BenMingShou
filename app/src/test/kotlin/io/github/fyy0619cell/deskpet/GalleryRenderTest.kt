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
import io.github.fyy0619cell.deskpet.species.Species
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

    private val dir = File(System.getProperty("gallery.dir")!!).apply { mkdirs() }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF5A5262.toInt() }
    private val pose = Pose()

    private fun drawPet(c: Canvas, sp: Species, f: Frame, x: Float, y: Float, cell: Int, look: io.github.fyy0619cell.deskpet.render.Look = sp.look) {
        pose.action = f.action; pose.walkPhase = f.walk; pose.progress = f.progress
        pose.time = f.time; pose.blink = 0f
        c.save()
        c.translate(x, y)
        c.scale(cell / 200f, cell / 200f)
        Renderers.create(look).draw(c, pose)
        c.restore()
    }

    private fun save(bmp: Bitmap, name: String) =
        File(dir, name).outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }

    /** One strip of all poses per species, plus one sheet per body plan. */
    @Test
    fun renderSpeciesStrips() {
        val cell = 300
        val labelH = 40
        text.textSize = 22f
        for ((plan, group) in Catalog.all.groupBy { it.look.plan }) {
            val sheet = Bitmap.createBitmap(cell * frames.size, (cell + labelH) * group.size, Bitmap.Config.ARGB_8888)
            val sc = Canvas(sheet)
            sc.drawColor(BG)
            group.forEachIndexed { row, sp ->
                val strip = Bitmap.createBitmap(cell * frames.size, cell + labelH, Bitmap.Config.ARGB_8888)
                val rc = Canvas(strip)
                rc.drawColor(BG)
                frames.forEachIndexed { i, f ->
                    drawPet(rc, sp, f, i * cell.toFloat(), labelH.toFloat(), cell)
                    rc.drawText(if (i == 0) "${sp.nameZh}  ${sp.latin}" else f.label, i * cell + 12f, 28f, text)
                }
                save(strip, "${sp.id}.png")
                sc.drawBitmap(strip, 0f, row * (cell + labelH).toFloat(), null)
                strip.recycle()
            }
            save(sheet, "_plan_${plan.name.lowercase()}.png")
            sheet.recycle()
        }
    }

    /** Every species idle in a grid: the at-a-glance overview. */
    @Test
    fun renderOverview() {
        val cell = 220
        val labelH = 30
        val cols = 9
        text.textSize = 20f
        val all = Catalog.all
        val rows = (all.size + cols - 1) / cols
        val bmp = Bitmap.createBitmap(cell * cols, (cell + labelH) * rows, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(BG)
        all.forEachIndexed { i, sp ->
            val x = (i % cols) * cell.toFloat()
            val y = (i / cols) * (cell + labelH).toFloat()
            drawPet(c, sp, frames[0], x, y + labelH, cell)
            c.drawText(sp.nameZh, x + 10f, y + 24f, text)
        }
        save(bmp, "_overview.png")
    }

    @Test
    fun renderVariants() {
        val cell = 300
        val labelH = 40
        text.textSize = 22f
        val ids = listOf("tiger", "panda", "bee", "trex")
        val cols = listOf<Pair<String, Rarity?>>(
            "seed 1" to Rarity.NORMAL, "seed 2" to Rarity.NORMAL, "seed 3" to Rarity.NORMAL,
            "白化" to Rarity.ALBINO, "金色" to Rarity.GOLDEN, "黑化" to Rarity.MELANISTIC,
        )
        val sheet = Bitmap.createBitmap(cell * cols.size, (cell + labelH) * ids.size, Bitmap.Config.ARGB_8888)
        val sc = Canvas(sheet)
        sc.drawColor(BG)
        ids.forEachIndexed { row, id ->
            val sp = Catalog.get(id)
            cols.forEachIndexed { i, (label, rarity) ->
                val look = Variation.individualize(sp.look, (row * 10 + i + 1).toLong() * 7919L, rarity)
                val y = row * (cell + labelH).toFloat()
                drawPet(sc, sp, frames[0], i * cell.toFloat(), y + labelH, cell, look)
                sc.drawText("${sp.nameZh} · $label", i * cell + 12f, y + 28f, text)
            }
        }
        save(sheet, "_variants.png")
    }

    /** Does the halo help pale pets on pale wallpapers without looking heavy on dark ones? */
    @Test
    fun renderHaloComparison() {
        val cell = 220
        val ids = listOf("polar_bear", "chicken", "penguin", "moon_jelly", "swallowtail")
        val bgs = listOf(Color.WHITE, Color.rgb(226, 232, 240), Color.rgb(34, 38, 48))
        val bmp = Bitmap.createBitmap(cell * bgs.size * 2, cell * ids.size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val halo = io.github.fyy0619cell.deskpet.render.Halo()
        val p = Paint()
        ids.forEachIndexed { row, id ->
            val renderer = Renderers.create(Catalog.get(id).look)
            for (col in 0 until bgs.size * 2) {
                val x = col * cell.toFloat()
                val y = row * cell.toFloat()
                p.color = bgs[col / 2]
                c.drawRect(x, y, x + cell, y + cell, p)
                pose.action = Action.IDLE; pose.time = 0.6f; pose.blink = 0f
                c.save()
                c.translate(x, y)
                c.scale(cell / 200f, cell / 200f)
                if (col % 2 == 1) halo.draw(c, renderer, pose)
                renderer.draw(c, pose)
                c.restore()
            }
        }
        save(bmp, "_halo.png")
    }

    companion object {
        val BG = Color.rgb(246, 244, 241)
    }
}
