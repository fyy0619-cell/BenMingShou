package io.github.fyy0619cell.deskpet.render

enum class BodyPlan { QUADRUPED, THEROPOD, INSECT }

enum class Ear { NONE, POINTY, ROUND, LONG, FLOPPY, BIG_FAN }
enum class Tail { NONE, THIN, BUSHY, SHORT, TUFT, THICK }
enum class Pattern { NONE, STRIPES, SPOTS, PANDA }

enum class Feature {
    TRUNK, TUSKS, SHAGGY_FUR, MANE, ARMS_TINY, BACK_SPIKES, FEATHERS, TEETH,
    COMPOUND_EYES, CLEAR_WINGS, STINGER, ANTENNAE, FUZZY,
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
