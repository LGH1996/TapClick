package com.lgh.advertising.myactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lgh.advertising.going.MainFunction;
import com.lgh.advertising.going.MyAccessibilityService;
import com.lgh.advertising.going.MyAccessibilityServiceNoGesture;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.LatestVersionMessage;
import com.lgh.advertising.myclass.MyAppConfig;

import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends Activity {

    private MyAppConfig myAppConfig;
    private DataDao dataDao;
    private boolean startActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.main_listView);
        dataDao = DataDaoFactory.getInstance(this);
        myAppConfig = dataDao.getMyAppConfig();
        if (myAppConfig == null) myAppConfig = new MyAppConfig();
        final LayoutInflater inflater = LayoutInflater.from(this);
        final List<Resource> source = new ArrayList<>();
        source.add(new Resource("授权管理", R.drawable.authorization));
        source.add(new Resource("添加广告", R.drawable.advertising));
        source.add(new Resource("数据管理", R.drawable.edit));
        source.add(new Resource("应用设置", R.drawable.setting));

        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return source.size();
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = inflater.inflate(R.layout.view_main_item, null);
                ImageView imageView = convertView.findViewById(R.id.main_img);
                TextView textView = convertView.findViewById(R.id.main_name);
                Resource resource = source.get(position);
                imageView.setImageResource(resource.drawableId);
                textView.setText(resource.name);
                return convertView;
            }
        };
        listView.setAdapter(baseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this,AppAuthorizationActivity.class));
                        break;
                    case 1:
                        if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
                            Toast.makeText(MainActivity.this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
                        } else if (MyAccessibilityService.mainFunction != null && MyAccessibilityServiceNoGesture.mainFunction != null) {
                            Toast.makeText(MainActivity.this, "无障碍服务冲突", Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(MainActivity.this, AddAdvertisingActivity.class));
                            if (MyAccessibilityService.mainFunction != null) {
                                MyAccessibilityService.mainFunction.showAddAdvertisingFloat();
                            }
                            if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                                MyAccessibilityServiceNoGesture.mainFunction.showAddAdvertisingFloat();
                            }
                        }
                        break;
                    case 2:
                        MainActivity.this.startActivity(new Intent(MainActivity.this, AppSelectActivity.class));
                        break;
                    case 3:
                        Intent intent = new Intent(MainActivity.this,AppSettingActivity.class);
                        intent.putExtra("myAppConfig.autoHideOnTaskList",myAppConfig.autoHideOnTaskList);
                        startActivityForResult(intent,0);
                        break;
                }
                startActivity = true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView statusImg = findViewById(R.id.status_img);
        TextView statusTip = findViewById(R.id.status_tip);
        if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
            statusImg.setImageResource(R.drawable.error);
            statusTip.setText("无障碍服务未开启");
        } else if (MyAccessibilityService.mainFunction != null && MyAccessibilityServiceNoGesture.mainFunction != null) {
            statusImg.setImageResource(R.drawable.error);
            statusTip.setText("无障碍服务冲突");
        } else {
            statusImg.setImageResource(R.drawable.ok);
            statusTip.setText("无障碍服务已开启\n请确保允许该应用后台运行\n并在任务列表中下拉锁定该页面");
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if (myAppConfig.autoHideOnTaskList) {
                finishAndRemoveTask();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!startActivity && myAppConfig.autoHideOnTaskList){
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startActivity = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==0){
            myAppConfig.autoHideOnTaskList = data.getBooleanExtra("myAppConfig.autoHideOnTaskList",myAppConfig.autoHideOnTaskList);
            dataDao.insertMyAppConfig(myAppConfig);
        }
    }

    class Resource {
        public String name;
        public int drawableId;

        public Resource(String name, int drawableId) {
            this.name = name;
            this.drawableId = drawableId;
        }
    }
}