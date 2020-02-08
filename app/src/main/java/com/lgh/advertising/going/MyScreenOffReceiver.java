package com.lgh.advertising.going;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyScreenOffReceiver extends BroadcastReceiver {

    public static String TAG = "MyScreenOffReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        try {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (MyAccessibilityService.mainFunction != null || MyAccessibilityServiceNoGesture.mainFunction != null){
                    MainFunction.handler.sendEmptyMessage(0x01);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
