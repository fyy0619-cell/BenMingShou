package io.github.fyy0619cell.deskpet.render

/** Turns a species' canonical look into one individual, fully determined by [seed]. */
object Variation {
    const val RARE_CHANCE = 0.01f

    fun individualize(look: Look, seed: Long, forceRarity: Rarity? = null): Look {
        val r = Rng(seed)
        val dh = r.range(-7f, 7f)
        val ds = r.range(-0.06f, 0.06f)
        val dl = r.range(-0.05f, 0.05f)
        val rarity = forceRarity ?: if (r.chance(RARE_CHANCE)) {
            Rarity.entries[1 + (r.nextFloat() * 3).toInt().coerceAtMost(2)]
        } else {
            Rarity.NORMAL
        }
        val pal = look.palette
        val palette = when (rarity) {
            Rarity.NORMAL -> pal.copy(
                base = ColorX.shift(pal.base, dh, ds, dl),
                belly = ColorX.shift(pal.belly, dh * 0.5f, ds * 0.5f, dl * 0.4f),
                accent = ColorX.shift(pal.accent, dh, ds, dl * 0.6f),
            )
            Rarity.ALBINO -> pal.copy(
                base = 0xFFF4EFE9.toInt(), belly = 0xFFFFFCF8.toInt(),
                accent = 0xFFDCCACA.toInt(), eye = 0xFFC2505C.toInt(),
            )
            Rarity.GOLDEN -> pal.copy(
                base = 0xFFE3B64C.toInt(), belly = 0xFFF8E8B4.toInt(), accent = 0xFFB4842F.toInt(),
            )
            Rarity.MELANISTIC -> pal.copy(
                base = 0xFF3C3742.toInt(), belly = 0xFF5B5462.toInt(),
                accent = 0xFF25222B.toInt(), eye = 0xFFE7C35A.toInt(),
            )
        }
        return look.copy(
            palette = palette,
            bodyLength = look.bodyLength * r.range(0.95f, 1.05f),
            bodyHeight = look.bodyHeight * r.range(0.93f, 1.1f),
            headSize = look.headSize * r.range(0.96f, 1.05f),
            legLength = look.legLength * r.range(0.94f, 1.06f),
            patternSeed = seed,
            rare = rarity,
        )
    }
}
