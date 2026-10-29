package io.github.fyy0619cell.deskpet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fyy0619cell.deskpet.pet.Adoption
import io.github.fyy0619cell.deskpet.render.ColorX
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.ui.theme.Palette
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    adoption: Adoption,
    overlayOn: Boolean,
    onToggleOverlay: () -> Unit,
    now: Long = System.currentTimeMillis(),
    animate: Boolean = true,
) {
    val look = remember(adoption) { adoption.look }
    val sp = adoption.species
    val days = TimeUnit.MILLISECONDS.toDays(now - adoption.adoptedAt) + 1
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp)) {
        Eyebrow("陪 伴 第 $days 天")
        Spacer(Modifier.height(16.dp))
        Text(adoption.name, style = MaterialTheme.typography.displayMedium, color = Palette.Ink)
        Text("${sp.nameZh} · ${sp.latin}", fontStyle = FontStyle.Italic, fontSize = 14.sp, color = Palette.Muted)
        Spacer(Modifier.weight(0.5f))
        PetCanvas(look, Modifier.fillMaxWidth().height(300.dp), animate = animate && !overlayOn)
        Spacer(Modifier.weight(1f))
        Text(
            if (overlayOn) "${adoption.name}正在桌面上。点它互动，按住拖动，长按回到这里。" else "放到桌面后，它会在你用手机时陪着你。",
            style = MaterialTheme.typography.bodyMedium,
            color = Palette.Muted,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(16.dp))
        PillButton(if (overlayOn) "把${adoption.name}收回来" else "让${adoption.name}去桌面", onToggleOverlay)
    }
}

/** A readable accent derived from the pet's main colour: never too pale for white text. */
fun accentFor(look: Look): Int {
    val hsl = ColorX.hsl(look.palette.base)
    return ColorX.fromHsl(hsl[0], hsl[1].coerceAtMost(0.55f), hsl[2].coerceIn(0.22f, 0.42f))
}
