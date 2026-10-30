package io.github.fyy0619cell.deskpet.ui.quiz

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fyy0619cell.deskpet.quiz.Question
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.Look
import io.github.fyy0619cell.deskpet.render.Rarity
import io.github.fyy0619cell.deskpet.species.Catalog
import io.github.fyy0619cell.deskpet.species.Species
import io.github.fyy0619cell.deskpet.ui.Eyebrow
import io.github.fyy0619cell.deskpet.ui.Hairline
import io.github.fyy0619cell.deskpet.ui.OptionCard
import io.github.fyy0619cell.deskpet.ui.PetCanvas
import io.github.fyy0619cell.deskpet.ui.PillButton
import io.github.fyy0619cell.deskpet.ui.QuietButton
import io.github.fyy0619cell.deskpet.ui.Tag
import io.github.fyy0619cell.deskpet.ui.theme.LocalAccent
import io.github.fyy0619cell.deskpet.ui.theme.Palette
import androidx.compose.foundation.background
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle

private val Gutter = 28.dp

@Composable
fun IntroScreen(speciesCount: Int, questionCount: Int, animate: Boolean = true, onStart: () -> Unit) {
    val parade = remember { PARADE.map { Catalog.get(it).look } }
    Column(Modifier.fillMaxSize().padding(horizontal = Gutter, vertical = 24.dp)) {
        Spacer(Modifier.weight(0.5f))
        Row(Modifier.fillMaxWidth().alpha(0.13f), horizontalArrangement = Arrangement.SpaceBetween) {
            parade.forEach { PetCanvas(it, Modifier.size(58.dp), silhouette = 1f, animate = animate) }
        }
        Spacer(Modifier.weight(0.5f))
        Eyebrow("第 一 次 见 面")
        Spacer(Modifier.height(20.dp))
        Text("遇见你的\n本命动物", style = MaterialTheme.typography.displayMedium, color = Palette.Ink)
        Spacer(Modifier.height(24.dp))
        Text(
            "${questionCount}个问题，凭直觉回答。\n从远古到现代，${speciesCount}种生命里，\n只有一种属于你。",
            style = MaterialTheme.typography.bodyLarge,
            color = Palette.Muted,
        )
        Spacer(Modifier.weight(1.4f))
        Text("一次安装只能测一次，它会一直陪着你。", style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
        Spacer(Modifier.height(16.dp))
        PillButton("开始", onStart)
    }
}

@Composable
fun QuestionScreen(index: Int, total: Int, question: Question, selected: Int?, onSelect: (Int) -> Unit, onBack: (() -> Unit)?) {
    Column(Modifier.fillMaxSize().padding(horizontal = Gutter, vertical = 24.dp)) {
        Progress(index, total)
        Spacer(Modifier.weight(0.6f))
        Text(question.text, style = MaterialTheme.typography.headlineMedium, color = Palette.Ink)
        Spacer(Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { i, opt ->
                OptionCard(LETTERS[i], opt.text, selected == i) { onSelect(i) }
            }
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.height(40.dp)) {
            if (onBack != null) QuietButton("上一题", onBack)
        }
    }
}

@Composable
private fun Progress(index: Int, total: Int) {
    val accent = LocalAccent.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(2.dp).clip(CircleShape).background(Palette.Hairline)) {
            Box(Modifier.fillMaxWidth((index + 1f) / total).height(2.dp).background(accent))
        }
        Spacer(Modifier.width(16.dp))
        Text("${index + 1} / $total", color = Palette.Muted, fontSize = 13.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun SeekingScreen() {
    val pulse by rememberInfiniteTransition(label = "seek").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "pulse",
    )
    Column(Modifier.fillMaxSize().padding(Gutter), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Canvas(Modifier.size(160.dp)) {
            for (k in 0..2) {
                val f = (pulse + k / 3f) % 1f
                drawCircle(Palette.Ink.copy(alpha = (1f - f) * 0.35f), radius = size.minDimension / 2f * (0.2f + 0.8f * f), center = Offset(size.width / 2f, size.height / 2f), style = Stroke(1.5f.dp.toPx()))
            }
            drawCircle(Palette.Ink, radius = 4.dp.toPx())
        }
        Spacer(Modifier.height(32.dp))
        Text("正在穿越时间\n寻找与你相似的生命", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, color = Palette.Muted)
    }
}

@Composable
fun RevealScreen(species: Species, look: Look, commonGround: List<String>, reveal: Float, onNext: () -> Unit, animate: Boolean = true) {
    val shown = reveal >= 1f
    Column(Modifier.fillMaxSize().padding(horizontal = Gutter, vertical = 24.dp)) {
        Eyebrow("你 的 本 命 动 物")
        Spacer(Modifier.height(8.dp))
        PetCanvas(
            look, Modifier.fillMaxWidth().height(260.dp),
            action = if (shown) Action.HAPPY else Action.IDLE,
            silhouette = 1f - reveal, animate = animate,
        )
        Column(Modifier.fillMaxWidth().alpha(((reveal - 0.7f) / 0.3f).coerceIn(0f, 1f))) {
            Text(species.nameZh, style = MaterialTheme.typography.displayMedium, color = Palette.Ink)
            Text(species.latin, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = Palette.Muted)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(species.era)
                if (look.rare != Rarity.NORMAL) Tag(rarityLabel(look.rare), Palette.Gold)
            }
            Spacer(Modifier.height(24.dp))
            Hairline()
            Spacer(Modifier.height(20.dp))
            Eyebrow("你 们 的 共 同 点")
            Spacer(Modifier.height(12.dp))
            commonGround.forEach { line ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(LocalAccent.current))
                    Spacer(Modifier.width(12.dp))
                    Text(line, style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (shown) PillButton("给它起个名字", onNext)
    }
}

@Composable
fun NamingScreen(look: Look, name: String, onName: (String) -> Unit, onShuffle: () -> Unit, onConfirm: () -> Unit, animate: Boolean = true) {
    val accent = LocalAccent.current
    Column(Modifier.fillMaxSize().padding(horizontal = Gutter, vertical = 24.dp)) {
        Spacer(Modifier.weight(0.4f))
        PetCanvas(look, Modifier.fillMaxWidth().height(220.dp), action = Action.IDLE, animate = animate)
        Spacer(Modifier.height(24.dp))
        Text("给它起个名字", style = MaterialTheme.typography.headlineMedium, color = Palette.Ink)
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            BasicTextField(
                value = name,
                onValueChange = { onName(it.take(MAX_NAME)) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 24.sp, color = Palette.Ink, letterSpacing = 1.sp),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.weight(1f),
            )
            QuietButton("换一个", onShuffle)
        }
        Spacer(Modifier.height(8.dp))
        Hairline()
        Spacer(Modifier.height(10.dp))
        Text("最多 $MAX_NAME 个字", color = Palette.Muted, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        PillButton("就是它了", onConfirm, enabled = name.isNotBlank())
    }
}

fun rarityLabel(r: Rarity) = when (r) {
    Rarity.ALBINO -> "稀有 · 白化"
    Rarity.GOLDEN -> "稀有 · 金色"
    Rarity.MELANISTIC -> "稀有 · 黑化"
    Rarity.NORMAL -> ""
}

const val MAX_NAME = 8
private val LETTERS = listOf("A", "B", "C", "D")
private val PARADE = listOf("trex", "owl", "moon_jelly", "bee", "tiger")
