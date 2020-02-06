package com.lgh.advertising.going;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.accessibility.AccessibilityViewCommand;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.Widget;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppSelectActivity extends AppCompatActivity {
    LayoutInflater inflater;
    PackageManager packageManager;
    DataDao dataDao;
    Map<String,AppDescribe> appDescribeMap;
    List<AppDescribe> appDescribeList;
    public static AppDescribe appDescribe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);
        packageManager = getPackageManager();
        dataDao = DataDaoFactory.getInstance(getApplicationContext());
        if (MainFunction.appDescribeMap != null){
            appDescribeMap = MainFunction.appDescribeMap;
            appDescribeList = Lists.newArrayList(appDescribeMap.values());
        } else {
            appDescribeList = dataDao.getAppDescribes();
        }
        setContentView(R.layout.view_select);
        ListView listView = findViewById(R.id.listView);
        for (AppDescribe e:appDescribeList){
            try {
                e.appDrawable = packageManager.getApplicationIcon(e.appPackage);
            } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                nameNotFoundException.printStackTrace();
            }
        }
        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return appDescribeList.size();
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
                AppDescribe tem = appDescribeList.get(position);
                holder.textView.setText(tem.appName+"("+(tem.on_off?"开":"关")+")");
                holder.imageView.setImageDrawable(tem.appDrawable);
                return convertView;
            }
        };
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String packageName = appDescribeList.get(position).appPackage;
                if (appDescribeMap != null){
                    appDescribe = appDescribeMap.get(packageName);
                } else {
                    appDescribe = appDescribeList.get(position);
                    appDescribe.autoFinder = dataDao.getAutoFinder(appDescribe.appPackage);
                    appDescribe.coordinateMap = Maps.uniqueIndex(dataDao.getCoordinates(appDescribe.appPackage), new Function<Coordinate, String>() {
                        @Override
                        public String apply(Coordinate input) {
                            return input.appActivity;
                        }
                    });
                    List<Widget> widgetList = dataDao.getWidgets(appDescribe.appPackage);
                    appDescribe.widgetSetMap = new HashMap<>();
                    for (Widget w:widgetList){
                        Set<Widget> widgetSet = appDescribe.widgetSetMap.get(w.appActivity);
                        if (widgetSet == null){
                            widgetSet = new HashSet<>();
                            widgetSet.add(w);
                            appDescribe.widgetSetMap.put(w.appActivity,widgetSet);
                        } else {
                            widgetSet.add(w);
                        }
                    }
                }
                startActivity(new Intent(AppSelectActivity.this,AppConfigActivity.class));
            }
        });
        listView.setAdapter(baseAdapter);

    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(View v) {
            textView = v.findViewById(R.id.name);
            imageView = v.findViewById(R.id.img);
        }
    }
}
