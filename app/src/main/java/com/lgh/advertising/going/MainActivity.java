package com.lgh.advertising.going;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
            Intent intent_abs = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent_abs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent_abs);
//            Toast.makeText(this, "请打开其中一个无障碍服务", Toast.LENGTH_SHORT).show();
        } else if (MyAccessibilityService.mainFunction != null && MyAccessibilityServiceNoGesture.mainFunction != null) {
            Intent intent_abs = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent_abs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent_abs);
//            Toast.makeText(this, "无障碍服务冲突，请关闭其中一个", Toast.LENGTH_SHORT).show();
        } else {

            if (MyAccessibilityService.mainFunction != null) {
//                MyAccessibilityService.mainFunction.handler.sendEmptyMessage(0x00);
            }

            if (MyAccessibilityServiceNoGesture.mainFunction != null) {
//                MyAccessibilityServiceNoGesture.mainFunction.handler.sendEmptyMessage(0x00);
            }
        }
        setContentView(R.layout.activity_main);
        LinearLayout authorization = findViewById(R.id.main_authorization);
        LinearLayout advertising = findViewById(R.id.main_advertising);
        LinearLayout dataEdit = findViewById(R.id.main_edit);
        LinearLayout setting = findViewById(R.id.main_setting);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.main_authorization:

                        break;
                    case R.id.main_advertising:
                        MainFunction.handler.sendEmptyMessage(0x00);
                        break;
                    case R.id.main_edit:
                        MainActivity.this.startActivity(new Intent(MainActivity.this,AppSelectActivity.class));
                        break;
                    case R.id.main_setting:
                        break;
                }
            }
        };
        authorization.setOnClickListener(clickListener);
        advertising.setOnClickListener(clickListener);
        dataEdit.setOnClickListener(clickListener);
        setting.setOnClickListener(clickListener);
    }

}
