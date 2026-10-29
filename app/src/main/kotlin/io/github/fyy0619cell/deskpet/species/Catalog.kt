package io.github.fyy0619cell.deskpet.species

import io.github.fyy0619cell.deskpet.render.BodyPlan.INSECT
import io.github.fyy0619cell.deskpet.render.BodyPlan.QUADRUPED
import io.github.fyy0619cell.deskpet.render.BodyPlan.THEROPOD
import io.github.fyy0619cell.deskpet.render.Ear
import io.github.fyy0619cell.deskpet.render.Feature
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.render.Palette
import io.github.fyy0619cell.deskpet.render.Pattern
import io.github.fyy0619cell.deskpet.render.Tail

private fun c(hex: Long) = hex.toInt()

object Catalog {
    val all: List<Species> = listOf(
        Species(
            "tiger", "老虎", "Panthera tigris", "现代",
            Look(
                QUADRUPED, Palette(c(0xFFE38B45), c(0xFFF7EEE2), c(0xFF3A2A2C)),
                ear = Ear.ROUND, tail = Tail.THIN, pattern = Pattern.STRIPES,
                bodyLength = 1.08f, snout = 1.05f,
            ),
        ),
        Species(
            "panda", "大熊猫", "Ailuropoda melanoleuca", "现代",
            Look(
                QUADRUPED, Palette(c(0xFFEDE8DF), c(0xFFFAF8F4), c(0xFF2F2B31)),
                ear = Ear.ROUND, tail = Tail.SHORT, pattern = Pattern.PANDA,
                bodyLength = 1.05f, bodyHeight = 1.18f, legLength = 0.82f, headSize = 1.08f, snout = 0.9f,
            ),
        ),
        Species(
            "cat", "猫", "Felis catus", "现代",
            Look(
                QUADRUPED, Palette(c(0xFFC4B29A), c(0xFFF4ECE0), c(0xFF8C7660)),
                ear = Ear.POINTY, tail = Tail.THIN, pattern = Pattern.STRIPES,
                bodyLength = 0.95f, bodyHeight = 0.92f, snout = 0.85f,
            ),
        ),
        Species(
            "mammoth", "猛犸象", "Mammuthus primigenius", "更新世 · 约 1 万年前",
            Look(
                QUADRUPED, Palette(c(0xFF8E6B55), c(0xFFA98C76), c(0xFF6B4F40)),
                ear = Ear.ROUND, tail = Tail.SHORT,
                features = setOf(Feature.TRUNK, Feature.TUSKS, Feature.SHAGGY_FUR),
                bodyLength = 1.15f, bodyHeight = 1.25f, legLength = 1.0f, headSize = 1.05f,
            ),
        ),
        Species(
            "trex", "霸王龙", "Tyrannosaurus rex", "白垩纪晚期 · 约 6800 万年前",
            Look(
                THEROPOD, Palette(c(0xFF6F9A6C), c(0xFFDCE5CB), c(0xFF4E7550)),
                pattern = Pattern.STRIPES, features = setOf(Feature.ARMS_TINY, Feature.TEETH),
                headSize = 1.12f, snout = 1.05f,
            ),
        ),
        Species(
            "raptor", "迅猛龙", "Velociraptor mongoliensis", "白垩纪晚期 · 约 7500 万年前",
            Look(
                THEROPOD, Palette(c(0xFFC48F62), c(0xFFF0DFC8), c(0xFF5B7FA3)),
                features = setOf(Feature.ARMS_TINY, Feature.FEATHERS),
                bodyLength = 0.9f, bodyHeight = 0.85f, headSize = 0.9f, snout = 1.15f, legLength = 1.1f,
            ),
        ),
        Species(
            "fly", "苍蝇", "Musca domestica", "现代",
            Look(
                INSECT, Palette(c(0xFF4A525C), c(0xFF6B737D), c(0xFF4F7C6E), dark = c(0xFF2C3036), eye = c(0xFFB9473F)),
                features = setOf(Feature.COMPOUND_EYES, Feature.CLEAR_WINGS),
                bodyLength = 0.9f, bodyHeight = 0.95f,
            ),
        ),
        Species(
            "bee", "蜜蜂", "Apis mellifera", "现代",
            Look(
                INSECT, Palette(c(0xFFB8864A), c(0xFFF5D27A), c(0xFFF2C14E), dark = c(0xFF3A2F2B)),
                pattern = Pattern.STRIPES,
                features = setOf(Feature.CLEAR_WINGS, Feature.STINGER, Feature.ANTENNAE, Feature.FUZZY),
                headSize = 1.05f,
            ),
        ),
    )
}
