package com.lgh.advertising.myactivity;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.lgh.advertising.myclass.MyApplication;

public class AuthorizationActivity extends Activity {

    Context context;
    AppOpsManager appOps;
    PackageManager packageManager;

    ImageView accessibilityOnOffImg;
    ImageView batteryOnOffImg;
    ImageView alertWindowOnOffImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        context = getApplicationContext();
        appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        packageManager = getPackageManager();

        if (!MyApplication.myAppConfig.isVip) {
            View noVip = findViewById(R.id.no_vip);
            noVip.setVisibility(View.VISIBLE);
        }

        accessibilityOnOffImg = findViewById(R.id.accessibility_on_off_img);
        batteryOnOffImg = findViewById(R.id.batteryIgnore_on_off_img);
        alertWindowOnOffImg = findViewById(R.id.alert_window_on_off_img);
        RelativeLayout accessibilityOnOff = findViewById(R.id.accessibility_on_off);
        RelativeLayout batteryIgnoreOnOff = findViewById(R.id.batteryIgnore_on_off);
        RelativeLayout alertWindowOnOff = findViewById(R.id.alertWindow_on_off);
        View.OnClickListener onOffClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.accessibility_on_off:
                        Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intentAccessibility);
                        break;
                    case R.id.batteryIgnore_on_off:
                        if (((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())) {
                            Toast.makeText(context, "忽略电池优化权限已打开", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intentBatteryIgnore = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
                            if (intentBatteryIgnore.resolveActivity(packageManager) != null) {
                                startActivity(intentBatteryIgnore);
                            } else {
                                Toast.makeText(context, "授权窗口打开失败，请手动打开", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case R.id.alertWindow_on_off:
                        Intent intentAlertWindow = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        if (intentAlertWindow.resolveActivity(packageManager) != null) {
                            startActivity(intentAlertWindow);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                        break;
                }
            }
        };
        accessibilityOnOff.setOnClickListener(onOffClickListener);
        batteryIgnoreOnOff.setOnClickListener(onOffClickListener);
        alertWindowOnOff.setOnClickListener(onOffClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (Settings.canDrawOverlays(context)) {
            alertWindowOnOffImg.setImageResource(R.drawable.ok);
        } else {
            alertWindowOnOffImg.setImageResource(R.drawable.error);
        }
    }
}
