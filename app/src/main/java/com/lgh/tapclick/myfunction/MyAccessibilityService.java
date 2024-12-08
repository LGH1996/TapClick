package com.lgh.tapclick.myfunction;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {

    @SuppressLint("StaticFieldLeak")
    public static MainFunction mainFunction;

    @Override
    public void onCreate() {
        super.onCreate();
        mainFunction = new MainFunction(this);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (mainFunction != null) {
            mainFunction.onServiceConnected();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (mainFunction != null) {
            mainFunction.onAccessibilityEvent(event);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mainFunction != null) {
            mainFunction.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mainFunction != null) {
            mainFunction.onUnbind(intent);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainFunction = null;
    }

    @Override
    public void onInterrupt() {
    }
}
