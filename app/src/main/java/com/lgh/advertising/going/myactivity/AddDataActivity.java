package com.lgh.advertising.going.myactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
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
        addDataBinding.dbClick.setChecked(myUtils.getDbClickEnable());
        addDataBinding.dbClickSetting.setVisibility(myUtils.getDbClickEnable() ? View.VISIBLE : View.GONE);

        addDataBinding.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!myUtils.isServiceRunning()) {
                    requestEnableAccessibility();
                }
                myUtils.requestShowAddDataWindow();
            }
        });

        addDataBinding.dbClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myUtils.setDbClickEnable(isChecked);
                myUtils.requestUpdateShowDbClickFloating(isChecked);
                addDataBinding.dbClickSetting.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        addDataBinding.dbClickSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!myUtils.isServiceRunning()) {
                    requestEnableAccessibility();
                    return;
                }
                myUtils.requestShowDbClickSetting();
            }
        });
    }

    private void requestEnableAccessibility() {
        Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intentAccessibility);
        Toast.makeText(context, "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
    }
}
