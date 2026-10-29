package io.github.fyy0619cell.deskpet.species

import io.github.fyy0619cell.deskpet.render.BodyPlan
import io.github.fyy0619cell.deskpet.render.BodyPlan.AMPHIBIAN
import io.github.fyy0619cell.deskpet.render.BodyPlan.ARTHROPOD
import io.github.fyy0619cell.deskpet.render.BodyPlan.BIRD
import io.github.fyy0619cell.deskpet.render.BodyPlan.FISH
import io.github.fyy0619cell.deskpet.render.BodyPlan.INSECT
import io.github.fyy0619cell.deskpet.render.BodyPlan.MOLLUSC
import io.github.fyy0619cell.deskpet.render.BodyPlan.QUADRUPED
import io.github.fyy0619cell.deskpet.render.BodyPlan.REPTILE
import io.github.fyy0619cell.deskpet.render.BodyPlan.SERPENT
import io.github.fyy0619cell.deskpet.render.BodyPlan.THEROPOD
import io.github.fyy0619cell.deskpet.render.Ear
import io.github.fyy0619cell.deskpet.render.Feature
import io.github.fyy0619cell.deskpet.render.Feature.*
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.render.Palette
import io.github.fyy0619cell.deskpet.render.Pattern
import io.github.fyy0619cell.deskpet.render.Tail
import io.github.fyy0619cell.deskpet.species.Habitat.AIR
import io.github.fyy0619cell.deskpet.species.Habitat.LAND
import io.github.fyy0619cell.deskpet.species.Habitat.WATER
import io.github.fyy0619cell.deskpet.species.Voice.*

private const val MODERN = "现代"

private fun pal(base: Long, belly: Long, accent: Long, dark: Long = 0xFF2B2233, eye: Long = 0xFF2B2233) =
    Palette(base.toInt(), belly.toInt(), accent.toInt(), dark.toInt(), eye.toInt())

private fun tr(energy: Int, social: Int, bold: Int, curious: Int, night: Int, ancient: Int) =
    Traits(energy, social, bold, curious, night, ancient)

private fun look(
    plan: BodyPlan, palette: Palette, vararg features: Feature,
    ear: Ear = Ear.NONE, tail: Tail = Tail.NONE, pattern: Pattern = Pattern.NONE,
    length: Float = 1f, height: Float = 1f, legs: Float = 1f, head: Float = 1f, snout: Float = 1f,
) = Look(plan, palette, ear, tail, pattern, features.toSet(), length, height, legs, head, snout)

object Catalog {
    val all: List<Species> = listOf(
        // ---- four-legged mammals ----
        Species("tiger", "老虎", "Panthera tigris", MODERN, LAND, tr(1, -2, 2, 0, 1, -1), "肉排", ROAR,
            look(QUADRUPED, pal(0xFFE38B45, 0xFFF7EEE2, 0xFF3A2A2C), ear = Ear.ROUND, tail = Tail.THIN, pattern = Pattern.STRIPES, length = 1.08f, snout = 1.05f)),
        Species("panda", "大熊猫", "Ailuropoda melanoleuca", MODERN, LAND, tr(-2, -1, -1, 0, -1, -1), "竹子", SQUEAK,
            look(QUADRUPED, pal(0xFFEDE8DF, 0xFFFAF8F4, 0xFF2F2B31), ear = Ear.ROUND, tail = Tail.SHORT, pattern = Pattern.PANDA, length = 1.05f, height = 1.18f, legs = 0.82f, head = 1.08f, snout = 0.9f)),
        Species("cat", "猫", "Felis catus", MODERN, LAND, tr(0, -1, 0, 2, 1, -2), "小鱼干", MEOW,
            look(QUADRUPED, pal(0xFFC4B29A, 0xFFF4ECE0, 0xFF8C7660), ear = Ear.POINTY, tail = Tail.THIN, pattern = Pattern.STRIPES, length = 0.95f, height = 0.92f, snout = 0.85f)),
        Species("mammoth", "猛犸象", "Mammuthus primigenius", "冰河时代 · 约 4000 年前灭绝", LAND, tr(-1, 2, 1, -1, -1, 2), "苔草", TRUMPET,
            look(QUADRUPED, pal(0xFF8E6B55, 0xFFA98C76, 0xFF6B4F40), TRUNK, TUSKS, SHAGGY_FUR, ear = Ear.ROUND, tail = Tail.SHORT, length = 1.15f, height = 1.25f, head = 1.05f)),
        Species("lion", "狮子", "Panthera leo", MODERN, LAND, tr(-1, 2, 2, -1, 0, -1), "肉排", ROAR,
            look(QUADRUPED, pal(0xFFD9A65E, 0xFFF3E3C4, 0xFFA8693A), MANE, ear = Ear.ROUND, tail = Tail.TUFT, length = 1.05f)),
        Species("fox", "赤狐", "Vulpes vulpes", MODERN, LAND, tr(1, -1, 0, 2, 2, -1), "浆果", SQUEAK,
            look(QUADRUPED, pal(0xFFE07B3C, 0xFFFBF1E6, 0xFFFBF1E6), ear = Ear.POINTY, tail = Tail.BUSHY, legs = 0.95f, snout = 1.25f)),
        Species("wolf", "狼", "Canis lupus", MODERN, LAND, tr(2, 2, 1, 0, 1, -1), "肉排", GROWL,
            look(QUADRUPED, pal(0xFF8E949C, 0xFFE9E6E1, 0xFFE9E6E1), ear = Ear.POINTY, tail = Tail.BUSHY, length = 1.05f, legs = 1.1f, snout = 1.2f)),
        Species("shiba", "柴犬", "Canis familiaris", MODERN, LAND, tr(2, 2, 0, 1, -1, -2), "骨头", BARK,
            look(QUADRUPED, pal(0xFFD9944F, 0xFFFBF0E2, 0xFFFBF0E2), ear = Ear.POINTY, tail = Tail.BUSHY, snout = 1.1f)),
        Species("rabbit", "兔子", "Oryctolagus cuniculus", MODERN, LAND, tr(1, 1, -2, 1, 1, -2), "胡萝卜", SQUEAK,
            look(QUADRUPED, pal(0xFFD8CFC6, 0xFFFBF7F2, 0xFFF2B8C6), ear = Ear.LONG, tail = Tail.SHORT, height = 1.1f, legs = 0.7f, head = 1.05f, snout = 0.75f)),
        Species("elephant", "亚洲象", "Elephas maximus", MODERN, LAND, tr(-1, 2, 0, 0, -1, -1), "香蕉", TRUMPET,
            look(QUADRUPED, pal(0xFF9CA3AE, 0xFFB7BDC6, 0xFF8A909A), TRUNK, ear = Ear.BIG_FAN, tail = Tail.THIN, length = 1.15f, height = 1.2f, legs = 1.1f)),
        Species("giraffe", "长颈鹿", "Giraffa camelopardalis", MODERN, LAND, tr(0, 1, -1, 1, -1, -1), "金合欢叶", SQUEAK,
            look(QUADRUPED, pal(0xFFE8BE73, 0xFFF6E7C6, 0xFFB7703A), LONG_NECK, HORNS, ear = Ear.FLOPPY, tail = Tail.TUFT, pattern = Pattern.PATCHES, length = 0.95f, height = 0.95f, legs = 1.7f, head = 0.8f, snout = 1.2f)),
        Species("smilodon", "剑齿虎", "Smilodon fatalis", "更新世 · 约 1 万年前灭绝", LAND, tr(0, 1, 2, -1, 1, 2), "肉排", ROAR,
            look(QUADRUPED, pal(0xFFC79A62, 0xFFF3E4CC, 0xFF8E6A44), TEETH, ear = Ear.ROUND, tail = Tail.SHORT, length = 1.05f, height = 1.05f, legs = 0.95f, snout = 1.05f)),
        Species("thylacine", "袋狼", "Thylacinus cynocephalus", "1936 年灭绝", LAND, tr(0, -2, -1, 1, 2, 1), "肉排", GROWL,
            look(QUADRUPED, pal(0xFFC9A37A, 0xFFF1E5D2, 0xFF4A3A32), POUCH_STRIPES, ear = Ear.POINTY, tail = Tail.THIN, pattern = Pattern.STRIPES, length = 1.15f, legs = 0.95f, snout = 1.3f)),
        Species("red_panda", "小熊猫", "Ailurus fulgens", MODERN, LAND, tr(0, -2, -1, 1, 1, -1), "竹叶", SQUEAK,
            look(QUADRUPED, pal(0xFFC4562E, 0xFFF2E6DA, 0xFF7A3B22), TAIL_RINGS, ear = Ear.POINTY, tail = Tail.BUSHY, legs = 0.8f, snout = 0.9f)),
        Species("raccoon", "浣熊", "Procyon lotor", MODERN, LAND, tr(1, 0, 1, 2, 2, -2), "水果", SQUEAK,
            look(QUADRUPED, pal(0xFF9C9590, 0xFFE8E4DF, 0xFF3B3639), TAIL_RINGS, MASK, ear = Ear.ROUND, tail = Tail.BUSHY)),
        Species("sika_deer", "梅花鹿", "Cervus nippon", MODERN, LAND, tr(0, 1, -2, 0, -1, -1), "嫩草", SQUEAK,
            look(QUADRUPED, pal(0xFFB97A4A, 0xFFF4E8D8, 0xFFF8F1E6), ANTLERS, ear = Ear.POINTY, tail = Tail.SHORT, pattern = Pattern.SPOTS, length = 0.95f, legs = 1.45f, head = 0.9f, snout = 1.15f)),
        Species("polar_bear", "北极熊", "Ursus maritimus", MODERN, LAND, tr(-1, -2, 2, 0, 0, -1), "鱼", GROWL,
            look(QUADRUPED, pal(0xFFF1EEE6, 0xFFFAF8F3, 0xFFD9D3C8), ear = Ear.ROUND, tail = Tail.SHORT, length = 1.12f, height = 1.15f, legs = 0.9f, snout = 1.15f)),
        Species("pig", "小猪", "Sus domesticus", MODERN, LAND, tr(-1, 1, 0, 1, -1, -2), "红薯", SQUEAK,
            look(QUADRUPED, pal(0xFFF2B8B0, 0xFFF9D8D2, 0xFFE79C93), PIG_SNOUT, ear = Ear.POINTY, tail = Tail.THIN, length = 1.05f, height = 1.12f, legs = 0.7f)),
        Species("koala", "考拉", "Phascolarctos cinereus", MODERN, LAND, tr(-2, -1, -1, -1, 1, -1), "桉树叶", GROWL,
            look(QUADRUPED, pal(0xFFA9A8AC, 0xFFE9E6E4, 0xFF8F8E93), ear = Ear.ROUND, height = 1.08f, legs = 0.75f, head = 1.15f, snout = 0.55f)),
        Species("hamster", "仓鼠", "Mesocricetus auratus", MODERN, LAND, tr(2, -1, -1, 2, 2, -2), "瓜子", SQUEAK,
            look(QUADRUPED, pal(0xFFE3B27B, 0xFFFBF1E3, 0xFFF7E2C6), ear = Ear.ROUND, length = 0.8f, height = 1.15f, legs = 0.45f, head = 1.08f, snout = 0.75f)),

        // ---- theropods ----
        Species("trex", "霸王龙", "Tyrannosaurus rex", "白垩纪晚期 · 约 6800 万年前", LAND, tr(1, -1, 2, 0, 0, 2), "肉排", DINO,
            look(THEROPOD, pal(0xFF6F9A6C, 0xFFDCE5CB, 0xFF4E7550), ARMS_TINY, TEETH, pattern = Pattern.STRIPES, head = 1.12f, snout = 1.05f)),
        Species("raptor", "迅猛龙", "Velociraptor mongoliensis", "白垩纪晚期 · 约 7500 万年前", LAND, tr(2, 1, 2, 2, 1, 2), "肉排", DINO,
            look(THEROPOD, pal(0xFFC48F62, 0xFFF0DFC8, 0xFF5B7FA3), ARMS_TINY, FEATHERS, LONG_SNOUT, length = 0.85f, height = 0.72f, legs = 1.15f, head = 0.78f)),
        Species("spinosaurus", "棘龙", "Spinosaurus aegyptiacus", "白垩纪中期 · 约 9500 万年前", WATER, tr(0, -1, 1, 1, 0, 2), "鱼", DINO,
            look(THEROPOD, pal(0xFF7E8F96, 0xFFDCE0D8, 0xFFC46A4C), SAIL, LONG_SNOUT, ARMS_TINY, length = 1.1f, head = 0.95f, snout = 1.1f)),
        Species("dilophosaurus", "双脊龙", "Dilophosaurus wetherilli", "侏罗纪早期 · 约 1.9 亿年前", LAND, tr(1, 0, 1, 1, 1, 2), "肉排", DINO,
            look(THEROPOD, pal(0xFF9AA65A, 0xFFEDEBD0, 0xFFD9673F), CREST, ARMS_TINY, pattern = Pattern.STRIPES, length = 0.95f, height = 0.85f, head = 0.88f, snout = 1.05f)),

        // ---- reptiles and four-legged dinosaurs ----
        Species("triceratops", "三角龙", "Triceratops horridus", "白垩纪晚期 · 约 6800 万年前", LAND, tr(-1, 2, 1, -1, -1, 2), "蕨类", DINO,
            look(REPTILE, pal(0xFF7C9A8E, 0xFFD9E2D6, 0xFFD9A35A), FRILL_HORNS, length = 1.1f, height = 1.15f, head = 1.15f)),
        Species("stegosaurus", "剑龙", "Stegosaurus stenops", "侏罗纪晚期 · 约 1.5 亿年前", LAND, tr(-2, 0, 0, -1, -1, 2), "蕨类", DINO,
            look(REPTILE, pal(0xFF8C9F6A, 0xFFE2E5CC, 0xFFD07A4C), BACK_PLATES, TAIL_SPIKES, length = 1.05f, height = 1.1f, legs = 1.1f, head = 0.7f)),
        Species("brachiosaurus", "腕龙", "Brachiosaurus altithorax", "侏罗纪晚期 · 约 1.5 亿年前", LAND, tr(-2, 1, -1, 1, -1, 2), "树叶", DINO,
            look(REPTILE, pal(0xFF8FA6B5, 0xFFDDE5EA, 0xFF6E8798), LONG_NECK, height = 1.2f, legs = 1.6f, head = 0.75f)),
        Species("ankylosaurus", "甲龙", "Ankylosaurus magniventris", "白垩纪晚期 · 约 6800 万年前", LAND, tr(-2, -1, 1, -2, 0, 2), "蕨类", DINO,
            look(REPTILE, pal(0xFFA08A6A, 0xFFE8DCC8, 0xFF6E5A44), ARMOR, CLUB_TAIL, length = 1.1f, height = 0.95f, legs = 0.8f, head = 0.9f)),
        Species("crocodile", "鳄鱼", "Crocodylus niloticus", MODERN, WATER, tr(-2, -1, 2, -2, 1, 1), "鱼", HISS,
            look(REPTILE, pal(0xFF6F8B5E, 0xFFD8DDB8, 0xFF4F6A42), LONG_SNOUT, length = 1.15f, height = 0.62f, legs = 0.55f, head = 0.95f)),
        Species("turtle", "乌龟", "Mauremys reevesii", MODERN, WATER, tr(-2, 0, -1, 0, -1, 1), "小虾", HISS,
            look(REPTILE, pal(0xFF8C9A6A, 0xFFE6E2C2, 0xFF7A6A4A), SHELL, length = 0.95f, height = 0.9f, legs = 0.55f, head = 0.8f)),
        Species("chameleon", "变色龙", "Chamaeleo calyptratus", MODERN, LAND, tr(-1, -2, -1, 2, -1, -1), "蟋蟀", HISS,
            look(REPTILE, pal(0xFF6FBF73, 0xFFE6F2C8, 0xFF3E9A6A), CURLED_TAIL, EYE_TURRET, CREST, length = 0.85f, height = 0.9f, legs = 0.9f)),

        // ---- birds and pterosaurs ----
        Species("owl", "雕鸮", "Bubo bubo", MODERN, AIR, tr(-1, -2, 0, 1, 2, -1), "老鼠", HOOT,
            look(BIRD, pal(0xFF9C7A5A, 0xFFE9D9C2, 0xFF7A5B42), EAR_TUFTS, UPRIGHT, height = 0.95f, legs = 0.6f, head = 1.25f)),
        Species("penguin", "帝企鹅", "Aptenodytes forsteri", MODERN, WATER, tr(0, 2, 0, 0, -1, -1), "磷虾", HONK,
            look(BIRD, pal(0xFF3D4552, 0xFFF1ECE2, 0xFF2E3540), UPRIGHT, FLIGHTLESS, length = 1.05f, height = 1.05f, legs = 0.4f)),
        Species("macaw", "金刚鹦鹉", "Ara macao", MODERN, AIR, tr(2, 2, 0, 2, -2, -1), "坚果", CHIRP,
            look(BIRD, pal(0xFFD9483B, 0xFFF2C14E, 0xFF3F7FC4), BEAK_HOOK, LONG_TAIL)),
        Species("golden_eagle", "金雕", "Aquila chrysaetos", MODERN, AIR, tr(1, -2, 2, 0, -2, -1), "鱼", CHIRP,
            look(BIRD, pal(0xFF8A5E3C, 0xFFC9A06E, 0xFF6E472C), BEAK_HOOK, length = 1.1f, head = 0.95f)),
        Species("dodo", "渡渡鸟", "Raphus cucullatus", "17 世纪灭绝", LAND, tr(-1, 1, -1, 1, -1, 1), "果实", HONK,
            look(BIRD, pal(0xFF9E9A94, 0xFFD8D3CC, 0xFFC9C4BA), BEAK_HOOK, FLIGHTLESS, length = 1.15f, height = 1.2f, legs = 0.8f, head = 1.35f)),
        Species("flamingo", "火烈鸟", "Phoenicopterus roseus", MODERN, WATER, tr(0, 2, -1, 0, -1, -1), "小虾", HONK,
            look(BIRD, pal(0xFFF2A3A8, 0xFFF8C9CB, 0xFFE07A82), BEAK_LONG, LONG_LEGS, LONG_NECK, length = 0.9f, legs = 3.0f, head = 0.8f)),
        Species("chicken", "小鸡", "Gallus gallus domesticus", MODERN, LAND, tr(1, 1, 0, 1, -2, -2), "谷粒", CHIRP,
            look(BIRD, pal(0xFFF4EEE4, 0xFFFBF8F2, 0xFFE9E0D2), COMB, FLIGHTLESS, head = 0.95f)),
        Species("pteranodon", "无齿翼龙", "Pteranodon longiceps", "白垩纪晚期 · 约 8500 万年前", AIR, tr(1, 1, 1, 1, 0, 2), "鱼", DINO,
            look(BIRD, pal(0xFFB8A99A, 0xFFE8E0D6, 0xFF8E7F72), MEMBRANE_WINGS, BEAK_LONG, CREST, length = 0.8f, legs = 0.8f, head = 0.95f)),
        Species("archaeopteryx", "始祖鸟", "Archaeopteryx lithographica", "侏罗纪晚期 · 约 1.5 亿年前", AIR, tr(1, 0, 0, 2, 0, 2), "虫子", CHIRP,
            look(BIRD, pal(0xFF4A5568, 0xFFCFD5DE, 0xFF2F3A4A), LONG_TAIL, length = 0.95f)),
        Species("mallard", "绿头鸭", "Anas platyrhynchos", MODERN, WATER, tr(1, 2, -1, 1, -1, -2), "水草", HONK,
            look(BIRD, pal(0xFFA57E57, 0xFFE5D2B8, 0xFF6B8FAE), BEAK_FLAT)),

        // ---- insects ----
        Species("fly", "苍蝇", "Musca domestica", MODERN, AIR, tr(2, -1, 0, 2, -1, -1), "方糖", BUZZ,
            look(INSECT, pal(0xFF4A525C, 0xFF6B737D, 0xFF4F7C6E, dark = 0xFF2C3036, eye = 0xFFB9473F), COMPOUND_EYES, CLEAR_WINGS, length = 0.9f, height = 0.95f)),
        Species("bee", "蜜蜂", "Apis mellifera", MODERN, AIR, tr(2, 2, 0, 0, -2, -1), "花蜜", BUZZ,
            look(INSECT, pal(0xFFB8864A, 0xFFF5D27A, 0xFFF2C14E, dark = 0xFF3A2F2B), CLEAR_WINGS, STINGER, ANTENNAE, FUZZY, pattern = Pattern.STRIPES, head = 1.05f)),
        Species("mosquito", "白纹伊蚊", "Aedes albopictus", MODERN, AIR, tr(1, -1, 1, 1, 2, -1), "花蜜", BUZZ,
            look(INSECT, pal(0xFF2B2F36, 0xFFD9DDE3, 0xFFEDEDED, dark = 0xFF2B2F36), CLEAR_WINGS, PROBOSCIS, ANTENNAE, pattern = Pattern.STRIPES, length = 0.85f, height = 0.55f, legs = 1.6f, head = 0.8f)),
        Species("swallowtail", "金凤蝶", "Papilio machaon", MODERN, AIR, tr(1, 0, -2, 1, -2, -1), "花蜜", BUZZ,
            look(INSECT, pal(0xFF3A3433, 0xFFF8EED2, 0xFFF2B13C), BIG_WINGS, ANTENNAE, length = 0.9f, height = 0.45f, head = 0.75f)),
        Species("ladybug", "七星瓢虫", "Coccinella septempunctata", MODERN, AIR, tr(0, 0, -1, 1, -2, -1), "蚜虫", BUZZ,
            look(INSECT, pal(0xFF2B2730, 0xFF3A3540, 0xFFD9443A), ELYTRA, pattern = Pattern.SPOTS, length = 0.95f, height = 1.1f, legs = 0.6f)),
        Species("ant", "蚂蚁", "Camponotus japonicus", MODERN, LAND, tr(2, 2, 0, 0, 0, -1), "糖粒", CLICK,
            look(INSECT, pal(0xFF6B3A2E, 0xFF8A5446, 0xFF5A3026, dark = 0xFF2B1E1A), ANTENNAE, length = 0.7f, height = 0.85f, legs = 1.1f)),
        Species("rhino_beetle", "独角仙", "Trypoxylus dichotomus", MODERN, AIR, tr(0, -1, 2, -1, 2, -1), "树汁", BUZZ,
            look(INSECT, pal(0xFF5A3A2A, 0xFF6E4A38, 0xFF4A2E22), ELYTRA, HORN, length = 1.05f, legs = 0.8f)),
        Species("dragonfly", "碧伟蜓", "Anax parthenope", MODERN, AIR, tr(2, -1, 1, 1, -2, 0), "蚊子", BUZZ,
            look(INSECT, pal(0xFF4A8FB0, 0xFF9CC8DA, 0xFF3E86A8, dark = 0xFF22323C, eye = 0xFF3FA6C9), COMPOUND_EYES, CLEAR_WINGS, LONG_ABDOMEN, length = 1.8f, height = 0.35f, legs = 0.7f, head = 0.9f)),

        // ---- other arthropods ----
        Species("jumping_spider", "跳蛛", "Phidippus regius", MODERN, LAND, tr(1, -2, 1, 2, -1, -1), "小虫", CLICK,
            look(ARTHROPOD, pal(0xFF3A3438, 0xFF5A5258, 0xFFF2A33A), EIGHT_LEGS, FUZZY, pattern = Pattern.SPOTS, length = 0.9f)),
        Species("crab", "中华绒螯蟹", "Eriocheir sinensis", MODERN, WATER, tr(0, 0, 1, 0, 1, -1), "小鱼", BUBBLE,
            look(ARTHROPOD, pal(0xFFD96A3F, 0xFFF2D3BE, 0xFFE07B4C), EYE_STALKS, CLAWS)),
        Species("scorpion", "东亚钳蝎", "Mesobuthus martensii", MODERN, LAND, tr(-1, -2, 2, -1, 2, 0), "小虫", CLICK,
            look(ARTHROPOD, pal(0xFFB88A4A, 0xFFE8D4B0, 0xFF9E7238), CLAWS, CURLED_STINGER, length = 0.9f)),
        Species("trilobite", "三叶虫", "Trilobita", "寒武纪至二叠纪 · 5.2 亿 – 2.5 亿年前", WATER, tr(-1, 1, -1, 1, 0, 2), "海底碎屑", CLICK,
            look(ARTHROPOD, pal(0xFF8C8478, 0xFFD8D2C6, 0xFFA39A8C), SEGMENTS, ANTENNAE)),

        // ---- fish and whales ----
        Species("goldfish", "金鱼", "Carassius auratus", MODERN, WATER, tr(0, 1, -1, 1, -1, -2), "鱼食", BUBBLE,
            look(FISH, pal(0xFFF08A3C, 0xFFFBD9B8, 0xFFF5A35C), LONG_TAIL, length = 0.8f, height = 1.05f, head = 1.1f)),
        Species("clownfish", "小丑鱼", "Amphiprion ocellaris", MODERN, WATER, tr(1, 2, 0, 1, -1, -1), "浮游生物", BUBBLE,
            look(FISH, pal(0xFFF07C2E, 0xFFF7A65C, 0xFFF59A4A), pattern = Pattern.BANDS, length = 0.85f)),
        Species("great_white", "大白鲨", "Carcharodon carcharias", MODERN, WATER, tr(1, -2, 2, 1, 0, 0), "鱼", BUBBLE,
            look(FISH, pal(0xFF8A9AA8, 0xFFF0F2F2, 0xFF7A8A98), TALL_FIN, TEETH, length = 1.2f, height = 0.85f)),
        Species("dunkleosteus", "邓氏鱼", "Dunkleosteus terrelli", "泥盆纪晚期 · 约 3.7 亿年前", WATER, tr(0, -2, 2, -1, 0, 2), "鱼", BUBBLE,
            look(FISH, pal(0xFF7A8A8E, 0xFFD9DED8, 0xFF9E8E72), HEAD_ARMOR, TEETH, length = 1.15f)),
        Species("coelacanth", "腔棘鱼", "Latimeria chalumnae", "现代 · 活化石", WATER, tr(-2, -1, 0, 0, 2, 1), "小鱼", BUBBLE,
            look(FISH, pal(0xFF3F5A7A, 0xFF7F95AE, 0xFF6F86A0), LOBE_FINS, pattern = Pattern.SPOTS, length = 1.05f, height = 0.95f)),
        Species("pufferfish", "红鳍东方鲀", "Takifugu rubripes", MODERN, WATER, tr(0, 0, -2, 1, 0, -1), "贝类", BUBBLE,
            look(FISH, pal(0xFFE8D89A, 0xFFF8F2DC, 0xFFC9B36A), ROUND_BODY, SPINES, pattern = Pattern.SPOTS)),
        Species("blue_whale", "蓝鲸", "Balaenoptera musculus", MODERN, WATER, tr(-1, 1, 0, 0, 0, -1), "磷虾", WHALE,
            look(FISH, pal(0xFF5C7A99, 0xFFD5DEE6, 0xFF4E6A88), FLUKES, length = 1.25f, height = 0.85f)),
        Species("dolphin", "宽吻海豚", "Tursiops truncatus", MODERN, WATER, tr(2, 2, 1, 2, 0, -1), "鱼", WHALE,
            look(FISH, pal(0xFF8FA3B5, 0xFFE9EEF2, 0xFF7E92A4), FLUKES, BEAK_SNOUT, TALL_FIN, length = 1.05f, height = 0.8f)),
        Species("anglerfish", "鮟鱇", "Melanocetus johnsonii", MODERN, WATER, tr(-1, -2, 0, 1, 2, 0), "小鱼", BUBBLE,
            look(FISH, pal(0xFF4B4458, 0xFF7A7088, 0xFF3E384A, eye = 0xFF2B2233), LURE, TEETH, length = 0.85f, height = 1.15f, head = 1.1f)),

        // ---- molluscs and jellies ----
        Species("octopus", "章鱼", "Octopus vulgaris", MODERN, WATER, tr(0, -2, 0, 2, 1, 0), "螃蟹", BUBBLE,
            look(MOLLUSC, pal(0xFFE07B6A, 0xFFF7C8BC, 0xFFC65A4A), TENTACLES, pattern = Pattern.SPOTS)),
        Species("moon_jelly", "海月水母", "Aurelia aurita", MODERN, WATER, tr(-2, 1, -1, -1, 0, 1), "浮游生物", BUBBLE,
            look(MOLLUSC, pal(0xFFC9B7E8, 0xFFF2EAFB, 0xFFB49ADB), BELL)),
        Species("ammonite", "菊石", "Ammonoidea", "泥盆纪至白垩纪 · 4 亿 – 6600 万年前", WATER, tr(0, 1, 0, 1, 0, 2), "浮游生物", BUBBLE,
            look(MOLLUSC, pal(0xFFCDB79A, 0xFFEFE4D2, 0xFFD9B98C), SPIRAL_SHELL, TENTACLES)),
        Species("snail", "蜗牛", "Bradybaena similaris", MODERN, LAND, tr(-2, -1, -1, 0, 1, -1), "菜叶", BUBBLE,
            look(MOLLUSC, pal(0xFFD6C3A2, 0xFFD9C7A8, 0xFFC98E5A), SPIRAL_SHELL, FOOT, EYE_STALKS)),

        // ---- amphibians ----
        Species("frog", "黑斑蛙", "Pelophylax nigromaculatus", MODERN, LAND, tr(1, 1, -1, 0, 1, -1), "虫子", CROAK,
            look(AMPHIBIAN, pal(0xFF7DBE6A, 0xFFE8F2C8, 0xFF4E8F4A), BIG_HIND_LEGS, pattern = Pattern.SPOTS)),
        Species("axolotl", "六角恐龙", "Ambystoma mexicanum", MODERN, WATER, tr(-1, 0, -1, 1, 1, -1), "小虾", BUBBLE,
            look(AMPHIBIAN, pal(0xFFF2B6C2, 0xFFFBE1E6, 0xFFE46F8A), GILLS, length = 0.9f)),

        // ---- serpentine ----
        Species("corn_snake", "玉米蛇", "Pantherophis guttatus", MODERN, LAND, tr(-1, -2, 1, 0, 1, -1), "老鼠", HISS,
            look(SERPENT, pal(0xFFE0864A, 0xFFF4D2B0, 0xFFC4462E), TONGUE, pattern = Pattern.BANDS)),
        Species("earthworm", "蚯蚓", "Lumbricus terrestris", MODERN, LAND, tr(0, 0, -2, 0, 2, -1), "落叶", SQUEAK,
            look(SERPENT, pal(0xFFE8A0A0, 0xFFF6CACA, 0xFFD47A80), SEGMENTS, length = 0.9f, height = 0.7f)),
        Species("caterpillar", "毛毛虫", "Papilio xuthus", "现代 · 柑橘凤蝶的幼虫", LAND, tr(-1, 0, -1, 1, -1, -1), "嫩叶", SQUEAK,
            look(SERPENT, pal(0xFF8CC66A, 0xFFDDEFB8, 0xFFF2C14E), PROLEGS, ANTENNAE, pattern = Pattern.SPOTS, length = 0.8f)),
    )

    private val byId = all.associateBy { it.id }

    fun get(id: String): Species = byId.getValue(id)
}
