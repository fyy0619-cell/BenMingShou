package io.github.fyy0619cell.deskpet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.fyy0619cell.deskpet.overlay.OverlayService
import io.github.fyy0619cell.deskpet.pet.PetStore
import io.github.fyy0619cell.deskpet.ui.HomeScreen
import io.github.fyy0619cell.deskpet.ui.accentFor
import io.github.fyy0619cell.deskpet.ui.quiz.QuizFlow
import io.github.fyy0619cell.deskpet.ui.theme.DeskTheme
import io.github.fyy0619cell.deskpet.ui.theme.Palette

class MainActivity : ComponentActivity() {
    private lateinit var store: PetStore
    private var adoption by mutableStateOf<io.github.fyy0619cell.deskpet.pet.Adoption?>(null)
    private var showOverlayGuide by mutableStateOf(false)
    private var pendingOverlayStart = false

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { OverlayService.start(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val light = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = light, navigationBarStyle = light)
        store = PetStore(this)
        adoption = store.adoption()
        setContent {
            val overlayOn by OverlayService.running.collectAsState()
            val current = adoption
            val accent = current?.let { Color(accentFor(it.look)) } ?: Palette.Ink
            DeskTheme(accent) {
                Surface(Modifier.fillMaxSize(), color = Palette.Paper) {
                    Box(Modifier.safeDrawingPadding()) {
                        if (current == null) {
                            QuizFlow { a -> if (store.adopt(a)) adoption = store.adoption() }
                        } else {
                            HomeScreen(current, overlayOn, onToggleOverlay = ::toggleOverlay)
                        }
                    }
                    if (showOverlayGuide) OverlayGuide(current?.name ?: "它")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (pendingOverlayStart && Settings.canDrawOverlays(this)) {
            pendingOverlayStart = false
            startOverlay()
        }
    }

    private fun toggleOverlay() {
        when {
            OverlayService.running.value -> OverlayService.stop(this)
            Settings.canDrawOverlays(this) -> startOverlay()
            else -> showOverlayGuide = true
        }
    }

    private fun startOverlay() {
        val needsAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsAsk) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else OverlayService.start(this)
    }

    @androidx.compose.runtime.Composable
    private fun OverlayGuide(name: String) {
        AlertDialog(
            onDismissRequest = { showOverlayGuide = false },
            containerColor = Palette.Card,
            title = { Text("需要一个权限") },
            text = {
                Text(
                    "让${name}待在桌面上，需要允许「显示在其他应用上层」。接下来会打开系统设置，找到本应用并打开开关。\n\n" +
                        "如果开关是灰色、点不动：进入 系统设置 → 应用 → 本应用，点右上角「⋮」选择「允许受限制的设置」，再回来打开。",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showOverlayGuide = false
                    pendingOverlayStart = true
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }) { Text("去开启", color = Palette.Ink) }
            },
            dismissButton = { TextButton(onClick = { showOverlayGuide = false }) { Text("以后再说", color = Palette.Muted) } },
        )
    }
}
