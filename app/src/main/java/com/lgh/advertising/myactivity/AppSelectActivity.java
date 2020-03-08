package com.lgh.advertising.myactivity;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lgh.advertising.going.MyAccessibilityService;
import com.lgh.advertising.going.MyAccessibilityServiceNoGesture;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppSelectActivity extends Activity {

    public static AppDescribe appDescribe;
    Context context;
    PackageManager packageManager;
    DataDao dataDao;
    Map<String, AppDescribe> appDescribeMap;
    List<AppDescribe> appDescribeList;
    List<AppDescribeAndIcon> appDescribeAndIconList;
    BaseAdapter baseAdapter;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_select);
        context = getApplicationContext();
        packageManager = getPackageManager();
        dataDao = DataDaoFactory.getInstance(getApplicationContext());
        appDescribeAndIconList = new ArrayList<>();
        final ListView listView = findViewById(R.id.listView);
        final ProgressBar progressBar = findViewById(R.id.progress);
        final LayoutInflater inflater = LayoutInflater.from(context);
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0x00:
                        baseAdapter = new BaseAdapter() {
                            @Override
                            public int getCount() {
                                return appDescribeAndIconList.size();
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
                                AppSelectActivity.ViewHolder holder;
                                if (convertView == null) {
                                    convertView = inflater.inflate(R.layout.view_select_item, null);
                                    holder = new AppSelectActivity.ViewHolder(convertView);
                                    convertView.setTag(holder);
                                } else {
                                    holder = (AppSelectActivity.ViewHolder) convertView.getTag();
                                }
                                AppDescribeAndIcon tem = appDescribeAndIconList.get(position);
                                holder.textView.setText(tem.appDescribe.appName + " (" + (tem.appDescribe.on_off ? "开启" : "关闭") + ")");
                                holder.imageView.setImageDrawable(tem.icon);
                                return convertView;
                            }
                        };
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                appDescribe = null;
                                String packageName = appDescribeList.get(position).appPackage;
                                if (appDescribeMap != null) {
                                    appDescribe = appDescribeMap.get(packageName);
                                }
                                if (appDescribe == null) {
                                    appDescribe = appDescribeList.get(position);
                                    appDescribe.getOtherFieldsFromDatabase(dataDao);
                                }
                                startActivity(new Intent(context, AppConfigActivity.class));
                            }
                        });
                        listView.setAdapter(baseAdapter);
                        listView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        break;
                    case 0x01:
                        Toast.makeText(context, "无障碍服务未开启", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x02:
                        Toast.makeText(context, "无障碍服务冲突", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
                    handler.sendEmptyMessage(0x01);
                } else if (MyAccessibilityService.mainFunction != null && MyAccessibilityServiceNoGesture.mainFunction != null) {
                    handler.sendEmptyMessage(0x02);
                } else {
                    if (MyAccessibilityService.mainFunction != null) {
                        appDescribeMap = MyAccessibilityService.mainFunction.getAppDescribeMap();
                    }
                    if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                        appDescribeMap = MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap();
                    }
                }
                if (appDescribeMap != null) {
                    appDescribeList = new ArrayList<>(appDescribeMap.values());
                } else {
                    appDescribeList = dataDao.getAppDescribes();
                }
                Collections.sort(appDescribeList, new Comparator<AppDescribe>() {
                    @Override
                    public int compare(AppDescribe o1, AppDescribe o2) {
                        return Collator.getInstance(Locale.CHINESE).compare(o1.appName, o2.appName);
                    }
                });
                for (AppDescribe e : appDescribeList) {
                    try {
                        Drawable icon = packageManager.getApplicationIcon(e.appPackage);
                        appDescribeAndIconList.add(new AppDescribeAndIcon(e, icon));
                    } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                        appDescribeList.remove(e);
                        nameNotFoundException.printStackTrace();
                    }
                }
                handler.sendEmptyMessage(0x00);
            }
        }).start();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        baseAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyAccessibilityService.mainFunction != null) {
            for (AppDescribe e : MyAccessibilityService.mainFunction.getAppDescribeMap().values()) {
                e.getOtherFieldsFromDatabase(dataDao);
            }
        }
        if (MyAccessibilityServiceNoGesture.mainFunction != null) {
            for (AppDescribe e : MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap().values()) {
                e.getOtherFieldsFromDatabase(dataDao);
            }
        }
    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(View v) {
            textView = v.findViewById(R.id.name);
            imageView = v.findViewById(R.id.img);
        }
    }

    class AppDescribeAndIcon {
        AppDescribe appDescribe;
        Drawable icon;

        public AppDescribeAndIcon(AppDescribe appDescribe, Drawable icon) {
            this.appDescribe = appDescribe;
            this.icon = icon;
        }
    }
}
