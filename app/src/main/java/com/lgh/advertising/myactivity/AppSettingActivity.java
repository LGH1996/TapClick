package com.lgh.advertising.myactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.LatestMessage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppSettingActivity extends Activity {

    private Context context;
    private boolean autoHideOnTaskList;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);
        context = getApplicationContext();
        autoHideOnTaskList = getIntent().getBooleanExtra("myAppConfig.autoHideOnTaskList", false);
        intent = new Intent();
        intent.putExtra("myAppConfig.autoHideOnTaskList", autoHideOnTaskList);
        setResult(Activity.RESULT_OK, intent);
        Button openDetail = findViewById(R.id.setting_open);
        Button checkUpdate = findViewById(R.id.setting_update);
        Button submitDebug = findViewById(R.id.setting_submit);
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
                intent.putExtra("myAppConfig.autoHideOnTaskList", autoHideOnTaskList);
            }
        });
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                @SuppressLint("StaticFieldLeak") AsyncTask<String, Integer, String> asyncTask = new AsyncTask<String, Integer, String>() {
                    private LatestMessage latestVersionMessage;
                    private AlertDialog waitDialog;
                    private boolean haveNewVersion;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        waitDialog = new AlertDialog.Builder(AppSettingActivity.this).setView(new ProgressBar(context)).setCancelable(false).create();
                        Window window = waitDialog.getWindow();
                        if (window != null)
                            window.setBackgroundDrawableResource(R.color.transparent);
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
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        waitDialog.dismiss();
                        if (haveNewVersion) {
                            View view = LayoutInflater.from(context).inflate(R.layout.view_update_message, null);
                            TextView textView = view.findViewById(R.id.update_massage);
                            textView.setText(Html.fromHtml(latestVersionMessage.body));
                            AlertDialog dialog = new AlertDialog.Builder(AppSettingActivity.this).setView(view).setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(latestVersionMessage.assets.get(0).browser_download_url));
                                    startActivity(intent);
                                }
                            }).create();
                            dialog.show();
                        } else {
                            Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                asyncTask.execute("https://api.github.com/repos/LGH1996/UPDATEADGO/releases/latest");
            }
        });
        submitDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final LayoutInflater inflater = LayoutInflater.from(context);
                    final View view = inflater.inflate(R.layout.view_commit, null);
                    final AlertDialog dialogCommit = new AlertDialog.Builder(AppSettingActivity.this).setView(view).setCancelable(false).create();
                    final EditText textView = view.findViewById(R.id.editText);
                    TextView but_empty = view.findViewById(R.id.empty);
                    TextView but_cancel = view.findViewById(R.id.cancel);
                    TextView but_sure = view.findViewById(R.id.sure);
                    but_empty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            textView.setText("");
                        }
                    });
                    but_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogCommit.dismiss();
                        }
                    });
                    but_sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                final String commitMessage = textView.getText().toString().trim();
                                if (commitMessage.isEmpty()) {
                                    Toast.makeText(context, "内容不能为空", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                @SuppressLint("StaticFieldLeak") AsyncTask<String, Integer, String> asyncTask = new AsyncTask<String, Integer, String>() {
                                    private boolean successful;
                                    private AlertDialog waitDialog;

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        successful = true;
                                        waitDialog = new AlertDialog.Builder(AppSettingActivity.this).setView(new ProgressBar(context)).setCancelable(false).create();
                                        Window window = waitDialog.getWindow();
                                        if (window != null)
                                            window.setBackgroundDrawableResource(R.color.transparent);
                                        waitDialog.show();

                                    }

                                    @Override
                                    protected String doInBackground(String... strings) {
                                        try {
                                            HashMap<String, String> postMessage = new HashMap<>();
                                            postMessage.put("message", new SimpleDateFormat("yyyy:MM:dd HH:mm:ss a", Locale.ENGLISH).format(new Date()));
                                            postMessage.put("content", Base64.encodeToString(commitMessage.getBytes(), Base64.DEFAULT));
                                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(postMessage));
                                            Headers headers = new Headers.Builder().add("Authorization", "token ccb492b65726eafa5a66eb53fac607ccaa021c62").build();
                                            Request request = new Request.Builder().put(requestBody).headers(headers).url(strings[0]).build();
                                            OkHttpClient httpClient = new OkHttpClient();
                                            Response response = httpClient.newCall(request).execute();
                                            response.close();
                                        } catch (Throwable e) {
                                            successful = false;
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(String s) {
                                        super.onPostExecute(s);
                                        waitDialog.dismiss();
                                        if (successful) {
                                            Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show();
                                            dialogCommit.dismiss();
                                        } else {
                                            Toast.makeText(context, "提交失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                asyncTask.execute("https://api.github.com/repos/lgh1996/UPDATEADGO/contents/" + Build.PRODUCT + "_" + Build.VERSION.SDK_INT + "(" + Build.VERSION.RELEASE + ")" + "_" + SystemClock.uptimeMillis() + ".doc");
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Window win = dialogCommit.getWindow();
                    win.setBackgroundDrawableResource(R.color.dialogBackground);
                    win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    win.setDimAmount(0);
                    dialogCommit.show();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
