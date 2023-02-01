package com.lgh.advertising.going.myfunction;

import android.content.Intent;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.lgh.advertising.going.myclass.MyApplication;

public class MyTileServer extends TileService {
    MyUtils myUtils = MyApplication.myUtils;

    @Override
    public void onClick() {
        if (myUtils.isServiceRunning()) {
            myUtils.requestShowAddDataWindow();
        } else {
            Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intentAccessibility.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityAndCollapse(intentAccessibility);
            Toast.makeText(getApplicationContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
        }
    }
}
