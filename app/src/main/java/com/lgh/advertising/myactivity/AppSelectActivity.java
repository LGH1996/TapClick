package com.lgh.advertising.myactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.common.collect.Lists;
import com.lgh.advertising.going.MainFunction;
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

public class AppSelectActivity extends AppCompatActivity {

    public static AppDescribe appDescribe;
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
        packageManager = getPackageManager();
        dataDao = DataDaoFactory.getInstance(getApplicationContext());
        appDescribeAndIconList = new ArrayList<>();
        final ListView listView = findViewById(R.id.listView);
        final ProgressBar progressBar = findViewById(R.id.progress);
        final LayoutInflater inflater = LayoutInflater.from(this);
        listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MainFunction.appDescribeMap != null) {
                    appDescribeMap = MainFunction.appDescribeMap;
                    appDescribeList = Lists.newArrayList(appDescribeMap.values());
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
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.what == 0x00) {

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
                                convertView = inflater.inflate(R.layout.view_pac, null);
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

                            String packageName = appDescribeList.get(position).appPackage;
                            appDescribe = null;
                            if (appDescribeMap != null) {
                                appDescribe = appDescribeMap.get(packageName);
                            }
                            if (appDescribe == null) {
                                appDescribe = appDescribeList.get(position);
                                appDescribe.getOtherField(dataDao);
                            }
                            startActivity(new Intent(AppSelectActivity.this, AppConfigActivity.class));
                        }
                    });
                    listView.setAdapter(baseAdapter);
                    listView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                return true;
            }
        });

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        baseAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MainFunction.appDescribeMap != null) {
            for (AppDescribe e : MainFunction.appDescribeMap.values()) {
                e.getOtherField(dataDao);
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
