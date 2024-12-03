package com.lgh.tapclick.myactivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.lgh.tapclick.databinding.ActivityAddDataBinding;
import com.lgh.tapclick.myfunction.MyUtils;

public class AddDataActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAddDataBinding addDataBinding = ActivityAddDataBinding.inflate(getLayoutInflater());
        setContentView(addDataBinding.getRoot());
        addDataBinding.dbClick.setChecked(MyUtils.getDbClickEnable());
        addDataBinding.dbClickSetting.setVisibility(MyUtils.getDbClickEnable() ? View.VISIBLE : View.GONE);

        addDataBinding.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MyUtils.isServiceRunning()) {
                    requestEnableAccessibility();
                }
                MyUtils.requestShowAddDataWindow();
            }
        });

        addDataBinding.dbClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyUtils.setDbClickEnable(isChecked);
                MyUtils.requestUpdateShowDbClickFloating(isChecked);
                addDataBinding.dbClickSetting.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        addDataBinding.dbClickSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MyUtils.isServiceRunning()) {
                    requestEnableAccessibility();
                    return;
                }
                MyUtils.requestShowDbClickSetting();
            }
        });
    }

    private void requestEnableAccessibility() {
        Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intentAccessibility);
        Toast.makeText(getApplicationContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
    }
}
