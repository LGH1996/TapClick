package com.lgh.advertising.going.myactivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.util.Consumer;

import com.lgh.advertising.going.myfunction.MyUtils;

public class ShortCutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getAction().equals("com.lgh.action.addData")) {
            MyUtils.getInstance().checkServiceState(getApplicationContext(), new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) {
                    if (aBoolean) {
                        MyUtils.getInstance().requestShowAddDataWindow(getApplicationContext());
                    } else {
                        Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        intentAccessibility.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentAccessibility);
                        Toast.makeText(getApplicationContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        finishAndRemoveTask();
    }
}
