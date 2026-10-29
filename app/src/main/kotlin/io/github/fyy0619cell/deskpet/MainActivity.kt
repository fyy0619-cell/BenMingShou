package io.github.fyy0619cell.deskpet

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.fyy0619cell.deskpet.pet.PetStore
import io.github.fyy0619cell.deskpet.ui.HomeScreen
import io.github.fyy0619cell.deskpet.ui.accentFor
import io.github.fyy0619cell.deskpet.ui.quiz.QuizFlow
import io.github.fyy0619cell.deskpet.ui.theme.DeskTheme
import io.github.fyy0619cell.deskpet.ui.theme.Palette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val light = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = light, navigationBarStyle = light)
        val store = PetStore(this)
        setContent {
            var adoption by remember { mutableStateOf(store.adoption()) }
            val accent = adoption?.let { Color(accentFor(it.look)) } ?: Palette.Ink
            DeskTheme(accent) {
                Surface(Modifier.fillMaxSize(), color = Palette.Paper) {
                    Box(Modifier.safeDrawingPadding()) {
                        val current = adoption
                        if (current == null) {
                            QuizFlow { a -> if (store.adopt(a)) adoption = store.adoption() }
                        } else {
                            HomeScreen(current)
                        }
                    }
                }
            }
        }
    }
}
