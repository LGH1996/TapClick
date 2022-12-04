package com.lgh.advertising.going.myactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.lgh.advertising.going.databinding.ActivityAddDataBinding;
import com.lgh.advertising.going.myclass.MyApplication;
import com.lgh.advertising.going.myfunction.MyUtils;

public class AddDataActivity extends BaseActivity {
    private Context context;
    private MyUtils myUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAddDataBinding addDataBinding = ActivityAddDataBinding.inflate(getLayoutInflater());
        setContentView(addDataBinding.getRoot());
        context = getApplicationContext();
        myUtils = MyApplication.myUtils;

        addDataBinding.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUtils.isServiceRunning()) {
                    myUtils.requestShowAddDataWindow();
                } else {
                    Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intentAccessibility);
                    Toast.makeText(context, "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
