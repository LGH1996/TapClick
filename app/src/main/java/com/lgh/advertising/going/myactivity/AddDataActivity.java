package com.lgh.advertising.going.myactivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.lgh.advertising.going.databinding.ActivityAddDataBinding;
import com.lgh.advertising.going.myfunction.MyAccessibilityService;
import com.lgh.advertising.going.myfunction.MyAccessibilityServiceNoGesture;

public class AddDataActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAddDataBinding addDataBinding = ActivityAddDataBinding.inflate(getLayoutInflater());
        setContentView(addDataBinding.getRoot());

        addDataBinding.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
                    Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intentAccessibility);
                    Toast.makeText(getApplicationContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
                } else {
                    if (MyAccessibilityService.mainFunction != null) {
                        MyAccessibilityService.mainFunction.showAddDataFloat();
                    }
                    if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                        MyAccessibilityServiceNoGesture.mainFunction.showAddDataFloat();
                    }
                }
            }
        });
    }
}
