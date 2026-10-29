package io.github.fyy0619cell.deskpet

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import io.github.fyy0619cell.deskpet.pet.Adoption
import io.github.fyy0619cell.deskpet.pet.Stats
import io.github.fyy0619cell.deskpet.quiz.Matcher
import io.github.fyy0619cell.deskpet.share.ShareCard
import io.github.fyy0619cell.deskpet.ui.HomeActions
import io.github.fyy0619cell.deskpet.quiz.Questions
import io.github.fyy0619cell.deskpet.render.Rarity
import io.github.fyy0619cell.deskpet.render.Variation
import io.github.fyy0619cell.deskpet.species.Catalog
import io.github.fyy0619cell.deskpet.ui.HomeScreen
import io.github.fyy0619cell.deskpet.ui.accentFor
import io.github.fyy0619cell.deskpet.ui.quiz.IntroScreen
import io.github.fyy0619cell.deskpet.ui.quiz.NamingScreen
import io.github.fyy0619cell.deskpet.ui.quiz.QuestionScreen
import io.github.fyy0619cell.deskpet.ui.quiz.RevealScreen
import io.github.fyy0619cell.deskpet.ui.quiz.SeekingScreen
import io.github.fyy0619cell.deskpet.ui.settings.KeepAliveScreen
import io.github.fyy0619cell.deskpet.ui.settings.PrivacyConsentScreen
import io.github.fyy0619cell.deskpet.ui.settings.PrivacyPolicyScreen
import io.github.fyy0619cell.deskpet.ui.settings.SettingsActions
import io.github.fyy0619cell.deskpet.ui.settings.SettingsScreen
import io.github.fyy0619cell.deskpet.ui.settings.SettingsState
import io.github.fyy0619cell.deskpet.ui.theme.DeskTheme
import io.github.fyy0619cell.deskpet.ui.theme.Palette
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w392dp-h850dp-xhdpi")
class ScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    private val dir = File(System.getProperty("gallery.dir")!!, "screens").apply { mkdirs() }
    private val answers = intArrayOf(0, 0, 2, 0, 2, 0)

    private fun shoot(name: String, accent: Color = Palette.Ink, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            DeskTheme(accent) { Surface(Modifier.fillMaxSize(), color = Palette.Paper) { content() } }
        }
        rule.mainClock.advanceTimeBy(800)
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap()
        File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun intro() = shoot("1_intro") { IntroScreen(Catalog.all.size, Questions.all.size, animate = false) {} }

    @Test fun question() = shoot("2_question") {
        QuestionScreen(2, Questions.all.size, Questions.all[2], selected = 1, onSelect = {}, onBack = {})
    }

    @Test fun seeking() = shoot("3_seeking") { SeekingScreen() }

    @Test fun reveal() {
        val sp = Matcher.match(answers)
        val look = Variation.individualize(sp.look, 7L)
        shoot("4_reveal") { RevealScreen(sp, look, Matcher.commonGround(answers, sp), 1f, onNext = {}, animate = false) }
    }

    @Test fun revealRare() {
        val sp = Catalog.get("triceratops")
        val look = Variation.individualize(sp.look, 7L, Rarity.GOLDEN)
        shoot("4b_reveal_rare") { RevealScreen(sp, look, Matcher.commonGround(answers, sp), 1f, onNext = {}, animate = false) }
    }

    @Test fun revealSilhouette() {
        val sp = Catalog.get("octopus")
        shoot("4c_reveal_half") { RevealScreen(sp, sp.look, emptyList(), 0.35f, onNext = {}, animate = false) }
    }

    @Test fun naming() {
        val sp = Matcher.match(answers)
        shoot("5_naming") { NamingScreen(Variation.individualize(sp.look, 7L), "团子", {}, {}, {}, animate = false) }
    }

    @Test fun home() {
        val adoption = Adoption("red_panda", 7L, "栗子", 0L, "0,0,0,0,0,0")
        val accent = Color(accentFor(adoption.look))
        val stats = Stats(hunger = 64f, clean = 22f, mood = 81f, xp = 70)
        shoot("6_home", accent) { HomeScreen(adoption, stats, overlayOn = false, actions = HomeActions(), now = 3L * 86_400_000L, animate = false) }
    }

    @Test fun homeOverlayOn() {
        val adoption = Adoption("great_white", 7L, "可乐", 0L, "0,0,0,0,0,0")
        val accent = Color(accentFor(adoption.look))
        shoot("6b_home_on", accent) { HomeScreen(adoption, Stats(), overlayOn = true, actions = HomeActions(), now = 0L, animate = false) }
    }

    @Test fun consent() = shoot("0_consent") { PrivacyConsentScreen({}, {}, {}) }

    @Test fun settings() {
        val adoption = Adoption("red_panda", 7L, "栗子", 0L, "0,0,0,0,0,0")
        shoot("8_settings", Color(accentFor(adoption.look))) {
            SettingsScreen(SettingsState(1, true, true, false, false), "0.4.0", SettingsActions())
        }
    }

    @Test fun keepAlive() = shoot("9_keep_alive") { KeepAliveScreen("Xiaomi", {}, {}, {}) }

    @Test fun privacyPolicy() = shoot("10_privacy") { PrivacyPolicyScreen {} }

    @Test fun launcherIcon() {
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val px = 432
        val bmp = Bitmap.createBitmap(px * 2, px, Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(bmp)
        c.drawColor(0xFFDDE3EA.toInt())
        val fg = ctx.getDrawable(R.drawable.ic_launcher_foreground)!!
        for (round in 0..1) {
            c.save()
            c.translate(round * px.toFloat(), 0f)
            val clip = android.graphics.Path().apply {
                if (round == 1) addCircle(px / 2f, px / 2f, px * 0.3f, android.graphics.Path.Direction.CW)
                else addRoundRect(px * 0.2f, px * 0.2f, px * 0.8f, px * 0.8f, px * 0.13f, px * 0.13f, android.graphics.Path.Direction.CW)
            }
            c.clipPath(clip)
            c.scale(1.2f, 1.2f, px / 2f, px / 2f)
            c.drawColor(0xFF231E29.toInt())
            fg.setBounds(0, 0, px, px)
            fg.draw(c)
            c.restore()
        }
        File(dir, "11_icon.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun shareCard() {
        val adoption = Adoption("triceratops", 7L, "年糕", 0L, "3,0,0,0,3,1")
        val bmp = ShareCard.render(adoption, accentFor(adoption.look), "本命兽")
        File(dir, "7_share_card.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
