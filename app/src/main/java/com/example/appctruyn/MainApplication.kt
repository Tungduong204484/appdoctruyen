package com.example.appctruyn

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Áp dụng giao diện ngay khi khởi chạy ứng dụng
        ThemeHelper.applyTheme(this)
    }
}