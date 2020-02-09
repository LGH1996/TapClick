package com.lgh.advertising.going;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityServiceNoGesture extends AccessibilityService {

    private int create_num, connect_num;
    public static MainFunction mainFunction;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            create_num = 0;
            connect_num = 0;
            create_num++;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (++connect_num != create_num) {
            throw new RuntimeException("无障碍服务出现异常");
        }
        mainFunction = new MainFunction(this);
        mainFunction.onServiceConnected();
        if (MyAccessibilityService.mainFunction != null) {
            MyAccessibilityService.mainFunction.closeService();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mainFunction.onAccessibilityEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mainFunction.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mainFunction.onUnbind(intent);
        mainFunction = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {
    }
}
