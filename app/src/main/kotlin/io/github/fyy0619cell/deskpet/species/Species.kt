package io.github.fyy0619cell.deskpet.species

import io.github.fyy0619cell.deskpet.render.Look

enum class Habitat { LAND, WATER, AIR }

enum class Voice { MEOW, ROAR, GROWL, BARK, TRUMPET, SQUEAK, DINO, CHIRP, HOOT, HONK, BUZZ, CLICK, BUBBLE, WHALE, CROAK, HISS }

/**
 * Personality axes used by the quiz, each -2..2.
 * energy: calm..lively, social: solitary..sociable, bold: timid..brave,
 * curious: steady..curious, night: day..night owl, ancient: modern..primeval.
 */
data class Traits(val energy: Int, val social: Int, val bold: Int, val curious: Int, val night: Int, val ancient: Int) {
    fun toArray() = intArrayOf(energy, social, bold, curious, night, ancient)
}

data class Species(
    val id: String,
    val nameZh: String,
    val latin: String,
    val era: String,
    val habitat: Habitat,
    val traits: Traits,
    val food: String,
    val voice: Voice,
    val look: Look,
)
