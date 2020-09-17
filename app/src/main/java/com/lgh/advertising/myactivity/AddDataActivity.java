package com.lgh.advertising.myactivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.lgh.advertising.going.MyAccessibilityService;
import com.lgh.advertising.going.MyAccessibilityServiceNoGesture;
import com.lgh.advertising.going.databinding.ActivityAddDataBinding;

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
                    Toast.makeText(getApplicationContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
                } else if (MyAccessibilityService.mainFunction != null && MyAccessibilityServiceNoGesture.mainFunction != null) {
                    Toast.makeText(getApplicationContext(), "无障碍服务冲突", Toast.LENGTH_SHORT).show();
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
