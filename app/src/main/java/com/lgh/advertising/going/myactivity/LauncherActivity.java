package com.lgh.advertising.going.myactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.lgh.advertising.going.R;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String xrxsApp = "com.client.xrxs.com.xrxsapp";
        Intent intent = getPackageManager().getLaunchIntentForPackage(xrxsApp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAndRemoveTask();
    }
}