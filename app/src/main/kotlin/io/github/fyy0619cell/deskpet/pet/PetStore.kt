package io.github.fyy0619cell.deskpet.pet

import android.content.Context
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.render.Variation
import io.github.fyy0619cell.deskpet.species.Catalog
import io.github.fyy0619cell.deskpet.species.Species

data class Adoption(
    val speciesId: String,
    val seed: Long,
    val name: String,
    val adoptedAt: Long,
    val answers: String,
) {
    val species: Species get() = Catalog.get(speciesId)
    val look: Look get() = Variation.individualize(species.look, seed)
}

/** The adoption is written once and never overwritten: one pet per person. */
class PetStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("pet", Context.MODE_PRIVATE)

    fun adoption(): Adoption? {
        val id = prefs.getString(KEY_SPECIES, null) ?: return null
        return Adoption(
            speciesId = id,
            seed = prefs.getLong(KEY_SEED, 0L),
            name = prefs.getString(KEY_NAME, "") ?: "",
            adoptedAt = prefs.getLong(KEY_AT, 0L),
            answers = prefs.getString(KEY_ANSWERS, "") ?: "",
        )
    }

    /** Returns false if a pet already exists; the first adoption always wins. */
    fun adopt(a: Adoption): Boolean {
        if (prefs.contains(KEY_SPECIES)) return false
        return prefs.edit()
            .putString(KEY_SPECIES, a.speciesId)
            .putLong(KEY_SEED, a.seed)
            .putString(KEY_NAME, a.name)
            .putLong(KEY_AT, a.adoptedAt)
            .putString(KEY_ANSWERS, a.answers)
            .commit()
    }

    private companion object {
        const val KEY_SPECIES = "species"
        const val KEY_SEED = "seed"
        const val KEY_NAME = "name"
        const val KEY_AT = "adopted_at"
        const val KEY_ANSWERS = "answers"
    }
}
