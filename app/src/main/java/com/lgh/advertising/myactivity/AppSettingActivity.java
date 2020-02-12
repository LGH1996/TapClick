package com.lgh.advertising.myactivity;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.LatestVersionMessage;
import com.lgh.advertising.myclass.MyAppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
        Button openDetail = findViewById(R.id.setting_open);
        Button checkUpdate = findViewById(R.id.setting_update);
        CheckBox checkBox = findViewById(R.id.setting_autoHideOnTaskList);
        checkBox.setChecked(autoHideOnTaskList);
        openDetail.setOnClickListener(new View.OnClickListener() {
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
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak") AsyncTask<String,Integer,String> asyncTask = new AsyncTask<String, Integer, String>() {
                    private LatestVersionMessage latestVersionMessage;
                    private AlertDialog waitDialog;
                    private boolean haveNewVersion;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        waitDialog = new AlertDialog.Builder(AppSettingActivity.this).setView(new ProgressBar(AppSettingActivity.this)).setCancelable(false).create();
                        Window window = waitDialog.getWindow();
                        if (window != null)
                        window.setBackgroundDrawableResource(R.color.transparent);
                        waitDialog.show();

                    }

                    @Override
                    protected String doInBackground(String... strings) {
                        try {
                            URL url = new URL(strings[0]);
                            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                            httpsURLConnection.setRequestMethod("GET");
                            httpsURLConnection.setUseCaches(false);
                            httpsURLConnection.setConnectTimeout(10000);
                            httpsURLConnection.connect();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String str;
                            while ((str = reader.readLine())!=null){
                                stringBuilder.append(str);
                            }
                            latestVersionMessage = new Gson().fromJson(stringBuilder.toString(),LatestVersionMessage.class);
                            int versionCode = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionCode;
                            String appName = latestVersionMessage.assets.get(0).name;
                            int newVersion = Integer.valueOf(appName.substring(appName.lastIndexOf('-')+1,appName.lastIndexOf('.')));
                            if (newVersion > versionCode){
                                haveNewVersion = true;
                            } else {
                                haveNewVersion = false;
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }  catch (IOException e) {
                            e.printStackTrace();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        } catch (Throwable e){
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        super.onProgressUpdate(values);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        waitDialog.dismiss();
                        if (haveNewVersion){
                            AlertDialog dialog = new AlertDialog.Builder(AppSettingActivity.this).setIcon(R.drawable.update_app).setTitle("发现新版本("+latestVersionMessage.tag_name.substring(1)+")").setMessage(latestVersionMessage.body).setNegativeButton("取消",null).setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(latestVersionMessage.assets.get(0).browser_download_url));
                                    startActivity(intent);
                                }
                            }).create();
                            dialog.show();
                        } else {
                            Toast.makeText(AppSettingActivity.this,"当前已是最新版本",Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                asyncTask.execute("https://api.github.com/repos/LGH1996/UPDATEADGO/releases/latest");
            }
        });
    }
}
