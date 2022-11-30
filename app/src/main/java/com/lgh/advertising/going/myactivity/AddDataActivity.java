package com.lgh.advertising.going.myactivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.core.util.Consumer;

import com.lgh.advertising.going.databinding.ActivityAddDataBinding;
import com.lgh.advertising.going.myfunction.MyUtils;

public class AddDataActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAddDataBinding addDataBinding = ActivityAddDataBinding.inflate(getLayoutInflater());
        setContentView(addDataBinding.getRoot());

        addDataBinding.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUtils.getInstance().checkServiceState(getApplicationContext(), new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        if (aBoolean) {
                            MyUtils.getInstance().requestShowAddDataWindow(getApplicationContext());
                        } else {
                            Intent intentAccessibility = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            startActivity(intentAccessibility);
                            Toast.makeText(getApplicationContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
