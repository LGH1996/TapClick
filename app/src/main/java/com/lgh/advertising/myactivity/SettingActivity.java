package com.lgh.advertising.myactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.LatestMessage;
import com.lgh.advertising.myclass.MyAppConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingActivity extends Activity {

    private Context context;
    private DataDao dataDao;
    private MyAppConfig myAppConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context = getApplicationContext();
        dataDao = DataDaoFactory.getInstance(context);
        myAppConfig = dataDao.getMyAppConfig();

        Button openDetail = findViewById(R.id.setting_open);
        Button checkUpdate = findViewById(R.id.setting_update);
        Button givePraise = findViewById(R.id.setting_praise);
        Button moreMessage = findViewById(R.id.setting_more);
        TextView authorChat = findViewById(R.id.authorChat);
        CheckBox checkBox = findViewById(R.id.setting_autoHideOnTaskList);
        checkBox.setChecked(myAppConfig.autoHideOnTaskList);

        openDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });

        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                @SuppressLint("StaticFieldLeak") AsyncTask<String, Integer, String> asyncTask = new AsyncTask<String, Integer, String>() {
                    private LatestMessage latestVersionMessage;
                    private AlertDialog waitDialog;
                    private boolean haveNewVersion;
                    private boolean occurError;


                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        waitDialog = new AlertDialog.Builder(SettingActivity.this).setView(new ProgressBar(context)).setCancelable(false).create();
                        Window window = waitDialog.getWindow();
                        if (window != null) {
                            window.setBackgroundDrawableResource(R.color.transparent);
                        }
                        waitDialog.show();

                    }

                    @Override
                    protected String doInBackground(String... strings) {
                        try {
                            OkHttpClient httpClient = new OkHttpClient();
                            Request request = new Request.Builder().get().url(strings[0]).build();
                            Response response = httpClient.newCall(request).execute();
                            latestVersionMessage = new Gson().fromJson(response.body().string(), LatestMessage.class);
                            response.close();
                            int versionCode = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionCode;
                            String appName = latestVersionMessage.assets.get(0).name;
                            Matcher matcher = Pattern.compile("\\d+").matcher(appName);
                            if (matcher.find()) {
                                int newVersion = Integer.valueOf(matcher.group());
                                haveNewVersion = newVersion > versionCode;
                            }
                        } catch (Throwable e) {
                            occurError = true;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        waitDialog.dismiss();
                        if (occurError) {
                            Toast.makeText(context, "查询新版本时出现错误", Toast.LENGTH_SHORT).show();
                        } else if (haveNewVersion) {
                            Intent intent = new Intent(context, UpdateActivity.class);
                            intent.putExtra("updateMessage", latestVersionMessage.body);
                            intent.putExtra("updateUrl", latestVersionMessage.assets.get(0).browser_download_url);
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                asyncTask.execute("https://api.github.com/repos/LGH1996/ADGORELEASE/releases/latest");
            }
        });

        givePraise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "请选择酷安应用市场或华为应用市场"));
                } else {
                    Toast.makeText(context, "请到酷安应用市场或华为应用市场评分", Toast.LENGTH_SHORT).show();
                }
            }
        });

        moreMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, MoreMessageActivity.class));
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myAppConfig.autoHideOnTaskList = isChecked;
                dataDao.updateMyAppConfig(myAppConfig);
            }
        });

        authorChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openChat = new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=2281442260"));
                if (openChat.resolveActivity(getPackageManager()) != null) {
                    startActivity(openChat);
                }
            }
        });
    }
}
