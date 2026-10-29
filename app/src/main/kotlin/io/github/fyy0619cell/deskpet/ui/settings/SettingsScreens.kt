package io.github.fyy0619cell.deskpet.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fyy0619cell.deskpet.pet.Settings
import io.github.fyy0619cell.deskpet.ui.Eyebrow
import io.github.fyy0619cell.deskpet.ui.Hairline
import io.github.fyy0619cell.deskpet.ui.PillButton
import io.github.fyy0619cell.deskpet.ui.QuietButton
import io.github.fyy0619cell.deskpet.ui.theme.LocalAccent
import io.github.fyy0619cell.deskpet.ui.theme.Palette

data class SettingsState(
    val size: Int,
    val wander: Boolean,
    val halo: Boolean,
    val sound: Boolean,
    val autoStart: Boolean,
)

class SettingsActions(
    val onSize: (Int) -> Unit = {},
    val onWander: (Boolean) -> Unit = {},
    val onHalo: (Boolean) -> Unit = {},
    val onSound: (Boolean) -> Unit = {},
    val onAutoStart: (Boolean) -> Unit = {},
    val onKeepAlive: () -> Unit = {},
    val onPrivacy: () -> Unit = {},
    val onBack: () -> Unit = {},
)

private val Gutter = 28.dp

@Composable
private fun Header(title: String, onBack: () -> Unit) {
    Column {
        QuietButton("‹  返回", onBack, Modifier.padding(start = 0.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Palette.Ink)
    }
}

@Composable
fun SettingsScreen(state: SettingsState, version: String, actions: SettingsActions) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Gutter, vertical = 16.dp),
    ) {
        Header("设置", actions.onBack)
        Section("桌 宠")
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 14.dp)) {
            Text("大小", style = MaterialTheme.typography.bodyLarge, color = Palette.Ink, modifier = Modifier.weight(1f))
            Segmented(Settings.SIZE_LABELS, state.size, actions.onSize)
        }
        Hairline()
        ToggleRow("自由活动", "放着不管时，偶尔自己走走、飞飞、游游", state.wander, actions.onWander)
        Hairline()
        ToggleRow("淡淡的轮廓", "在纯白或纯黑的壁纸上也看得清", state.halo, actions.onHalo)
        Hairline()
        ToggleRow("声音", "点它时会叫；手机静音时一律不出声", state.sound, actions.onSound)
        Section("后 台")
        ToggleRow("开机后自动回到桌面", "仅当关机前它在桌面上时生效", state.autoStart, actions.onAutoStart)
        Hairline()
        LinkRow("让它不被系统清理", "各品牌手机的设置方法", actions.onKeepAlive)
        Section("关 于")
        LinkRow("隐私政策", "不联网，不收集任何信息", actions.onPrivacy)
        Hairline()
        Row(Modifier.padding(vertical = 16.dp)) {
            Text("版本", style = MaterialTheme.typography.bodyLarge, color = Palette.Ink, modifier = Modifier.weight(1f))
            Text(version, style = MaterialTheme.typography.bodyLarge, color = Palette.Muted)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(32.dp))
    Eyebrow(title)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ToggleRow(title: String, hint: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val accent = LocalAccent.current
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = accent, checkedThumbColor = Color.White,
                uncheckedTrackColor = Palette.Hairline, uncheckedThumbColor = Color.White, uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun LinkRow(title: String, hint: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
        }
        Text("›", fontSize = 22.sp, color = Palette.Muted)
    }
}

@Composable
private fun Segmented(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val accent = LocalAccent.current
    Row(
        Modifier.clip(RoundedCornerShape(18.dp)).border(1.dp, Palette.Hairline, RoundedCornerShape(18.dp)),
    ) {
        labels.forEachIndexed { i, label ->
            Box(
                Modifier.width(48.dp).height(34.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (i == selected) accent else Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = if (i == selected) Color.White else Palette.Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun KeepAliveScreen(manufacturer: String, onAppSettings: () -> Unit, onBatterySettings: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Gutter, vertical = 16.dp)) {
        Header("让它不被系统清理", onBack)
        Spacer(Modifier.height(12.dp))
        Text(
            "不少手机为了省电，会悄悄关掉后台应用，桌宠就会突然消失。按下面的方法把本命兽设为「不限制」即可。本应用本身非常省电：睡着时每秒只画几帧，熄屏完全停止。",
            style = MaterialTheme.typography.bodyMedium, color = Palette.Muted,
        )
        KeepAlive.ordered(manufacturer).forEach { b ->
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(b.name, style = MaterialTheme.typography.titleMedium, color = Palette.Ink)
                if (KeepAlive.matches(b, manufacturer)) {
                    Spacer(Modifier.width(8.dp))
                    Text("你的手机", fontSize = 11.sp, color = LocalAccent.current, letterSpacing = 1.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(b.steps, style = MaterialTheme.typography.bodyMedium, color = Palette.Ink)
        }
        Spacer(Modifier.height(12.dp))
        Text("不同系统版本的菜单名称可能略有不同。", style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
        Spacer(Modifier.height(28.dp))
        PillButton("打开本应用的系统设置", onAppSettings)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { QuietButton("打开电池优化列表", onBatterySettings) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Gutter, vertical = 16.dp)) {
        Header("隐私政策", onBack)
        Text("更新日期：${PrivacyText.UPDATED}", style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
        PrivacyText.sections.forEach { (title, body) ->
            Spacer(Modifier.height(24.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = Palette.Ink)
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Palette.Ink)
        }
        Spacer(Modifier.height(32.dp))
    }
}

/** First launch, before anything else: what the app does with data, in three lines. */
@Composable
fun PrivacyConsentScreen(onAgree: () -> Unit, onDisagree: () -> Unit, onReadPolicy: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = Gutter, vertical = 24.dp)) {
        Spacer(Modifier.weight(1f))
        Eyebrow("在 开 始 之 前")
        Spacer(Modifier.height(20.dp))
        Text("你的数据\n只属于你", style = MaterialTheme.typography.displayMedium, color = Palette.Ink)
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PrivacyText.summary.forEach { line ->
                Row {
                    Text("—", color = LocalAccent.current, modifier = Modifier.width(24.dp))
                    Text(line, style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        QuietButton("阅读完整隐私政策 ›", onReadPolicy)
        Spacer(Modifier.weight(1.3f))
        PillButton("同意并继续", onAgree)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { QuietButton("不同意", onDisagree) }
    }
}
