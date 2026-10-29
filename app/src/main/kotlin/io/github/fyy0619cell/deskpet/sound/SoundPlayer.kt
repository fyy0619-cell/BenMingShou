package io.github.fyy0619cell.deskpet.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import io.github.fyy0619cell.deskpet.pet.Settings
import io.github.fyy0619cell.deskpet.species.Voice

/**
 * Plays synthesized clips through static AudioTracks on the sonification stream.
 * Silent whenever the phone is on silent/vibrate or the in-app switch is off.
 */
class SoundPlayer(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val settings = Settings(context)
    private val tracks = HashMap<String, AudioTrack>()

    fun voice(v: Voice) = play(v.name) { SfxSynth.voice(v) }

    fun nom() = play("NOM") { SfxSynth.nom() }

    private fun play(key: String, pcm: () -> ShortArray) {
        if (!settings.soundOn || audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        runCatching {
            val track = tracks.getOrPut(key) { build(pcm()) }
            track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    private fun build(pcm: ShortArray): AudioTrack =
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SfxSynth.RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
            .also { it.write(pcm, 0, pcm.size) }

    fun release() {
        tracks.values.forEach { runCatching { it.release() } }
        tracks.clear()
    }
}
