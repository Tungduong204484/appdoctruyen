package com.example.appctruyn

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.util.Calendar

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_AUTO = 2

    fun applyTheme(context: Context) {
        val mode = getThemePreference(context)
        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_AUTO -> {
                if (isDayTime()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }

    fun setThemePreference(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME_MODE, mode)
            .apply()
        applyTheme(context)
    }

    fun getThemePreference(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME_MODE, MODE_AUTO)
    }

    private fun isDayTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 7..21
    }

    fun getThemeName(context: Context, mode: Int): String {
        return when (mode) {
            MODE_LIGHT -> context.getString(R.string.theme_light)
            MODE_DARK -> context.getString(R.string.theme_dark)
            else -> context.getString(R.string.theme_auto)
        }
    }
}
