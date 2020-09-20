package com.lgh.advertising.going.myactivity;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.lgh.advertising.going.myfunction.MyAccessibilityService;
import com.lgh.advertising.going.myfunction.MyAccessibilityServiceNoGesture;
import com.lgh.advertising.going.R;
import com.lgh.advertising.going.databinding.ActivityAuthorizationBinding;

public class AuthorizationActivity extends BaseActivity {

    private Context context;
    private AppOpsManager appOps;
    private PackageManager packageManager;
    private ActivityAuthorizationBinding authorizationBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorizationBinding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
        setContentView(authorizationBinding.getRoot());
        context = getApplicationContext();
        appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        packageManager = getPackageManager();

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
        authorizationBinding.accessibilityOnOff.setOnClickListener(onOffClickListener);
        authorizationBinding.batteryIgnoreOnOff.setOnClickListener(onOffClickListener);
        authorizationBinding.alertWindowOnOff.setOnClickListener(onOffClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
            authorizationBinding.accessibilityOnOffImg.setImageResource(R.drawable.error);
        } else {
            authorizationBinding.accessibilityOnOffImg.setImageResource(R.drawable.ok);
        }
        if (((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())) {
            authorizationBinding.batteryIgnoreOnOffImg.setImageResource(R.drawable.ok);
        } else {
            authorizationBinding.batteryIgnoreOnOffImg.setImageResource(R.drawable.error);
        }
        if (Settings.canDrawOverlays(context)) {
            authorizationBinding.alertWindowOnOffImg.setImageResource(R.drawable.ok);
        } else {
            authorizationBinding.alertWindowOnOffImg.setImageResource(R.drawable.error);
        }
    }
}
