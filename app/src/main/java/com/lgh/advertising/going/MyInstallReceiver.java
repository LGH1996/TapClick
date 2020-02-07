package com.lgh.advertising.going;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if (action!=null){
            if (action.equals(Intent.ACTION_PACKAGE_ADDED)){

            }
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)){

            }
        }
        if (MyAccessibilityService.mainFunction != null) {
            MyAccessibilityService.mainFunction.handler.sendEmptyMessage(0x03);
        }
        if (MyAccessibilityServiceNoGesture.mainFunction != null) {
            MyAccessibilityServiceNoGesture.mainFunction.handler.sendEmptyMessage(0x03);
        }
    }
}
