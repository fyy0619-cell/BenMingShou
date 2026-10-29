package io.github.fyy0619cell.deskpet.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import io.github.fyy0619cell.deskpet.pet.PetStore
import io.github.fyy0619cell.deskpet.pet.Settings as PetSettings

/**
 * Brings the pet back only if the user had it on screen: after a reboot (and only when
 * "start on boot" is on), or after the app itself was updated.
 */
class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val s = PetSettings(context)
        val allowed = when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> s.autoStart && s.overlayWanted
            Intent.ACTION_MY_PACKAGE_REPLACED -> s.overlayWanted
            else -> false
        }
        if (!allowed || PetStore(context).adoption() == null || !Settings.canDrawOverlays(context)) return
        runCatching { OverlayService.start(context) }
    }
}
