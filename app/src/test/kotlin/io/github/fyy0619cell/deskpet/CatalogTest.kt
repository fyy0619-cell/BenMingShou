package io.github.fyy0619cell.deskpet

import io.github.fyy0619cell.deskpet.render.BodyPlan
import io.github.fyy0619cell.deskpet.render.Rarity
import io.github.fyy0619cell.deskpet.render.Rng
import io.github.fyy0619cell.deskpet.render.Variation
import io.github.fyy0619cell.deskpet.species.Catalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CatalogTest {
    @Test
    fun idsAreUnique() {
        val ids = Catalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun catalogIsLargeAndCoversEveryBodyPlan() {
        assertTrue(Catalog.all.size >= 65)
        val plans = Catalog.all.map { it.look.plan }.toSet()
        assertEquals(BodyPlan.entries.toSet(), plans)
    }

    @Test
    fun textFieldsAndTraitsAreValid() {
        for (sp in Catalog.all) {
            assertTrue(sp.id, sp.nameZh.isNotBlank() && sp.latin.isNotBlank() && sp.era.isNotBlank() && sp.food.isNotBlank())
            assertTrue(sp.id, sp.traits.toArray().all { it in -2..2 })
        }
    }

    @Test
    fun splitMix64MatchesReferenceOutput() {
        assertEquals(-0x1ddf57c684e23251L, Rng(0L).nextLong())
    }

    @Test
    fun individualIsDeterministicForSeed() {
        val sp = Catalog.get("tiger")
        assertEquals(Variation.individualize(sp.look, 42L), Variation.individualize(sp.look, 42L))
    }

    @Test
    fun rareChanceIsRoughlyOnePercent() {
        val sp = Catalog.get("tiger")
        val n = 20_000
        val rare = (0 until n).count { Variation.individualize(sp.look, it * 2654435761L).rare != Rarity.NORMAL }
        assertTrue("rare=$rare", rare in 120..300)
    }
}
