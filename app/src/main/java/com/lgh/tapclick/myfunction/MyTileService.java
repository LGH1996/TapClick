package com.lgh.tapclick.myfunction;

import android.content.Intent;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class MyTileService extends TileService {

    @Override
    public void onClick() {
        if (MyUtils.isServiceRunning()) {
            MyUtils.requestShowAddDataWindow();
        } else {
            Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intentAccessibility.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentAccessibility);
            Toast.makeText(getApplicationContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
        }
    }
}
