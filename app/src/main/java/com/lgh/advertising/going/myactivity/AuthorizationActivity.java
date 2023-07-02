package com.lgh.advertising.going.myactivity;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.lgh.advertising.going.R;
import com.lgh.advertising.going.databinding.ActivityAuthorizationBinding;
import com.lgh.advertising.going.myclass.MyApplication;
import com.lgh.advertising.going.myfunction.MyDeviceAdminReceiver;
import com.lgh.advertising.going.myfunction.MyUtils;

public class AuthorizationActivity extends BaseActivity {

    private ActivityAuthorizationBinding authorizationBinding;
    private Context context;
    private MyUtils myUtils;
    private Handler handler;
    private PackageManager packageManager;
    private DevicePolicyManager devicePolicyManager;
    private PowerManager powerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorizationBinding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
        setContentView(authorizationBinding.getRoot());
        context = getApplicationContext();
        myUtils = MyApplication.myUtils;
        handler = new Handler();
        packageManager = getPackageManager();
        devicePolicyManager = getSystemService(DevicePolicyManager.class);
        powerManager = getSystemService(PowerManager.class);


        View.OnClickListener onOffClickListener = new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.accessibility_on_off: {
                        Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        if (intentAccessibility.resolveActivity(packageManager) != null) {
                            startActivity(intentAccessibility);
                        } else {
                            Toast.makeText(context, "授权窗口打开失败，请手动打开", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case R.id.device_admin_on_off: {
                        ComponentName compMyDeviceAdmin = new ComponentName(context, MyDeviceAdminReceiver.class);
                        if (devicePolicyManager.isAdminActive(compMyDeviceAdmin)) {
                            Toast.makeText(context, "设备管理器权限已开启", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intentDeviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intentDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compMyDeviceAdmin);
                            if (intentDeviceAdmin.resolveActivity(packageManager) != null) {
                                startActivity(intentDeviceAdmin);
                            } else {
                                Toast.makeText(context, "授权窗口打开失败，请手动打开", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    case R.id.batteryIgnore_on_off: {
                        if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                            Toast.makeText(context, "忽略电池优化权限已开启", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intentBatteryIgnore = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
                            if (intentBatteryIgnore.resolveActivity(packageManager) != null) {
                                startActivity(intentBatteryIgnore);
                            } else {
                                Toast.makeText(context, "授权窗口打开失败，请手动打开", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    case R.id.notification_on_off: {
                        boolean keepAliveByNotification = !myUtils.getKeepAliveByNotification();
                        myUtils.setKeepAliveByNotification(keepAliveByNotification);
                        myUtils.requestUpdateKeepAliveByNotification(keepAliveByNotification);
                        authorizationBinding.notificationOnOffImg.setImageResource(keepAliveByNotification ? R.drawable.ic_ok : R.drawable.ic_error);
                        Toast.makeText(context, keepAliveByNotification ? "已开启" : "已关闭", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.floating_window_on_off: {
                        boolean keepAliveByFloatingWindow = !myUtils.getKeepAliveByFloatingWindow();
                        myUtils.setKeepAliveByFloatingWindow(keepAliveByFloatingWindow);
                        myUtils.requestUpdateKeepAliveByFloatingWindow(keepAliveByFloatingWindow);
                        authorizationBinding.floatingWindowOnOffImg.setImageResource(keepAliveByFloatingWindow ? R.drawable.ic_ok : R.drawable.ic_error);
                        Toast.makeText(context, keepAliveByFloatingWindow ? "已开启" : "已关闭", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };
        authorizationBinding.accessibilityOnOff.setOnClickListener(onOffClickListener);
        authorizationBinding.deviceAdminOnOff.setOnClickListener(onOffClickListener);
        authorizationBinding.batteryIgnoreOnOff.setOnClickListener(onOffClickListener);
        authorizationBinding.notificationOnOff.setOnClickListener(onOffClickListener);
        authorizationBinding.floatingWindowOnOff.setOnClickListener(onOffClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (myUtils.isServiceRunning()) {
                    authorizationBinding.accessibilityOnOffImg.setImageResource(R.drawable.ic_ok);
                } else {
                    authorizationBinding.accessibilityOnOffImg.setImageResource(R.drawable.ic_error);
                }
                if (devicePolicyManager.isAdminActive(new ComponentName(context, MyDeviceAdminReceiver.class))) {
                    authorizationBinding.deviceAdminOnOffImg.setImageResource(R.drawable.ic_ok);
                } else {
                    authorizationBinding.deviceAdminOnOffImg.setImageResource(R.drawable.ic_error);
                }
                if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                    authorizationBinding.batteryIgnoreOnOffImg.setImageResource(R.drawable.ic_ok);
                } else {
                    authorizationBinding.batteryIgnoreOnOffImg.setImageResource(R.drawable.ic_error);
                }
                if (myUtils.getKeepAliveByNotification()) {
                    authorizationBinding.notificationOnOffImg.setImageResource(R.drawable.ic_ok);
                } else {
                    authorizationBinding.notificationOnOffImg.setImageResource(R.drawable.ic_error);
                }
                if (myUtils.getKeepAliveByFloatingWindow()) {
                    authorizationBinding.floatingWindowOnOffImg.setImageResource(R.drawable.ic_ok);
                } else {
                    authorizationBinding.floatingWindowOnOffImg.setImageResource(R.drawable.ic_error);
                }
                handler.postDelayed(this, 500);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
