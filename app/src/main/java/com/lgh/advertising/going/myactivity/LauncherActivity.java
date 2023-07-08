package com.lgh.advertising.going.myactivity;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.lgh.advertising.going.R;
import com.lgh.advertising.going.myfunction.MyAccessibilityService;
import com.lgh.advertising.going.myfunction.MyAccessibilityServiceNoGesture;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if (!TextUtils.isEmpty(getIntent().getStringExtra("requestMediaProject"))) {
            MediaProjectionManager mediaProjectionManager = getSystemService(MediaProjectionManager.class);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 0x01);
        } else {
            final String xrxsApp = "com.client.xrxs.com.xrxsapp";
            Intent intent = getPackageManager().getLaunchIntentForPackage(xrxsApp);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == RESULT_OK && data != null) {
            if (MyAccessibilityService.mainFunction != null) {
                MyAccessibilityService.mainFunction.initCapture(resultCode, data);
                finishAndRemoveTask();
            }
            if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                MyAccessibilityServiceNoGesture.mainFunction.initCapture(resultCode, data);
                finishAndRemoveTask();
            }
        } else {
            Toast.makeText(this, "截屏请求失败", Toast.LENGTH_SHORT).show();
        }
    }
}