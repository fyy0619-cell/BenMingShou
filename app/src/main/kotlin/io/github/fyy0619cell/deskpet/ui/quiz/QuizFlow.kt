package io.github.fyy0619cell.deskpet.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.fyy0619cell.deskpet.pet.Adoption
import io.github.fyy0619cell.deskpet.quiz.Matcher
import io.github.fyy0619cell.deskpet.quiz.Questions
import io.github.fyy0619cell.deskpet.render.Variation
import io.github.fyy0619cell.deskpet.species.Catalog
import io.github.fyy0619cell.deskpet.ui.theme.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

private const val INTRO = -1
private val COUNT = Questions.all.size
private val SEEKING = COUNT
private val REVEAL = COUNT + 1
private val NAMING = COUNT + 2

@Composable
fun QuizFlow(onAdopt: (Adoption) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(INTRO) }
    var answers by rememberSaveable { mutableStateOf(IntArray(COUNT) { -1 }) }
    var seed by rememberSaveable { mutableLongStateOf(0L) }
    var name by rememberSaveable { mutableStateOf("") }
    var confirming by remember { mutableStateOf(false) }
    var advancing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { withContext(Dispatchers.Default) { Matcher.assignment } }

    AnimatedContent(
        targetState = step,
        transitionSpec = { fadeIn(tween(420)) togetherWith fadeOut(tween(220)) },
        label = "quiz",
    ) { s ->
        when {
            s == INTRO -> IntroScreen(Catalog.all.size, COUNT) { step = 0 }
            s < COUNT -> QuestionScreen(
                index = s,
                total = COUNT,
                question = Questions.all[s],
                selected = answers[s].takeIf { it >= 0 },
                onSelect = { i ->
                    if (!advancing) {
                        advancing = true
                        answers = answers.copyOf().also { it[s] = i }
                        scope.launch {
                            delay(280)
                            if (s + 1 == COUNT) seed = SecureRandom().nextLong()
                            step = s + 1
                            advancing = false
                        }
                    }
                },
                onBack = if (s > 0) ({ step = s - 1 }) else null,
            )
            s == SEEKING -> {
                SeekingScreen()
                LaunchedEffect(Unit) { delay(2600); step = REVEAL }
            }
            s == REVEAL -> {
                val sp = remember(answers.contentHashCode()) { Matcher.match(answers) }
                val look = remember(sp, seed) { Variation.individualize(sp.look, seed) }
                val lines = remember(sp) { Matcher.commonGround(answers, sp) }
                val reveal = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    delay(500)
                    reveal.animateTo(1f, tween(1600, easing = FastOutSlowInEasing))
                }
                RevealScreen(sp, look, lines, reveal.value, onNext = {
                    if (name.isBlank()) name = Names.forSeed(seed)
                    step = NAMING
                })
            }
            else -> {
                val sp = remember(answers.contentHashCode()) { Matcher.match(answers) }
                val look = remember(sp, seed) { Variation.individualize(sp.look, seed) }
                NamingScreen(look, name, { name = it }, { name = Names.after(name) }, { confirming = true })
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            containerColor = Palette.Card,
            title = { Text("确定是它了吗？") },
            text = { Text("一次安装只能测一次。带它回家之后，就不能再换了。") },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    val sp = Matcher.match(answers)
                    onAdopt(Adoption(sp.id, seed, name.trim(), System.currentTimeMillis(), answers.joinToString(",")))
                }) { Text("带它回家", color = Palette.Ink) }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("再想想", color = Palette.Muted) } },
        )
    }
}

object Names {
    private val pool = listOf(
        "团子", "豆包", "小满", "阿福", "糯糯", "球球", "年糕", "可乐", "麦芽", "布丁",
        "汤圆", "饭团", "栗子", "奶盖", "芝麻", "花卷", "果冻", "拿铁", "元宝", "毛豆",
    )

    fun forSeed(seed: Long): String = pool[((seed ushr 1) % pool.size).toInt()]

    fun after(current: String): String = pool[(pool.indexOf(current) + 1).mod(pool.size)]
}
