package com.lgh.advertising.myactivity;

import androidx.annotation.NonNull;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.MyAppConfig;

public class AppSettingActivity extends Activity {

    private boolean autoHideOnTaskList;
    private  Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);
        autoHideOnTaskList = getIntent().getBooleanExtra("myAppConfig.autoHideOnTaskList",false);
        intent = new Intent();
        intent.putExtra("myAppConfig.autoHideOnTaskList",autoHideOnTaskList);
        setResult(Activity.RESULT_OK,intent);
        Button button = findViewById(R.id.setting_open);
        CheckBox checkBox = findViewById(R.id.setting_autoHideOnTaskList);
        checkBox.setChecked(autoHideOnTaskList);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoHideOnTaskList = isChecked;
                intent.putExtra("myAppConfig.autoHideOnTaskList",autoHideOnTaskList);
            }
        });
    }
}
