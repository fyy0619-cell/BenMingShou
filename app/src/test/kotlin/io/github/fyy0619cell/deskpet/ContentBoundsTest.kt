package io.github.fyy0619cell.deskpet

import io.github.fyy0619cell.deskpet.render.ContentBounds
import io.github.fyy0619cell.deskpet.render.Renderers
import io.github.fyy0619cell.deskpet.species.Catalog
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ContentBoundsTest {
    @Test
    fun croppedWindowIsMeaningfullySmallerAndStillSane() {
        var total = 0f
        for (sp in Catalog.all) {
            val r = ContentBounds.measure(Renderers.create(sp.look))
            val share = r.width() * r.height() / 40_000f
            assertTrue("${sp.id} $r", r.left >= 0f && r.top >= 0f && r.right <= 200f && r.bottom <= 200f)
            assertTrue("${sp.id} is suspiciously small: $r", r.width() > 40f && r.height() > 30f)
            total += share
        }
        val avg = total / Catalog.all.size
        println("average window area after cropping: %.0f%% of the square".format(avg * 100))
        assertTrue(avg < 0.8f)
    }
}
