package com.example.appctruyn;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    public static void applyTheme(Context context) {
        // Luôn luôn áp dụng chế độ tối (Dark Mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static int getThemePreference(Context context) {
        return 1; // Luôn trả về Dark
    }

    public static String getThemeName(Context context, int mode) {
        return "Tối";
    }
}
