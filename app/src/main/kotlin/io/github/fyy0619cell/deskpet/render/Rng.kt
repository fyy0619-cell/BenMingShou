package io.github.fyy0619cell.deskpet.render

/** SplitMix64: stable across Kotlin versions, unlike kotlin.random with a seed. */
class Rng(seed: Long) {
    private var state = seed

    fun nextLong(): Long {
        state += -0x61c8864680b583ebL
        var z = state
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
        return z xor (z ushr 31)
    }

    fun nextFloat(): Float = (nextLong() ushr 40).toFloat() / (1L shl 24).toFloat()

    fun range(min: Float, max: Float): Float = min + (max - min) * nextFloat()

    fun chance(p: Float): Boolean = nextFloat() < p
}
