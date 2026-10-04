package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundSynth {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var isEnabled: Boolean = true

    private val sampleRate = 22050
    private val jumpBuffer: ShortArray by lazy { generateSweep(260.0, 680.0, 0.12) }
    private val landBuffer: ShortArray by lazy { generateThud(120.0, 45.0, 0.08) }
    private val popBuffer: ShortArray by lazy { generateClick(1400.0, 200.0, 0.07) }
    private val deathBuffer: ShortArray by lazy { generateNoiseCrunch(0.28) }
    private val trollBuffer: ShortArray by lazy { generateTrollBuzzer(0.35) }
    private val winBuffer: ShortArray by lazy { generateWinArpeggio() }
    private val clickBuffer: ShortArray by lazy { generateClick(880.0, 440.0, 0.04) }

    fun playJump() = playAsync(jumpBuffer)
    fun playLand() = playAsync(landBuffer)
    fun playTrapTrigger() = playAsync(popBuffer)
    fun playDeath() = playAsync(deathBuffer)
    fun playTroll() = playAsync(trollBuffer)
    fun playWin() = playAsync(winBuffer)
    fun playClick() = playAsync(clickBuffer)

    private fun playAsync(pcm: ShortArray) {
        if (!isEnabled) return
        scope.launch {
            try {
                val bufferSize = pcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                // Sleep roughly duration of sound then release
                val durationMs = (pcm.size * 1000L / sampleRate) + 50L
                kotlinx.coroutines.delay(durationMs)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio failures on emulator or restricted audio devices
            }
        }
    }

    private fun generateSweep(startFreq: Double, endFreq: Double, durationSec: Double): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val data = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * freq / sampleRate
            val envelope = (1.0 - progress)
            val sample = (sin(phase) * envelope * 24000.0).toInt().coerceIn(-32768, 32767)
            data[i] = sample.toShort()
        }
        return data
    }

    private fun generateThud(startFreq: Double, endFreq: Double, durationSec: Double): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val data = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * (1.0 - progress)
            phase += 2.0 * PI * freq / sampleRate
            val envelope = (1.0 - progress) * (1.0 - progress)
            val sample = (sin(phase) * envelope * 22000.0).toInt().coerceIn(-32768, 32767)
            data[i] = sample.toShort()
        }
        return data
    }

    private fun generateClick(startFreq: Double, endFreq: Double, durationSec: Double): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val data = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * freq / sampleRate
            val envelope = kotlin.math.exp(-progress * 8.0)
            val sample = (sin(phase) * envelope * 26000.0).toInt().coerceIn(-32768, 32767)
            data[i] = sample.toShort()
        }
        return data
    }

    private fun generateNoiseCrunch(durationSec: Double): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val data = ShortArray(count)
        val random = Random(42)
        var lowTonePhase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val envelope = (1.0 - progress)
            val noise = (random.nextDouble() * 2.0 - 1.0)
            lowTonePhase += 2.0 * PI * 130.0 / sampleRate
            val lowTone = sin(lowTonePhase)
            val mixed = (noise * 0.7 + lowTone * 0.3) * envelope * 28000.0
            data[i] = mixed.toInt().coerceIn(-32768, 32767).toShort()
        }
        return data
    }

    private fun generateTrollBuzzer(durationSec: Double): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val data = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            // Descending cartoon pitch
            val freq = 260.0 - progress * 90.0
            phase += 2.0 * PI * freq / sampleRate
            // square wave for comedic buzzer timbre
            val raw = if (sin(phase) > 0) 1.0 else -1.0
            val envelope = (1.0 - progress * 0.4)
            data[i] = (raw * envelope * 18000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return data
    }

    private fun generateWinArpeggio(): ShortArray {
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val noteDuration = 0.10
        val count = (sampleRate * noteDuration * notes.size).toInt()
        val data = ShortArray(count)
        val noteSamples = (sampleRate * noteDuration).toInt()

        for (n in notes.indices) {
            val freq = notes[n]
            var phase = 0.0
            val startIdx = n * noteSamples
            for (i in 0 until noteSamples) {
                val idx = startIdx + i
                if (idx >= count) break
                val progress = i.toDouble() / noteSamples
                phase += 2.0 * PI * freq / sampleRate
                val envelope = (1.0 - progress * 0.7)
                // Add soft harmonics
                val wave = sin(phase) * 0.8 + sin(phase * 2.0) * 0.2
                data[idx] = (wave * envelope * 22000.0).toInt().coerceIn(-32768, 32767).toShort()
            }
        }
        return data
    }
}
