package com.example.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ThemeManager {
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun init(context: Context) {
        try {
            val sharedPref = context.applicationContext.getSharedPreferences("classpro_prefs", Context.MODE_PRIVATE)
            _isDarkTheme.value = sharedPref.getBoolean("is_dark_theme", false)
        } catch (e: Exception) {
            _isDarkTheme.value = false
        }
    }

    fun toggleTheme(context: Context) {
        setDarkTheme(context, !_isDarkTheme.value)
    }

    fun setDarkTheme(context: Context, dark: Boolean) {
        _isDarkTheme.value = dark
        try {
            val sharedPref = context.applicationContext.getSharedPreferences("classpro_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("is_dark_theme", dark).apply()
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
