package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class StudioSoundEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var isMuted: Boolean = false

    private val sampleRate = 22050

    private val accentClickPcm: ShortArray by lazy { generateClickPcm(startFreq = 1080.0, endFreq = 320.0, durationMs = 55, gain = 0.55) }
    private val beatClickPcm: ShortArray by lazy { generateClickPcm(startFreq = 740.0, endFreq = 220.0, durationMs = 50, gain = 0.35) }
    private val phaseChimePcm: ShortArray by lazy { generateTonePcm(freqs = listOf(660.0 to 180), gain = 0.25) }
    private val completeChimePcm: ShortArray by lazy {
        generateTonePcm(
            freqs = listOf(523.25 to 120, 659.25 to 120, 783.99 to 280),
            gain = 0.28
        )
    }

    fun playClick(isAccent: Boolean = false) {
        if (isMuted) return
        val pcm = if (isAccent) accentClickPcm else beatClickPcm
        playPcmAsync(pcm)
    }

    fun playChime(isComplete: Boolean = false) {
        if (isMuted) return
        val pcm = if (isComplete) completeChimePcm else phaseChimePcm
        playPcmAsync(pcm)
    }

    private fun playPcmAsync(pcm: ShortArray) {
        scope.launch {
            var track: AudioTrack? = null
            try {
                val byteCount = pcm.size * 2
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(byteCount)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val sleepMs = (pcm.size * 1000L) / sampleRate + 40L
                kotlinx.coroutines.delay(sleepMs)
            } catch (_: Throwable) {
                // Ignore AudioTrack failure on devices/tests without audio hardware
            } finally {
                try {
                    track?.stop()
                } catch (_: Throwable) {}
                try {
                    track?.release()
                } catch (_: Throwable) {}
            }
        }
    }

    private fun generateClickPcm(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        gain: Double
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * t
            phase += 2.0 * PI * freq / sampleRate
            // Triangle-like punchy click with exponential decay
            val tri = 2.0 * kotlin.math.abs(2.0 * ((phase / (2.0 * PI)) - kotlin.math.floor(phase / (2.0 * PI) + 0.5))) - 1.0
            val env = exp(-5.5 * t)
            out[i] = (tri * env * gain * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun generateTonePcm(
        freqs: List<Pair<Double, Int>>,
        gain: Double
    ): ShortArray {
        val segments = freqs.map { (f, ms) -> f to ((sampleRate * ms) / 1000) }
        val totalSamples = segments.sumOf { it.second }
        val out = ShortArray(totalSamples)
        var offset = 0
        for ((freq, count) in segments) {
            var phase = 0.0
            for (i in 0 until count) {
                val t = i.toDouble() / count
                phase += 2.0 * PI * freq / sampleRate
                val attack = if (i < sampleRate / 100) i.toDouble() / (sampleRate / 100) else 1.0
                val decay = exp(-3.0 * t)
                out[offset + i] = (sin(phase) * attack * decay * gain * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            offset += count
        }
        return out
    }
}
