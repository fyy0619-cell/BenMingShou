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
}
