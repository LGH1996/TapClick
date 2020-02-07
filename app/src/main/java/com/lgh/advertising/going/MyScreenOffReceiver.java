package com.lgh.advertising.going;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyScreenOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        try {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    if (MyAccessibilityService.mainFunction != null) {
                        MyAccessibilityService.mainFunction.handler.sendEmptyMessage(0x01);
                    }
                    if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                        MyAccessibilityServiceNoGesture.mainFunction.handler.sendEmptyMessage(0x01);
                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
