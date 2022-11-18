package com.lgh.advertising.going.myactivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Toast;

import com.lgh.advertising.going.databinding.ActivityListDataBinding;
import com.lgh.advertising.going.databinding.ViewListItemBinding;
import com.lgh.advertising.going.databinding.ViewSearchBinding;
import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;
import com.lgh.advertising.going.myfunction.MyAccessibilityService;
import com.lgh.advertising.going.myfunction.MyAccessibilityServiceNoGesture;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ListDataActivity extends BaseActivity {

    private Context context;
    private DataDao dataDao;
    private PackageManager packageManager;
    private LayoutInflater inflater;
    private Map<String, AppDescribe> appDescribeMap;
    private List<AppDescribe> appDescribeList;
    private List<AppDescribeAndIcon> appDescribeAndIconList;
    private List<AppDescribeAndIcon> appDescribeAndIconFilterList;
    private BaseAdapter baseAdapter;
    private ActivityListDataBinding listDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listDataBinding = ActivityListDataBinding.inflate(inflater = getLayoutInflater());
        setContentView(listDataBinding.getRoot());
        context = getApplicationContext();
        dataDao = MyApplication.dataDao;
        packageManager = getPackageManager();
        appDescribeAndIconList = new ArrayList<>();
        appDescribeAndIconFilterList = new ArrayList<>();

        if (MyAccessibilityService.mainFunction == null && MyAccessibilityServiceNoGesture.mainFunction == null) {
            Toast.makeText(context, "无障碍服务未开启", Toast.LENGTH_SHORT).show();
        } else {
            if (MyAccessibilityService.mainFunction != null) {
                appDescribeMap = MyAccessibilityService.mainFunction.getAppDescribeMap();
            }
            if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                appDescribeMap = MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap();
            }
        }

        ViewSearchBinding searchBinding = ViewSearchBinding.inflate(inflater);
        List<String> searchKeyword = new ArrayList<>();
        searchKeyword.add("@开启");
        searchKeyword.add("@关闭");
        searchKeyword.add("@已创建规则");
        searchKeyword.add("@未创建规则");
        searchKeyword.add("@系统应用");
        searchKeyword.add("@非系统应用");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, searchKeyword);
        searchBinding.searchBox.setAdapter(adapter);
        final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                appDescribeAndIconFilterList.clear();
                if (constraint.equals("@开启")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (e.appDescribe.onOff) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }
                if (constraint.equals("@关闭")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (!e.appDescribe.onOff) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }
                if (constraint.equals("@已创建规则")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (!e.appDescribe.coordinateMap.isEmpty() || !e.appDescribe.widgetSetMap.isEmpty()) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }
                if (constraint.equals("@未创建规则")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (e.appDescribe.coordinateMap.isEmpty() && e.appDescribe.widgetSetMap.isEmpty()) {
                            appDescribeAndIconFilterList.add(e);
                        }
                    }
                    return null;
                }
                if (constraint.equals("@系统应用")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        try {
                            if ((packageManager.getApplicationInfo(e.appDescribe.appPackage, PackageManager.GET_META_DATA).flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                                appDescribeAndIconFilterList.add(e);
                            }
                        } catch (PackageManager.NameNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return null;
                }
                if (constraint.equals("@非系统应用")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        try {
                            if ((packageManager.getApplicationInfo(e.appDescribe.appPackage, PackageManager.GET_META_DATA).flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                                appDescribeAndIconFilterList.add(e);
                            }
                        } catch (PackageManager.NameNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return null;
                }

                for (AppDescribeAndIcon e : appDescribeAndIconList) {
                    String str = constraint.toString().toLowerCase();
                    if (e.appDescribe.appName.toLowerCase().contains(str) || e.appDescribe.appPackage.contains(str)) {
                        appDescribeAndIconFilterList.add(e);
                    }
                }
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                baseAdapter.notifyDataSetChanged();
            }
        };
        searchBinding.searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter.filter(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        listDataBinding.listView.addHeaderView(searchBinding.getRoot());

        baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return appDescribeAndIconFilterList.size();
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
                ViewListItemBinding listItemBinding;
                if (convertView == null) {
                    listItemBinding = ViewListItemBinding.inflate(inflater);
                    convertView = listItemBinding.getRoot();
                    convertView.setTag(listItemBinding);
                } else {
                    listItemBinding = (ViewListItemBinding) convertView.getTag();
                }
                final AppDescribeAndIcon tem = appDescribeAndIconFilterList.get(position);
                listItemBinding.name.setText(tem.appDescribe.appName);
                listItemBinding.pkg.setText(tem.appDescribe.appPackage);
                listItemBinding.onOff.setText(tem.appDescribe.onOff ? "开启" : "关闭");
                listItemBinding.img.setImageDrawable(tem.icon);
                listItemBinding.scOnOff.setChecked(tem.appDescribe.onOff);
                listItemBinding.scOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        tem.appDescribe.onOff = isChecked;
                        tem.appDescribe.autoFinderOnOFF = isChecked;
                        tem.appDescribe.widgetOnOff = isChecked;
                        tem.appDescribe.coordinateOnOff = isChecked;
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyApplication.appDescribe = tem.appDescribe;
                        startActivity(new Intent(context, EditDataActivity.class));
                    }
                });
                return convertView;
            }
        };
        listDataBinding.listView.setAdapter(baseAdapter);

        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Throwable {
                if (appDescribeMap != null) {
                    appDescribeList = new ArrayList<>(appDescribeMap.values());
                } else {
                    appDescribeList = dataDao.getAllAppDescribes();
                    for (AppDescribe e : appDescribeList) {
                        e.getOtherFieldsFromDatabase(dataDao);
                    }
                }
                appDescribeList.sort(new Comparator<AppDescribe>() {
                    @Override
                    public int compare(AppDescribe o1, AppDescribe o2) {
                        return Collator.getInstance(Locale.CHINESE).compare(o1.appName, o2.appName);
                    }
                });
                ListIterator<AppDescribe> iterator = appDescribeList.listIterator();
                while (iterator.hasNext()) {
                    try {
                        AppDescribe e = iterator.next();
                        Drawable icon = packageManager.getApplicationIcon(e.appPackage);
                        appDescribeAndIconList.add(new AppDescribeAndIcon(e, icon));
                    } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                        iterator.remove();
//                        nameNotFoundException.printStackTrace();
                    }
                }
                appDescribeAndIconFilterList.addAll(appDescribeAndIconList);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                listDataBinding.listView.setVisibility(View.GONE);
                listDataBinding.progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Toast.makeText(context, "出现错误", Toast.LENGTH_SHORT).show();
                listDataBinding.progress.setVisibility(View.GONE);
                listDataBinding.listView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete() {
                baseAdapter.notifyDataSetChanged();
                listDataBinding.progress.setVisibility(View.GONE);
                listDataBinding.listView.setVisibility(View.VISIBLE);
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

    static class AppDescribeAndIcon {
        AppDescribe appDescribe;
        Drawable icon;

        public AppDescribeAndIcon(AppDescribe appDescribe, Drawable icon) {
            this.appDescribe = appDescribe;
            this.icon = icon;
        }
    }
}