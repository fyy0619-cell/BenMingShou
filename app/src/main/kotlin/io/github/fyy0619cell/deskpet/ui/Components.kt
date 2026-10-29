package io.github.fyy0619cell.deskpet.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fyy0619cell.deskpet.ui.theme.LocalAccent
import io.github.fyy0619cell.deskpet.ui.theme.Palette

@Composable
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val accent = LocalAccent.current
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (enabled) accent else Palette.Hairline)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
    }
}

@Composable
fun QuietButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        color = Palette.Muted,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun OptionCard(letter: String, text: String, selected: Boolean, onClick: () -> Unit) {
    val accent = LocalAccent.current
    val bg by animateColorAsState(if (selected) accent else Palette.Card, label = "option-bg")
    val fg by animateColorAsState(if (selected) Color.White else Palette.Ink, label = "option-fg")
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = BorderStroke(1.dp, if (selected) accent else Palette.Hairline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(letter, color = fg.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(14.dp))
            Text(text, color = fg, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal))
        }
    }
}

@Composable
fun Tag(text: String, color: Color = Palette.Muted) {
    Text(
        text,
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 5.dp),
        color = color,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
    )
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Palette.Hairline))
}

@Composable
fun Eyebrow(text: String, color: Color = Palette.Muted) {
    Text(text, color = color, style = MaterialTheme.typography.labelMedium)
}
