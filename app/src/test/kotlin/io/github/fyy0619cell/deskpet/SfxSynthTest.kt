package io.github.fyy0619cell.deskpet

import io.github.fyy0619cell.deskpet.sound.SfxSynth
import io.github.fyy0619cell.deskpet.species.Voice
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class SfxSynthTest {
    @Test
    fun everyVoiceIsShortAudibleAndNeverClips() {
        for (v in Voice.entries) {
            val pcm = SfxSynth.voice(v)
            val seconds = pcm.size.toFloat() / SfxSynth.RATE
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("$v lasts $seconds s", seconds in 0.2f..1.0f)
            assertTrue("$v peak $peak", peak in 10_000..20_000)
        }
    }

    @Test
    fun synthesisIsDeterministic() {
        assertArrayEquals(SfxSynth.voice(Voice.DINO), SfxSynth.voice(Voice.DINO))
    }

    /** Exports WAVs so the sounds can be auditioned on a PC. */
    @Test
    fun exportWavs() {
        val dir = File(System.getProperty("gallery.dir") ?: return, "sounds").apply { mkdirs() }
        for (v in Voice.entries) writeWav(File(dir, "${v.name.lowercase()}.wav"), SfxSynth.voice(v))
        writeWav(File(dir, "nom.wav"), SfxSynth.nom())
    }

    private fun writeWav(f: File, pcm: ShortArray) {
        DataOutputStream(FileOutputStream(f)).use { out ->
            fun le32(v: Int) = out.write(byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte()))
            fun le16(v: Int) = out.write(byteArrayOf(v.toByte(), (v shr 8).toByte()))
            out.writeBytes("RIFF"); le32(36 + pcm.size * 2); out.writeBytes("WAVE")
            out.writeBytes("fmt "); le32(16); le16(1); le16(1); le32(SfxSynth.RATE); le32(SfxSynth.RATE * 2); le16(2); le16(16)
            out.writeBytes("data"); le32(pcm.size * 2)
            for (s in pcm) le16(s.toInt())
        }
    }
}
