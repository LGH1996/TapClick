package com.lgh.advertising.going.myactivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import androidx.core.util.Consumer;

import com.lgh.advertising.going.BuildConfig;
import com.lgh.advertising.going.R;
import com.lgh.advertising.going.databinding.ActivityMainBinding;
import com.lgh.advertising.going.databinding.ViewMainItemBinding;
import com.lgh.advertising.going.databinding.ViewPrivacyAgreementBinding;
import com.lgh.advertising.going.mybean.LatestMessage;
import com.lgh.advertising.going.mybean.MyAppConfig;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;
import com.lgh.advertising.going.myfunction.MyUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends BaseActivity {

    private Context context;
    private MyAppConfig myAppConfig;
    private DataDao dataDao;
    private LayoutInflater inflater;
    private ActivityMainBinding mainBinding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(inflater = getLayoutInflater());
        setContentView(mainBinding.getRoot());
        context = getApplicationContext();
        dataDao = MyApplication.dataDao;
        myAppConfig = MyApplication.myAppConfig;
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        if (myAppConfig == null) {
            myAppConfig = new MyAppConfig();
            MyApplication.myAppConfig = myAppConfig;
            dataDao.insertMyAppConfig(myAppConfig);
        }

        final List<Resource> source = new ArrayList<>();
        source.add(new Resource("授权管理", R.drawable.authorization));
        source.add(new Resource("创建规则", R.drawable.add_data));
        source.add(new Resource("规则管理", R.drawable.edit_data));
        source.add(new Resource("应用设置", R.drawable.setting));
        source.add(new Resource("使用说明", R.drawable.instructions));
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
                ViewMainItemBinding itemBinding = ViewMainItemBinding.inflate(inflater);
                Resource resource = source.get(position);
                itemBinding.mainImg.setImageResource(resource.drawableId);
                itemBinding.mainName.setText(resource.name);
                return itemBinding.getRoot();
            }
        };
        mainBinding.mainListView.setAdapter(baseAdapter);
        mainBinding.mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        startActivity(new Intent(context, AuthorizationActivity.class));
                        break;
                    }
                    case 1: {
                        startActivity(new Intent(context, AddDataActivity.class));
                        break;
                    }
                    case 2: {
                        MainActivity.this.startActivity(new Intent(context, ListDataActivity.class));
                        break;
                    }
                    case 3: {
                        Intent intent = new Intent(context, SettingActivity.class);
                        startActivityForResult(intent, 0x01);
                        break;
                    }
                    case 4: {
                        startActivity(new Intent(context, MoreMessageActivity.class));
                        break;
                    }
                }
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH", Locale.getDefault());
        String forUpdate = dateFormat.format(new Date());
        if (!forUpdate.equals(myAppConfig.forUpdate)) {
            myAppConfig.forUpdate = forUpdate;
            dataDao.updateMyAppConfig(myAppConfig);
            showUpdateInfo();
        }

        if (sharedPreferences.getBoolean("isFirstStart", true)) {
            showPrivacyAgreement();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAccessibilityServiceStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (myAppConfig.autoHideOnTaskList) {
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01) {
            myAppConfig = dataDao.getMyAppConfig();
        }
    }

    private void refreshAccessibilityServiceStatus() {
        MyUtils.getInstance().checkServiceState(context, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    mainBinding.statusImg.setImageResource(R.drawable.ic_ok);
                    mainBinding.statusTip.setText("无障碍服务已开启");
                } else {
                    mainBinding.statusImg.setImageResource(R.drawable.ic_error);
                    mainBinding.statusTip.setText("无障碍服务未开启");
                }
            }
        });
    }

    private void showUpdateInfo() {
        Observable<LatestMessage> observable = MyApplication.myHttpRequest.getLatestMessage();
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<LatestMessage>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull LatestMessage latestMessage) {
                try {
                    String appName = latestMessage.assets.get(0).name;
                    Matcher matcher = Pattern.compile("\\d+").matcher(appName);
                    if (matcher.find()) {
                        int newVersion = Integer.parseInt(matcher.group());
                        if (newVersion > BuildConfig.VERSION_CODE) {
                            Intent intent = new Intent(context, UpdateActivity.class);
                            intent.putExtra("updateMessage", latestMessage.body);
                            intent.putExtra("updateUrl", latestMessage.assets.get(0).browser_download_url);
                            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_ALL) != null) {
                                startActivity(intent);
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    // e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                // e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private void showPrivacyAgreement() {
        Observable<String> observable = MyApplication.myHttpRequest.getPrivacyAgreement();
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull String str) {
                ViewPrivacyAgreementBinding privacyAgreementBinding = ViewPrivacyAgreementBinding.inflate(getLayoutInflater());
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setCancelable(false).setView(privacyAgreementBinding.getRoot()).create();
                privacyAgreementBinding.content.setText(str);
                privacyAgreementBinding.sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPreferences.edit().putBoolean("isFirstStart", false).apply();
                        alertDialog.dismiss();
                    }
                });
                privacyAgreementBinding.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        finishAndRemoveTask();
                    }
                });
                Window window = alertDialog.getWindow();
                window.setBackgroundDrawableResource(R.drawable.add_data_background);
                alertDialog.show();
                WindowManager.LayoutParams lp = window.getAttributes();
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                lp.width = metrics.widthPixels / 5 * 4;
                lp.height = metrics.heightPixels / 2;
                window.setAttributes(lp);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                // e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        });
    }

    static class Resource {
        public String name;
        public int drawableId;

        public Resource(String name, int drawableId) {
            this.name = name;
            this.drawableId = drawableId;
        }
    }
}