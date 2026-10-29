package io.github.fyy0619cell.deskpet.pet

import android.content.Context

class Settings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var soundOn: Boolean
        get() = prefs.getBoolean("sound_on", true)
        set(v) = prefs.edit().putBoolean("sound_on", v).apply()

    var wanderOn: Boolean
        get() = prefs.getBoolean("wander_on", true)
        set(v) = prefs.edit().putBoolean("wander_on", v).apply()

    /** 0 small, 1 medium, 2 large. */
    var size: Int
        get() = prefs.getInt("size", 1).coerceIn(0, 2)
        set(v) = prefs.edit().putInt("size", v.coerceIn(0, 2)).apply()

    var halo: Boolean
        get() = prefs.getBoolean("halo", true)
        set(v) = prefs.edit().putBoolean("halo", v).apply()

    /** Opt-in: only then does a reboot bring the pet back. */
    var autoStart: Boolean
        get() = prefs.getBoolean("auto_start", false)
        set(v) = prefs.edit().putBoolean("auto_start", v).apply()

    /** The user wants the pet on the desktop (cleared when they put it away). */
    var overlayWanted: Boolean
        get() = prefs.getBoolean("overlay_wanted", false)
        set(v) = prefs.edit().putBoolean("overlay_wanted", v).apply()

    var privacyAccepted: Boolean
        get() = prefs.getBoolean("privacy_accepted", false)
        set(v) = prefs.edit().putBoolean("privacy_accepted", v).commit().let { }

    val sizeDp: Int get() = SIZES_DP[size]

    companion object {
        val SIZES_DP = intArrayOf(96, 120, 150)
        val SIZE_LABELS = listOf("小", "中", "大")
    }
}
