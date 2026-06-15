package com.example.appctruyn;

import android.app.Application;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Áp dụng giao diện ngay khi khởi chạy ứng dụng
        ThemeHelper.applyTheme(this);
    }
}
