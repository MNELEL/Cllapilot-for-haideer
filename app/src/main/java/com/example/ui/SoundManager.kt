package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf

enum class SoundTheme(val displayName: String) {
    CLASSIC("קלאסי"),
    MODERN("מודרני"),
    SCI_FI("מדע בדיוני"),
    SILENT("השתק")
}

/**
 * Lightweight Sound & Haptics Library.
 * Uses ToneGenerator for 0-latency audio cues and Vibrator for haptics.
 */
object SoundManager {
    private var toneGen: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    
    var currentTheme = mutableStateOf(SoundTheme.MODERN)
    var isSoundEnabled = mutableStateOf(true)

    fun init(context: Context) {
        val appContext = context.applicationContext
        try {
            val sharedPref = appContext.getSharedPreferences("classpro_prefs", Context.MODE_PRIVATE)
            val themeStr = sharedPref.getString("sound_theme", SoundTheme.MODERN.name)
            currentTheme.value = SoundTheme.valueOf(themeStr ?: SoundTheme.MODERN.name)
            isSoundEnabled.value = sharedPref.getBoolean("is_sound_enabled", true)
        } catch (e: Exception) {
            currentTheme.value = SoundTheme.MODERN
            isSoundEnabled.value = true
        }

        if (toneGen == null) {
            try {
                // Use a modest volume (e.g., 70% of max tone volume)
                toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (e: Exception) {
                Log.e("SoundManager", "Failed to initialize ToneGenerator", e)
            }
        }
        
        if (vibrator == null) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorContext = try {
                    appContext.createAttributionContext("ClassPro")
                } catch (e: Exception) {
                    appContext
                }
                val vibratorManager = vibratorContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }
    }

    fun updateTheme(context: Context, theme: SoundTheme) {
        currentTheme.value = theme
        val appContext = context.applicationContext
        try {
            val sharedPref = appContext.getSharedPreferences("classpro_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putString("sound_theme", theme.name).apply()
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to save theme preference", e)
        }
    }

    fun updateSoundEnabled(context: Context, enabled: Boolean) {
        isSoundEnabled.value = enabled
        val appContext = context.applicationContext
        try {
            val sharedPref = appContext.getSharedPreferences("classpro_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("is_sound_enabled", enabled).apply()
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to save sound enabled preference", e)
        }
    }

    fun release() {
        toneGen?.release()
        toneGen = null
        vibrator = null
    }

    private fun triggerHaptic(duration: Long = 15L, amplitude: Int = 50) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Haptic feedback failed", e)
        }
    }

    fun playClick() {
        if (!isSoundEnabled.value || currentTheme.value == SoundTheme.SILENT) return
        triggerHaptic(10L, 30)
        when (currentTheme.value) {
            SoundTheme.CLASSIC -> toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            SoundTheme.SCI_FI -> toneGen?.startTone(ToneGenerator.TONE_DTMF_A, 30)
            else -> toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 20) // MODERN
        }
    }

    fun playPop() {
        if (!isSoundEnabled.value || currentTheme.value == SoundTheme.SILENT) return
        triggerHaptic(10L, 40)
        when (currentTheme.value) {
            SoundTheme.CLASSIC -> toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 30)
            SoundTheme.SCI_FI -> toneGen?.startTone(ToneGenerator.TONE_DTMF_B, 30)
            else -> toneGen?.startTone(ToneGenerator.TONE_DTMF_0, 30) // MODERN
        }
    }

    fun playModalOpen() {
        if (!isSoundEnabled.value || currentTheme.value == SoundTheme.SILENT) return
        triggerHaptic(20L, 80)
        when (currentTheme.value) {
            SoundTheme.CLASSIC -> toneGen?.startTone(ToneGenerator.TONE_SUP_INTERCEPT, 50)
            SoundTheme.SCI_FI -> toneGen?.startTone(ToneGenerator.TONE_DTMF_D, 50)
            else -> toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 40) // MODERN
        }
    }

    fun playTaskComplete() {
        if (!isSoundEnabled.value || currentTheme.value == SoundTheme.SILENT) return
        triggerHaptic(40L, 100)
        when (currentTheme.value) {
            SoundTheme.CLASSIC -> toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
            SoundTheme.SCI_FI -> toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
            else -> toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 100) // MODERN
        }
    }

    fun playError() {
        if (!isSoundEnabled.value) return
        if (currentTheme.value == SoundTheme.SILENT) {
            triggerHaptic(100L, 200)
            return
        }
        triggerHaptic(100L, 200) 
        when (currentTheme.value) {
            SoundTheme.CLASSIC -> toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
            SoundTheme.SCI_FI -> toneGen?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
            else -> toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 150) // MODERN
        }
    }

    fun playDelete() {
        if (!isSoundEnabled.value || currentTheme.value == SoundTheme.SILENT) return
        triggerHaptic(30L, 120) 
        when (currentTheme.value) {
            SoundTheme.CLASSIC -> toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 100)
            SoundTheme.SCI_FI -> toneGen?.startTone(ToneGenerator.TONE_DTMF_S, 100)
            else -> toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 80) // MODERN
        }
    }

    fun playNotification() {
        if (!isSoundEnabled.value || currentTheme.value == SoundTheme.SILENT) return
        triggerHaptic(30L, 80)
        when (currentTheme.value) {
            SoundTheme.CLASSIC -> toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
            SoundTheme.SCI_FI -> toneGen?.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 100)
            else -> toneGen?.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 100) // MODERN
        }
    }
}
