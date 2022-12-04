package com.lgh.advertising.going.myactivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Switch;
import android.widget.Toast;

import com.lgh.advertising.going.databinding.ActivityListDataBinding;
import com.lgh.advertising.going.databinding.ViewListItemBinding;
import com.lgh.advertising.going.databinding.ViewOnOffWarningBinding;
import com.lgh.advertising.going.databinding.ViewSearchBinding;
import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;
import com.lgh.advertising.going.myfunction.MyUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
    private List<AppDescribe> appDescribeList;
    private List<AppDescribeAndIcon> appDescribeAndIconList;
    private List<AppDescribeAndIcon> appDescribeAndIconFilterList;
    private BaseAdapter baseAdapter;
    private ActivityListDataBinding listDataBinding;
    private Set<String> pkgSuggestNotOnList;
    private MyUtils myUtils;

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
        pkgSuggestNotOnList = new HashSet<>();
        myUtils = MyApplication.myUtils;

        if (!myUtils.isServiceRunning()) {
            Toast.makeText(context, "无障碍服务未开启", Toast.LENGTH_SHORT).show();
        }

        Set<String> pkgSysSet = packageManager.
                getInstalledPackages(PackageManager.MATCH_SYSTEM_ONLY)
                .stream().map(e -> e.packageName)
                .collect(Collectors.toSet());
        Set<String> pkgInputMethodSet = getSystemService(InputMethodManager.class)
                .getInputMethodList()
                .stream()
                .map(InputMethodInfo::getPackageName)
                .collect(Collectors.toSet());
        Set<String> pkgHasHomeSet = packageManager
                .queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_ALL)
                .stream()
                .map(e -> e.activityInfo.packageName)
                .collect(Collectors.toSet());
        pkgSuggestNotOnList.addAll(pkgSysSet);
        pkgSuggestNotOnList.addAll(pkgInputMethodSet);
        pkgSuggestNotOnList.addAll(pkgHasHomeSet);

        ViewSearchBinding searchBinding = ViewSearchBinding.inflate(inflater);
        List<String> searchKeyword = new ArrayList<>();
        searchKeyword.add("@开启");
        searchKeyword.add("@关闭");
        searchKeyword.add("@已创建规则");
        searchKeyword.add("@未创建规则");
        searchKeyword.add("@系统应用");
        searchKeyword.add("@非系统应用");
        searchKeyword.add("@非必要不开启应用");
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
                            // ex.printStackTrace();
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
                            // ex.printStackTrace();
                        }
                    }
                    return null;
                }
                if (constraint.equals("@非必要不开启应用")) {
                    for (AppDescribeAndIcon e : appDescribeAndIconList) {
                        if (pkgSuggestNotOnList.contains(e.appDescribe.appPackage)) {
                            appDescribeAndIconFilterList.add(e);
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
                if (convertView == null) {
                    ViewListItemBinding listItemBinding = ViewListItemBinding.inflate(inflater);
                    convertView = listItemBinding.getRoot();
                    convertView.setTag(listItemBinding);
                }
                ViewListItemBinding listItemBinding = (ViewListItemBinding) convertView.getTag();
                final AppDescribeAndIcon tem = appDescribeAndIconFilterList.get(position);
                listItemBinding.name.setText(tem.appDescribe.appName);
                listItemBinding.pkg.setText(tem.appDescribe.appPackage);
                listItemBinding.img.setImageDrawable(tem.icon);
                listItemBinding.onOff.setChecked(tem.appDescribe.onOff);
                listItemBinding.onOff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isChecked = ((Switch) v).isChecked();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                tem.appDescribe.onOff = isChecked;
                                tem.appDescribe.autoFinderOnOFF = isChecked;
                                tem.appDescribe.widgetOnOff = isChecked;
                                tem.appDescribe.coordinateOnOff = isChecked;
                                dataDao.updateAppDescribe(tem.appDescribe);
                                myUtils.requestUpdateAppDescribe(tem.appDescribe.appPackage);
                            }
                        };
                        if (isChecked && pkgSuggestNotOnList.contains(tem.appDescribe.appPackage)) {
                            listItemBinding.onOff.setChecked(false);
                            View view = ViewOnOffWarningBinding.inflate(getLayoutInflater()).getRoot();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListDataActivity.this);
                            alertDialogBuilder.setView(view);
                            alertDialogBuilder.setNegativeButton("取消", null);
                            alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    runnable.run();
                                    listItemBinding.onOff.setChecked(true);
                                }
                            });
                            AlertDialog dialog = alertDialogBuilder.create();
                            dialog.show();
                        } else {
                            runnable.run();
                        }
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditDataActivity.appDescribe = tem.appDescribe;
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
                appDescribeList = dataDao.getAllAppDescribes();
                for (AppDescribe e : appDescribeList) {
                    e.getOtherFieldsFromDatabase(dataDao);
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
                    } catch (PackageManager.NameNotFoundException e) {
                        iterator.remove();
                        // e.printStackTrace();
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

    static class AppDescribeAndIcon {
        AppDescribe appDescribe;
        Drawable icon;

        public AppDescribeAndIcon(AppDescribe appDescribe, Drawable icon) {
            this.appDescribe = appDescribe;
            this.icon = icon;
        }
    }
}