package io.github.fyy0619cell.deskpet.promo

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import io.github.fyy0619cell.deskpet.R
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.render.Pose
import io.github.fyy0619cell.deskpet.render.Rarity
import io.github.fyy0619cell.deskpet.render.Renderers
import io.github.fyy0619cell.deskpet.render.Variation
import io.github.fyy0619cell.deskpet.species.Catalog
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Exports transparent animation frames for the promo video.
 * Skipped unless run with -PpromoDir=<folder>.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PromoSpritesTest {
    private val px = 600
    private val fps = 30

    private fun out(): File? = System.getProperty("promo.dir")?.takeIf { it.isNotBlank() }?.let { File(it, "sprites") }

    private fun export(dir: File, name: String, look: Look, frames: Int, pose: (Int, Pose) -> Unit) {
        val d = File(dir, name).apply { mkdirs() }
        val renderer = Renderers.create(look)
        val p = Pose()
        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        for (i in 0 until frames) {
            bmp.eraseColor(0)
            p.blink = 0f
            pose(i, p)
            c.save()
            c.scale(px / 200f, px / 200f)
            renderer.draw(c, p)
            c.restore()
            File(d, "%03d.png".format(i)).outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun idle(i: Int, p: Pose) { p.action = Action.IDLE; p.time = i / 30f }

    @Test
    fun exportPromoSprites() {
        val dir = out()
        assumeTrue("set -PpromoDir to export promo sprites", dir != null)
        dir!!.mkdirs()
        val hook = listOf("trex", "owl", "moon_jelly", "bee", "giraffe", "octopus", "swallowtail", "triceratops",
            "penguin", "crab", "blue_whale", "frog", "stegosaurus", "flamingo", "snail", "tiger")
        for (id in hook) export(dir, "sil_$id", Catalog.get(id).look, 1) { i, p -> idle(i, p) }

        val montage = listOf("trex", "panda", "fly", "trilobite", "blue_whale", "dodo", "octopus", "mammoth")
        for (id in montage) {
            export(dir, "m_$id", Catalog.get(id).look, fps) { i, p ->
                p.action = Action.HAPPY; p.progress = i / fps.toFloat(); p.time = i / fps.toFloat()
            }
        }
        val trex = Variation.individualize(Catalog.get("trex").look, 7L, Rarity.NORMAL)
        export(dir, "reveal_trex", trex, 90) { i, p ->
            if (i < 30) { p.action = Action.HAPPY; p.progress = i / 30f } else p.action = Action.IDLE
            p.time = i / 30f
        }
        export(dir, "walk_trex", trex, 90) { i, p ->
            p.action = Action.WALK; p.walkPhase = (i / 30f * 1.6f) % 1f; p.time = i / 30f
        }
        export(dir, "fly_bee", Catalog.get("bee").look, 90) { i, p ->
            p.action = Action.WALK; p.walkPhase = (i / 30f) % 1f; p.time = i / 30f
        }
        for (r in listOf(Rarity.ALBINO, Rarity.GOLDEN, Rarity.MELANISTIC)) {
            export(dir, "rare_${r.name.lowercase()}", Variation.individualize(Catalog.get("tiger").look, 11L, r), 20) { i, p -> idle(i, p) }
        }

        val meta = StringBuilder("{\n")
        (hook + montage + listOf("tiger")).distinct().forEachIndexed { n, id ->
            val sp = Catalog.get(id)
            val idx = Catalog.all.indexOf(sp) + 1
            if (n > 0) meta.append(",\n")
            meta.append("  \"$id\": {\"name\": \"${sp.nameZh}\", \"latin\": \"${sp.latin}\", \"era\": \"${sp.era}\", ")
            meta.append("\"base\": \"#%06X\", \"no\": $idx}".format(sp.look.palette.base and 0xFFFFFF))
        }
        meta.append("\n}\n")
        File(dir, "meta.json").writeText(meta.toString())

        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val icon = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        ctx.getDrawable(R.drawable.ic_launcher_foreground)!!.apply { setBounds(0, 0, px, px); draw(Canvas(icon)) }
        File(dir, "icon_fg.png").outputStream().use { icon.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
