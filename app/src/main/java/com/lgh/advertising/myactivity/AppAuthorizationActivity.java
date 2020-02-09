package com.lgh.advertising.myactivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lgh.advertising.going.MyAccessibilityService;
import com.lgh.advertising.going.MyAccessibilityServiceNoGesture;
import com.lgh.advertising.going.R;

public class AppAuthorizationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_authorization);
        RelativeLayout accessibilityOnOff = findViewById(R.id.accessibility_on_off);
        RelativeLayout batteryOnOff = findViewById(R.id.batteryIgnore_on_off);
        RelativeLayout alertWindowOnOff = findViewById(R.id.alert_window_off);
        ImageView.OnClickListener onOffClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.accessibility_on_off:
                        Intent intentA = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intentA);
                        break;
                    case R.id.batteryIgnore_on_off:
                        if (((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())) {
                            Toast.makeText(AppAuthorizationActivity.this, "忽略电池优化权限已打开", Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intentB = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
                            ResolveInfo resolveInfo = getPackageManager().resolveActivity(intentB, PackageManager.MATCH_ALL);
                            if (resolveInfo != null) {
                                startActivity(intentB);
                            } else {
                                Toast.makeText(AppAuthorizationActivity.this, "授权窗口打开失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case R.id.alert_window_off:
                        Intent intentC = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intentC, PackageManager.MATCH_ALL);
                        if (resolveInfo != null) {
                            startActivity(intentC);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                        break;
                }
            }
        };
        accessibilityOnOff.setOnClickListener(onOffClickListener);
        batteryOnOff.setOnClickListener(onOffClickListener);
        alertWindowOnOff.setOnClickListener(onOffClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView accessibilityOnOffImg = findViewById(R.id.accessibility_on_off_img);
        ImageView batteryOnOffImg = findViewById(R.id.batteryIgnore_on_off_img);
        ImageView alertWindowOnOffImg = findViewById(R.id.alert_window_on_off_img);
        if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
            accessibilityOnOffImg.setImageResource(R.drawable.error);
        } else {
            accessibilityOnOffImg.setImageResource(R.drawable.ok);
        }
        if (((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())) {
            batteryOnOffImg.setImageResource(R.drawable.ok);
        } else {
            batteryOnOffImg.setImageResource(R.drawable.error);
        }
        if (Settings.canDrawOverlays(this)) {
            alertWindowOnOffImg.setImageResource(R.drawable.ok);
        } else {
            alertWindowOnOffImg.setImageResource(R.drawable.error);
        }
    }
}
