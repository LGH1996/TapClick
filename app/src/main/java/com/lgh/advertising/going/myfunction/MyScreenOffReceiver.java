package com.lgh.advertising.going.myfunction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyScreenOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_SCREEN_OFF)) {
            if (MyAccessibilityService.mainFunction != null) {
                MyAccessibilityService.mainFunction.onScreenOff();
            }
            if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                MyAccessibilityServiceNoGesture.mainFunction.onScreenOff();
            }
        }
    }
}
