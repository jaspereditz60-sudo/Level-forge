package com.example.audio

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticsHelper(context: Context) {
    private val appContext = context.applicationContext
    var isEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrateJump() {
        if (!isEnabled) return
        vibrateMillis(15, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun vibrateDeath() {
        if (!isEnabled) return
        vibrateMillis(90, 255)
    }

    fun vibrateTrap() {
        if (!isEnabled) return
        vibrateMillis(35, 180)
    }

    fun vibrateWin() {
        if (!isEnabled) return
        vibratePattern(longArrayOf(0, 30, 50, 60))
    }

    private fun vibrateMillis(ms: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(ms, clampedAmp))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(ms)
            }
        } catch (_: Exception) {
            // Ignore vibration errors
        }
    }

    private fun vibratePattern(timings: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {
            // Ignore vibration errors
        }
    }
}
