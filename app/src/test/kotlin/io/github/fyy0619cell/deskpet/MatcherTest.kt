package io.github.fyy0619cell.deskpet

import io.github.fyy0619cell.deskpet.quiz.Matcher
import io.github.fyy0619cell.deskpet.quiz.Questions
import io.github.fyy0619cell.deskpet.species.Catalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatcherTest {
    private val n = Questions.combinations
    private val counts: Map<String, Int> by lazy {
        (0 until n).groupingBy { Matcher.match(Matcher.decode(it)).id }.eachCount()
    }

    @Test
    fun everySpeciesIsReachable() {
        val missing = Catalog.all.map { it.id }.filter { it !in counts }
        assertTrue("unreachable: $missing", missing.isEmpty())
    }

    @Test
    fun noSpeciesDominates() {
        val fair = n.toDouble() / Catalog.all.size
        val (id, max) = counts.maxBy { it.value }
        val (minId, min) = counts.minBy { it.value }
        println("fair=%.1f max=%s:%d min=%s:%d".format(fair, id, max, minId, min))
        assertTrue("$id wins $max of $n", max <= fair * 2)
        assertTrue("$minId wins only $min of $n", min >= fair * 0.5)
    }

    @Test
    fun matchIsDeterministic() {
        val answers = intArrayOf(0, 1, 2, 3, 0, 1)
        assertEquals(Matcher.match(answers).id, Matcher.match(answers.copyOf()).id)
    }

    @Test
    fun choosingDinosaurEraLeansPrimeval() {
        val dinoPicks = (0 until n).map(Matcher::decode).filter { it[3] == 0 }
        val ancient = dinoPicks.count { Matcher.match(it).traits.ancient >= 1 }
        val share = ancient.toDouble() / dinoPicks.size
        println("dinosaur-era answers -> primeval species: %.0f%%".format(share * 100))
        assertTrue(share > 0.6)
    }

    @Test
    fun everyResultExplainsItself() {
        for (k in 0 until n step 37) {
            val answers = Matcher.decode(k)
            val lines = Matcher.commonGround(answers, Matcher.match(answers))
            assertTrue(lines.isNotEmpty() && lines.size <= 3)
        }
    }

    @Test
    fun encodeDecodeRoundTrips() {
        for (k in 0 until n) assertEquals(k, Matcher.encode(Matcher.decode(k)))
    }

    /** Balance must not come at the cost of fit: most people should get a top-5 animal. */
    @Test
    fun assignedSpeciesUsuallyFitsWell() {
        var top5 = 0
        for (k in 0 until n) {
            val answers = Matcher.decode(k)
            val p = Matcher.profile(answers)
            val ranked = Catalog.all.sortedByDescending { sp ->
                sp.traits.toArray().indices.sumOf { p.axes[it] * sp.traits.toArray()[it] } +
                    (if (p.habitat == sp.habitat) 3 else 0)
            }.take(5).map { it.id }
            if (Matcher.match(answers).id in ranked) top5++
        }
        val share = top5.toDouble() / n
        println("assigned within score top-5: %.0f%%".format(share * 100))
        assertTrue(share > 0.5)
    }
}
