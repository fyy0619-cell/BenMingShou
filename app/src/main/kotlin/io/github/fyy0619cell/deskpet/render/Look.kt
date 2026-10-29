package io.github.fyy0619cell.deskpet.render

enum class BodyPlan { QUADRUPED, THEROPOD, REPTILE, BIRD, INSECT, ARTHROPOD, FISH, MOLLUSC, AMPHIBIAN, SERPENT }

enum class Locomotion { WALK, FLY, SWIM, SLITHER, HOP }

enum class Ear { NONE, POINTY, ROUND, LONG, FLOPPY, BIG_FAN }
enum class Tail { NONE, THIN, BUSHY, SHORT, TUFT, THICK }
enum class Pattern { NONE, STRIPES, SPOTS, PANDA, BANDS, PATCHES }

enum class Feature {
    // mammals
    TRUNK, TUSKS, SHAGGY_FUR, MANE, TEETH, HORNS, ANTLERS, LONG_NECK, MASK, TAIL_RINGS, PIG_SNOUT, POUCH_STRIPES,
    // dinosaurs and reptiles
    ARMS_TINY, BACK_SPIKES, FEATHERS, SAIL, CREST, FRILL_HORNS, BACK_PLATES, TAIL_SPIKES, ARMOR, CLUB_TAIL,
    SHELL, CURLED_TAIL, LONG_SNOUT, EYE_TURRET,
    // birds
    BEAK_HOOK, BEAK_LONG, BEAK_FLAT, FLIGHTLESS, LONG_LEGS, EAR_TUFTS, LONG_TAIL, MEMBRANE_WINGS, COMB, UPRIGHT,
    // insects and other arthropods
    COMPOUND_EYES, CLEAR_WINGS, STINGER, ANTENNAE, FUZZY, PROBOSCIS, BIG_WINGS, ELYTRA, HORN, LONG_ABDOMEN,
    EIGHT_LEGS, CLAWS, EYE_STALKS, CURLED_STINGER, SEGMENTS,
    // aquatic
    TALL_FIN, FLUKES, HEAD_ARMOR, LOBE_FINS, SPINES, BEAK_SNOUT, LURE, ROUND_BODY,
    TENTACLES, BELL, SPIRAL_SHELL, FOOT,
    // amphibians and serpents
    GILLS, BIG_HIND_LEGS, PROLEGS, TONGUE,
}

data class Palette(
    val base: Int,
    val belly: Int,
    val accent: Int,
    val dark: Int = 0xFF2B2233.toInt(),
    val eye: Int = 0xFF2B2233.toInt(),
)

data class Look(
    val plan: BodyPlan,
    val palette: Palette,
    val ear: Ear = Ear.NONE,
    val tail: Tail = Tail.NONE,
    val pattern: Pattern = Pattern.NONE,
    val features: Set<Feature> = emptySet(),
    val bodyLength: Float = 1f,
    val bodyHeight: Float = 1f,
    val legLength: Float = 1f,
    val headSize: Float = 1f,
    val snout: Float = 1f,
    val patternSeed: Long = 1L,
    val rare: Rarity = Rarity.NORMAL,
) {
    fun has(f: Feature) = f in features

    val locomotion: Locomotion
        get() = when (plan) {
            BodyPlan.BIRD -> if (has(Feature.FLIGHTLESS)) Locomotion.WALK else Locomotion.FLY
            BodyPlan.INSECT -> if (has(Feature.CLEAR_WINGS) || has(Feature.BIG_WINGS) || has(Feature.ELYTRA)) Locomotion.FLY else Locomotion.WALK
            BodyPlan.FISH -> Locomotion.SWIM
            BodyPlan.MOLLUSC -> if (has(Feature.FOOT)) Locomotion.SLITHER else Locomotion.SWIM
            BodyPlan.SERPENT -> Locomotion.SLITHER
            BodyPlan.AMPHIBIAN -> if (has(Feature.BIG_HIND_LEGS)) Locomotion.HOP else Locomotion.WALK
            else -> Locomotion.WALK
        }
}

enum class Rarity { NORMAL, ALBINO, GOLDEN, MELANISTIC }

enum class Action { IDLE, WALK, SLEEP, HAPPY, EAT }

/** Mutable and reused every frame so drawing never allocates. */
class Pose {
    var action: Action = Action.IDLE
    var time: Float = 0f
    var walkPhase: Float = 0f
    var progress: Float = 0f
    var blink: Float = 0f
}
