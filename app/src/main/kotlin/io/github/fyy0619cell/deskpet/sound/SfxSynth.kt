package io.github.fyy0619cell.deskpet.sound

import io.github.fyy0619cell.deskpet.render.Rng
import io.github.fyy0619cell.deskpet.species.Voice
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

/**
 * Builds every sound from sine harmonics, a pitch curve, an envelope and optional noise.
 * Pure Kotlin and deterministic, so sounds can be tested and exported as WAV off-device.
 */
object SfxSynth {
    const val RATE = 22050
    private const val TAU = (2 * PI).toFloat()
    private const val PEAK = 0.55f

    private class Track(seconds: Float) {
        val data = FloatArray((seconds * RATE).toInt())
        val noise = Rng(0x5EED)
    }

    /** Pitch curve through (time fraction, Hz) points, linearly interpolated. */
    private fun curve(vararg pts: Pair<Float, Float>): (Float) -> Float = { f ->
        val i = pts.indexOfLast { it.first <= f }.coerceIn(0, pts.size - 2)
        val (f0, h0) = pts[i]
        val (f1, h1) = pts[i + 1]
        h0 + (h1 - h0) * ((f - f0) / max(1e-4f, f1 - f0)).coerceIn(0f, 1f)
    }

    private fun Track.tone(
        start: Float, dur: Float, pitch: (Float) -> Float, harmonics: FloatArray,
        amp: Float = 1f, attack: Float = 0.02f, release: Float = 0.08f,
        vibHz: Float = 0f, vibDepth: Float = 0f, noise: Float = 0f, amHz: Float = 0f, amDepth: Float = 0f,
    ) {
        val s0 = (start * RATE).toInt()
        val n = (dur * RATE).toInt()
        var phase = 0f
        for (k in 0 until n) {
            val i = s0 + k
            if (i >= data.size) break
            val t = k.toFloat() / RATE
            val f = k.toFloat() / n
            val hz = pitch(f) + if (vibHz > 0f) sin(TAU * vibHz * t) * vibDepth else 0f
            phase += TAU * hz / RATE
            var s = 0f
            for (h in harmonics.indices) s += harmonics[h] * sin(phase * (h + 1))
            if (noise > 0f) s += noise * (this.noise.nextFloat() * 2f - 1f)
            if (amHz > 0f) s *= 1f - amDepth * (0.5f + 0.5f * sin(TAU * amHz * t))
            data[i] += s * amp * envelope(t, dur, attack, release)
        }
    }

    private fun Track.hiss(start: Float, dur: Float, amp: Float, smooth: Float, attack: Float = 0.004f, release: Float = 0.02f) {
        val s0 = (start * RATE).toInt()
        val n = (dur * RATE).toInt()
        var y = 0f
        for (k in 0 until n) {
            val i = s0 + k
            if (i >= data.size) break
            y += smooth * ((noise.nextFloat() * 2f - 1f) - y)
            data[i] += y * amp * envelope(k.toFloat() / RATE, dur, attack, release)
        }
    }

    private fun envelope(t: Float, dur: Float, attack: Float, release: Float): Float {
        val a = (t / attack).coerceIn(0f, 1f)
        val r = ((dur - t) / release).coerceIn(0f, 1f)
        return a * a * (3f - 2f * a) * r * r * (3f - 2f * r)
    }

    private fun Track.finish(): ShortArray {
        val peak = data.maxOf { abs(it) }.coerceAtLeast(1e-6f)
        val gain = PEAK / peak * Short.MAX_VALUE
        return ShortArray(data.size) { (data[it] * gain).toInt().coerceIn(-32768, 32767).toShort() }
    }

    private fun h(vararg v: Float) = v

    fun voice(v: Voice): ShortArray = when (v) {
        Voice.MEOW -> Track(0.45f).apply {
            tone(0f, 0.44f, curve(0f to 520f, 0.35f to 760f, 1f to 470f), h(1f, 0.45f, 0.25f, 0.12f), attack = 0.04f, release = 0.12f, vibHz = 5f, vibDepth = 8f)
        }
        Voice.ROAR -> Track(0.58f).apply {
            tone(0f, 0.56f, curve(0f to 240f, 1f to 170f), h(1f, 0.7f, 0.5f, 0.35f, 0.2f), noise = 0.35f, vibHz = 7f, vibDepth = 10f, attack = 0.06f, release = 0.2f)
        }
        Voice.GROWL -> Track(0.48f).apply {
            tone(0f, 0.46f, curve(0f to 150f, 1f to 128f), h(1f, 0.6f, 0.4f), noise = 0.15f, amHz = 28f, amDepth = 0.6f, attack = 0.05f, release = 0.15f)
        }
        Voice.BARK -> Track(0.34f).apply {
            for (s in floatArrayOf(0f, 0.2f)) tone(s, 0.11f, curve(0f to 520f, 1f to 300f), h(1f, 0.6f, 0.4f, 0.2f), noise = 0.25f, attack = 0.005f, release = 0.06f)
        }
        Voice.TRUMPET -> Track(0.52f).apply {
            tone(0f, 0.5f, curve(0f to 420f, 0.4f to 640f, 1f to 620f), h(1f, 0.8f, 0.6f, 0.5f, 0.4f, 0.3f), vibHz = 6f, vibDepth = 12f, attack = 0.05f, release = 0.12f)
        }
        Voice.SQUEAK -> Track(0.3f).apply {
            for (s in floatArrayOf(0f, 0.16f)) tone(s, 0.12f, curve(0f to 1500f, 0.5f to 2100f, 1f to 1600f), h(1f, 0.2f), attack = 0.01f, release = 0.04f)
        }
        Voice.DINO -> Track(0.75f).apply {
            tone(0f, 0.72f, curve(0f to 210f, 1f to 120f), h(1f, 0.8f, 0.6f, 0.4f, 0.3f), noise = 0.3f, vibHz = 5f, vibDepth = 14f, attack = 0.08f, release = 0.25f)
        }
        Voice.CHIRP -> Track(0.3f).apply {
            for (s in floatArrayOf(0f, 0.1f, 0.2f)) tone(s, 0.07f, curve(0f to 2400f, 1f to 3400f), h(1f, 0.3f), attack = 0.005f, release = 0.03f)
        }
        Voice.HOOT -> Track(0.58f).apply {
            for (s in floatArrayOf(0f, 0.32f)) tone(s, 0.22f, curve(0f to 400f, 1f to 360f), h(1f, 0.15f), attack = 0.05f, release = 0.1f)
        }
        Voice.HONK -> Track(0.4f).apply {
            for (s in floatArrayOf(0f, 0.22f)) tone(s, 0.16f, curve(0f to 480f, 1f to 440f), h(1f, 0.1f, 0.7f, 0.1f, 0.5f), noise = 0.05f, attack = 0.01f, release = 0.05f)
        }
        Voice.BUZZ -> Track(0.52f).apply {
            tone(0f, 0.5f, curve(0f to 230f, 1f to 210f), h(1f, 0.5f, 0.33f, 0.25f, 0.2f, 0.17f), vibHz = 3f, vibDepth = 15f, amHz = 45f, amDepth = 0.5f, attack = 0.05f, release = 0.15f)
        }
        Voice.CLICK -> Track(0.3f).apply {
            for (i in 0 until 5) hiss(i * 0.06f, 0.006f, 1f, 0.8f, attack = 0.0005f, release = 0.003f)
        }
        Voice.BUBBLE -> Track(0.33f).apply {
            for (s in floatArrayOf(0f, 0.12f, 0.24f)) tone(s, 0.07f, curve(0f to 320f, 1f to 980f), h(1f), attack = 0.005f, release = 0.03f)
        }
        Voice.WHALE -> Track(0.92f).apply {
            tone(0f, 0.9f, curve(0f to 280f, 0.45f to 460f, 1f to 340f), h(1f, 0.3f, 0.1f), vibHz = 4f, vibDepth = 10f, attack = 0.2f, release = 0.3f, amp = 0.8f)
        }
        Voice.CROAK -> Track(0.36f).apply {
            for (s in floatArrayOf(0f, 0.2f)) tone(s, 0.14f, curve(0f to 170f, 1f to 150f), h(1f, 0.8f, 0.6f, 0.5f), noise = 0.1f, amHz = 55f, amDepth = 0.8f, attack = 0.01f, release = 0.05f)
        }
        Voice.HISS -> Track(0.47f).apply { hiss(0f, 0.45f, 0.7f, 0.35f, attack = 0.05f, release = 0.2f) }
    }.finish()

    fun nom(): ShortArray = Track(0.42f).apply {
        for (s in floatArrayOf(0f, 0.15f, 0.3f)) tone(s, 0.09f, curve(0f to 300f, 1f to 220f), h(1f, 0.3f), attack = 0.005f, release = 0.05f)
    }.finish()
}
