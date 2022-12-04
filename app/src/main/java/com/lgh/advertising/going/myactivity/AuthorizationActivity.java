package com.lgh.advertising.going.myactivity;

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
import com.lgh.advertising.going.myfunction.MyUtils;

public class AuthorizationActivity extends BaseActivity {

    private ActivityAuthorizationBinding authorizationBinding;
    private Context context;
    private MyUtils myUtils;
    private Handler handler;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorizationBinding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
        setContentView(authorizationBinding.getRoot());
        context = getApplicationContext();
        myUtils = MyApplication.myUtils;
        handler = new Handler();
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
                        if (getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(getPackageName())) {
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
                }
            }
        };
        authorizationBinding.accessibilityOnOff.setOnClickListener(onOffClickListener);
        authorizationBinding.batteryIgnoreOnOff.setOnClickListener(onOffClickListener);
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
                if (getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(getPackageName())) {
                    authorizationBinding.batteryIgnoreOnOffImg.setImageResource(R.drawable.ic_ok);
                } else {
                    authorizationBinding.batteryIgnoreOnOffImg.setImageResource(R.drawable.ic_error);
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
