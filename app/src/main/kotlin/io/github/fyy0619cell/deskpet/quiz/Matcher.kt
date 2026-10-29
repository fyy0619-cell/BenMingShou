package io.github.fyy0619cell.deskpet.quiz

import io.github.fyy0619cell.deskpet.species.Catalog
import io.github.fyy0619cell.deskpet.species.Habitat
import io.github.fyy0619cell.deskpet.species.Species

/**
 * Every answer combination is assigned a species up front.
 * score = user·traits + habitat bonus (all integers). Pairs are taken in descending score
 * order and a species stops accepting once it holds its fair share, so every species is
 * reachable and none dominates. Equal scores are ordered by a fixed hash, not catalog order.
 */
object Matcher {
    private const val HABITAT_BONUS = 3
    private const val AXES = 6

    private val species = Catalog.all
    private val traits: Array<IntArray> = Array(species.size) { species[it].traits.toArray() }
    private val radices = IntArray(Questions.all.size) { Questions.all[it].options.size }

    class Profile(val axes: IntArray, val habitat: Habitat?)

    fun profile(answers: IntArray): Profile {
        require(answers.size == Questions.all.size)
        val u = IntArray(AXES)
        var habitat: Habitat? = null
        answers.forEachIndexed { q, a ->
            val opt = Questions.all[q].options[a]
            for (i in 0 until AXES) u[i] += opt.delta[i]
            if (opt.habitat != null) habitat = opt.habitat
        }
        return Profile(u, habitat)
    }

    private fun score(p: Profile, j: Int): Int {
        var s = 0
        val t = traits[j]
        for (i in 0 until AXES) s += p.axes[i] * t[i]
        if (p.habitat != null && species[j].habitat == p.habitat) s += HABITAT_BONUS
        return s
    }

    fun encode(answers: IntArray): Int {
        var k = 0
        for (q in radices.indices.reversed()) k = k * radices[q] + answers[q]
        return k
    }

    fun decode(index: Int): IntArray {
        var k = index
        return IntArray(radices.size) { q -> (k % radices[q]).also { k /= radices[q] } }
    }

    /** Combination index -> species index. Computed once (~300k integer scores). */
    val assignment: IntArray by lazy { assign() }

    fun match(answers: IntArray): Species = species[assignment[encode(answers)]]

    private fun assign(): IntArray {
        val n = Questions.combinations
        val m = species.size
        val scores = IntArray(n * m)
        var lo = Int.MAX_VALUE
        var hi = Int.MIN_VALUE
        for (k in 0 until n) {
            val p = profile(decode(k))
            for (j in 0 until m) {
                val s = score(p, j)
                scores[k * m + j] = s
                if (s < lo) lo = s
                if (s > hi) hi = s
            }
        }
        // Bucket pairs by score, then order each bucket by a fixed hash so ties are fair.
        val buckets = Array(hi - lo + 1) { ArrayList<Int>() }
        for (idx in scores.indices) buckets[scores[idx] - lo].add(idx)
        val cap = (n + m - 1) / m
        val load = IntArray(m)
        val result = IntArray(n) { -1 }
        var assigned = 0
        for (b in buckets.indices.reversed()) {
            val bucket = buckets[b]
            bucket.sortWith(compareBy { mix(it.toLong()) })
            for (idx in bucket) {
                val k = idx / m
                val j = idx % m
                if (result[k] == -1 && load[j] < cap) {
                    result[k] = j
                    load[j]++
                    if (++assigned == n) return result
                }
            }
        }
        return result
    }

    private fun mix(x: Long): Long {
        var z = x * -0x61c8864680b583ebL
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        return z xor (z ushr 31)
    }

    /** Up to three short lines on what the user and their animal have in common. */
    fun commonGround(answers: IntArray, sp: Species): List<String> {
        val p = profile(answers)
        val t = sp.traits.toArray()
        val lines = (0 until AXES)
            .map { i -> i to p.axes[i] * t[i] }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { (i, _) -> AXIS_LINES[i][if (t[i] > 0) 0 else 1] }
            .toMutableList()
        if (p.habitat == sp.habitat) lines.add(minOf(lines.size, 1), HABITAT_LINES.getValue(sp.habitat))
        if (lines.isEmpty()) {
            val strongest = t.indices.maxBy { kotlin.math.abs(t[it]) }
            lines.add(AXIS_LINES[strongest][if (t[strongest] >= 0) 0 else 1])
        }
        return lines.take(3)
    }

    private val AXIS_LINES = arrayOf(
        arrayOf("都精力充沛，闲不下来", "都懂得慢下来，享受安静"),
        arrayOf("都喜欢热闹，身边总有伙伴", "都享受独处，一个人也很自在"),
        arrayOf("都胆子大，遇事敢往前冲", "都细腻谨慎，从不冒失"),
        arrayOf("都好奇心旺盛，什么都想看看", "都踏实专注，认准了就不变"),
        arrayOf("都是夜猫子，越晚越精神", "都是早起派，作息规律"),
        arrayOf("骨子里都有种古老的气质", "都活在当下，喜欢新鲜事物"),
    )

    private val HABITAT_LINES = mapOf(
        Habitat.WATER to "都向往大海",
        Habitat.AIR to "都向往天空",
        Habitat.LAND to "都脚踏实地，热爱大地",
    )
}
