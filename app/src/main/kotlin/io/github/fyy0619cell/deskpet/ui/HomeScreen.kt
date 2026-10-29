package io.github.fyy0619cell.deskpet.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fyy0619cell.deskpet.pet.Adoption
import io.github.fyy0619cell.deskpet.pet.Stats
import io.github.fyy0619cell.deskpet.pet.StatsMath
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.ColorX
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.ui.theme.LocalAccent
import io.github.fyy0619cell.deskpet.ui.theme.Palette
import java.util.concurrent.TimeUnit

class HomeActions(
    val onTapPet: () -> Unit = {},
    val onFeed: () -> Unit = {},
    val onClean: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onToggleOverlay: () -> Unit = {},
)

@Composable
fun HomeScreen(
    adoption: Adoption,
    stats: Stats,
    overlayOn: Boolean,
    actions: HomeActions,
    reaction: Action? = null,
    now: Long = System.currentTimeMillis(),
    animate: Boolean = true,
) {
    val look = remember(adoption) { adoption.look }
    val sp = adoption.species
    val days = TimeUnit.MILLISECONDS.toDays(now - adoption.adoptedAt) + 1
    val level = StatsMath.level(stats.xp)
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp)) {
        Eyebrow("陪伴第 $days 天 · Lv.$level")
        Spacer(Modifier.height(14.dp))
        Text(adoption.name, style = MaterialTheme.typography.displayMedium, color = Palette.Ink)
        Text("${sp.nameZh} · ${sp.latin}", fontStyle = FontStyle.Italic, fontSize = 14.sp, color = Palette.Muted)
        Spacer(Modifier.height(6.dp))
        LevelBar(StatsMath.levelProgress(stats.xp))
        Spacer(Modifier.weight(0.3f))
        PetCanvas(
            look,
            Modifier.fillMaxWidth().height(230.dp)
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = actions.onTapPet),
            action = reaction ?: Action.IDLE,
            animate = animate && (!overlayOn || reaction != null),
        )
        Spacer(Modifier.weight(0.3f))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatRow("饱食", stats.hunger)
            StatRow("清洁", stats.clean)
            StatRow("心情", stats.mood)
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinePill("喂${sp.food}", actions.onFeed, Modifier.weight(1f))
            OutlinePill("清洁", actions.onClean, Modifier.weight(1f))
        }
        Spacer(Modifier.weight(1f))
        QuietButton("保存分享卡片", actions.onShare, Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        PillButton(if (overlayOn) "把${adoption.name}收回来" else "让${adoption.name}去桌面", actions.onToggleOverlay)
    }
}

@Composable
private fun LevelBar(progress: Float) {
    val p by animateFloatAsState(progress, tween(600), label = "level")
    Box(Modifier.width(120.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(Palette.Hairline)) {
        Box(Modifier.fillMaxWidth(p).height(2.dp).background(LocalAccent.current))
    }
}

@Composable
private fun StatRow(label: String, value: Float) {
    val v by animateFloatAsState(value / 100f, tween(500), label = label)
    val needy = value < StatsMath.NEEDY
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = Palette.Muted, modifier = Modifier.width(44.dp))
        Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Palette.Hairline)) {
            Box(Modifier.fillMaxWidth(v).height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (needy) Palette.Warn else LocalAccent.current))
        }
        Spacer(Modifier.width(12.dp))
        Text(value.toInt().toString(), fontSize = 13.sp, color = if (needy) Palette.Warn else Palette.Muted, modifier = Modifier.width(28.dp))
    }
}

@Composable
private fun OutlinePill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(24.dp))
            .border(1.dp, Palette.Hairline, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Palette.Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    }
}

/** A readable accent derived from the pet's main colour: never too pale for white text. */
fun accentFor(look: Look): Int {
    val hsl = ColorX.hsl(look.palette.base)
    return ColorX.fromHsl(hsl[0], hsl[1].coerceAtMost(0.55f), hsl[2].coerceIn(0.22f, 0.42f))
}
