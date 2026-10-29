package io.github.fyy0619cell.deskpet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import io.github.fyy0619cell.deskpet.overlay.OverlayService
import io.github.fyy0619cell.deskpet.pet.Adoption
import io.github.fyy0619cell.deskpet.pet.PetStore
import io.github.fyy0619cell.deskpet.pet.Stats
import io.github.fyy0619cell.deskpet.pet.StatsMath
import io.github.fyy0619cell.deskpet.pet.StatsStore
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.share.ShareCard
import io.github.fyy0619cell.deskpet.sound.SoundPlayer
import io.github.fyy0619cell.deskpet.ui.HomeActions
import io.github.fyy0619cell.deskpet.ui.HomeScreen
import io.github.fyy0619cell.deskpet.ui.accentFor
import io.github.fyy0619cell.deskpet.ui.quiz.QuizFlow
import io.github.fyy0619cell.deskpet.ui.settings.KeepAliveScreen
import io.github.fyy0619cell.deskpet.ui.settings.PrivacyConsentScreen
import io.github.fyy0619cell.deskpet.ui.settings.PrivacyPolicyScreen
import io.github.fyy0619cell.deskpet.ui.settings.SettingsActions
import io.github.fyy0619cell.deskpet.ui.settings.SettingsScreen
import io.github.fyy0619cell.deskpet.ui.settings.SettingsState
import io.github.fyy0619cell.deskpet.ui.theme.DeskTheme
import io.github.fyy0619cell.deskpet.ui.theme.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.fyy0619cell.deskpet.pet.Settings as PetSettings

private enum class Screen { MAIN, SETTINGS, KEEP_ALIVE, PRIVACY }

class MainActivity : ComponentActivity() {
    private lateinit var store: PetStore
    private lateinit var statsStore: StatsStore
    private lateinit var prefs: PetSettings
    private val sound by lazy { SoundPlayer(this) }

    private var adoption by mutableStateOf<Adoption?>(null)
    private var stats by mutableStateOf(Stats())
    private var reaction by mutableStateOf<Action?>(null)
    private var reactionId by mutableLongStateOf(0L)
    private var showOverlayGuide by mutableStateOf(false)
    private var screen by mutableStateOf(Screen.MAIN)
    private var consented by mutableStateOf(false)
    private var settingsState by mutableStateOf(SettingsState(1, true, true, true, false))
    private var pendingOverlayStart = false

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { OverlayService.start(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val light = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = light, navigationBarStyle = light)
        store = PetStore(this)
        statsStore = StatsStore(this)
        prefs = PetSettings(this)
        adoption = store.adoption()
        consented = prefs.privacyAccepted
        settingsState = readSettings()
        val homeActions = HomeActions(
            onTapPet = ::tapPet, onFeed = ::feed, onClean = ::clean, onShare = ::share,
            onToggleOverlay = ::toggleOverlay, onSettings = { screen = Screen.SETTINGS },
        )
        val settingsActions = SettingsActions(
            onSize = { changeSetting { size = it } },
            onWander = { changeSetting { wanderOn = it } },
            onHalo = { changeSetting { halo = it } },
            onSound = { changeSetting { soundOn = it } },
            onAutoStart = { changeSetting { autoStart = it } },
            onKeepAlive = { screen = Screen.KEEP_ALIVE },
            onPrivacy = { screen = Screen.PRIVACY },
            onBack = { screen = Screen.MAIN },
        )
        setContent {
            val overlayOn by OverlayService.running.collectAsState()
            val current = adoption
            val accent = current?.let { Color(accentFor(it.look)) } ?: Palette.Ink
            LaunchedEffect(reactionId) {
                if (reaction != null) { delay(1800); reaction = null }
            }
            BackHandler(enabled = screen != Screen.MAIN) { screen = parentOf(screen) }
            DeskTheme(accent) {
                Surface(Modifier.fillMaxSize(), color = Palette.Paper) {
                    Box(Modifier.safeDrawingPadding()) {
                        when {
                            screen == Screen.PRIVACY -> PrivacyPolicyScreen { screen = parentOf(Screen.PRIVACY) }
                            !consented -> PrivacyConsentScreen(
                                onAgree = { prefs.privacyAccepted = true; consented = true },
                                onDisagree = { finish() },
                                onReadPolicy = { screen = Screen.PRIVACY },
                            )
                            screen == Screen.SETTINGS -> SettingsScreen(settingsState, versionName(), settingsActions)
                            screen == Screen.KEEP_ALIVE -> KeepAliveScreen(
                                Build.MANUFACTURER,
                                onAppSettings = ::openAppSettings,
                                onBatterySettings = ::openBatterySettings,
                                onBack = { screen = Screen.SETTINGS },
                            )
                            current == null -> QuizFlow { a ->
                                if (store.adopt(a)) {
                                    adoption = store.adoption()
                                    stats = statsStore.read()
                                }
                            }
                            else -> HomeScreen(current, stats, overlayOn, homeActions, reaction)
                        }
                    }
                    if (showOverlayGuide) OverlayGuide(current?.name ?: "它")
                }
            }
        }
    }

    private fun parentOf(s: Screen) = when (s) {
        Screen.KEEP_ALIVE -> Screen.SETTINGS
        Screen.PRIVACY -> if (consented) Screen.SETTINGS else Screen.MAIN
        else -> Screen.MAIN
    }

    override fun onResume() {
        super.onResume()
        if (adoption != null) stats = statsStore.read()
        if (pendingOverlayStart && Settings.canDrawOverlays(this)) {
            pendingOverlayStart = false
            startOverlay()
        }
    }

    override fun onDestroy() {
        sound.release()
        super.onDestroy()
    }

    // ---- settings ----

    private fun readSettings() = SettingsState(prefs.size, prefs.wanderOn, prefs.halo, prefs.soundOn, prefs.autoStart)

    private fun changeSetting(edit: PetSettings.() -> Unit) {
        prefs.edit()
        settingsState = readSettings()
        OverlayService.send(this, OverlayService.ACTION_SETTINGS)
    }

    private fun versionName(): String =
        runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: ""

    private fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
    }

    private fun openBatterySettings() {
        runCatching { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
            .onFailure { openAppSettings() }
    }

    // ---- care ----

    private fun react(a: Action) {
        reaction = a
        reactionId++
    }

    private fun tapPet() {
        val a = adoption ?: return
        stats = statsStore.update { s, now -> StatsMath.pet(s, now) }
        react(Action.HAPPY)
        sound.voice(a.species.voice)
    }

    private fun feed() {
        stats = statsStore.update { s, now -> StatsMath.feed(s, now) }
        react(Action.EAT)
        if (OverlayService.running.value) OverlayService.send(this, OverlayService.ACTION_FEED) else sound.nom()
    }

    private fun clean() {
        val a = adoption ?: return
        stats = statsStore.update { s, now -> StatsMath.clean(s, now) }
        react(Action.HAPPY)
        if (OverlayService.running.value) OverlayService.send(this, OverlayService.ACTION_CLEAN) else sound.voice(a.species.voice)
    }

    private fun share() {
        val a = adoption ?: return
        val accent = accentFor(a.look)
        val appName = getString(R.string.app_name)
        lifecycleScope.launch {
            val uri = withContext(Dispatchers.IO) {
                val bmp = ShareCard.render(a, accent, appName)
                ShareCard.save(this@MainActivity, bmp, appName, "${a.name}-${a.species.nameZh}-${System.currentTimeMillis()}.png")
            }
            if (uri == null) {
                Toast.makeText(this@MainActivity, "保存失败，请稍后再试", Toast.LENGTH_SHORT).show()
                return@launch
            }
            Toast.makeText(this@MainActivity, "已保存到相册", Toast.LENGTH_SHORT).show()
            val send = Intent(Intent.ACTION_SEND).setType("image/png")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(send, "分享${a.name}"))
        }
    }

    // ---- overlay ----

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

    @Composable
    private fun OverlayGuide(name: String) {
        AlertDialog(
            onDismissRequest = { showOverlayGuide = false },
            containerColor = Palette.Card,
            title = { Text("需要一个权限") },
            text = {
                Text(
                    "让${name}待在桌面上，需要允许「显示在其他应用上层」。接下来会打开系统设置，找到「本命兽」并打开开关。\n\n" +
                        "如果开关是灰色、点不动：进入 系统设置 → 应用 → 本命兽，点右上角「⋮」选择「允许受限制的设置」，再回来打开。",
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
