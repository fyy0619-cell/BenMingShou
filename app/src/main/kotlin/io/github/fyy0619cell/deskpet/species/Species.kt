package io.github.fyy0619cell.deskpet.species

import io.github.fyy0619cell.deskpet.render.Look

data class Species(
    val id: String,
    val nameZh: String,
    val latin: String,
    val era: String,
    val look: Look,
)
