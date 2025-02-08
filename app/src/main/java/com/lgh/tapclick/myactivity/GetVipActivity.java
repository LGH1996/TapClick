package com.lgh.tapclick.myactivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lgh.tapclick.BuildConfig;
import com.lgh.tapclick.databinding.ActivityGetVipBinding;
import com.lgh.tapclick.databinding.ViewDialogGetVipBinding;
import com.lgh.tapclick.myclass.MyApplication;
import com.lgh.tapclick.myfunction.MyUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GetVipActivity extends BaseActivity {

    private ActivityGetVipBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGetVipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress <= 10) {
                    binding.indicator.start();
                }
                if (newProgress >= 90) {
                    binding.indicator.complete();
                }
            }
        });
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.supportMultipleWindows();
        binding.webView.loadUrl("https://docs.qq.com/doc/DWVJmQndIaWtiVUFG");

        binding.getVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewDialogGetVipBinding binding = ViewDialogGetVipBinding.inflate(getLayoutInflater());
                binding.deviceNo.setText(MyUtils.getMyDeviceNo());
                binding.deviceNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboardManager = getSystemService(ClipboardManager.class);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(binding.deviceNo.getText(), binding.deviceNo.getText()));
                        Toast.makeText(GetVipActivity.this, "设备号已复制", Toast.LENGTH_LONG).show();
                    }
                });
                binding.paymentNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            binding.paymentNo.performLongClick();
                        }
                    }
                });
                AlertDialog dialog = new AlertDialog.Builder(GetVipActivity.this)
                        .setView(binding.getRoot())
                        .setCancelable(false)
                        .show();
                binding.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                binding.sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (StrUtil.isBlank(binding.paymentNo.getText())) {
                            Toast.makeText(GetVipActivity.this, "请输入激活码", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Map<String, String> map = new HashMap<>();
                        map.put("deviceNo", StrUtil.toStringOrNull(binding.deviceNo.getText()));
                        map.put("deviceName", String.format("%s/%s", Build.MANUFACTURER, Build.MODEL));
                        map.put("androidVersion", String.format("%s/%s/%s", Build.VERSION.RELEASE, Build.VERSION.SDK_INT, DateUtil.format(new Date(Build.TIME), "yyyyMMddHHmmss")));
                        map.put("appVersion", String.format("%s/%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
                        map.put("paymentNo", StrUtil.toStringOrNull(binding.paymentNo.getText()));
                        Observable<String> observable = MyApplication.myHttpRequest.getVip(map);
                        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull String s) {
                                try {
                                    JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
                                    boolean ok = jsonObject.get("ok").getAsBoolean();
                                    String msg = jsonObject.get("msg").getAsString();
                                    if (ok) {
                                        MyUtils.setIsVip(true);
                                        Toast.makeText(GetVipActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(GetVipActivity.this, msg, Toast.LENGTH_LONG).show();
                                    }
                                } catch (RuntimeException e) {
                                    Toast.makeText(GetVipActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                Toast.makeText(GetVipActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.webView.destroy();
    }
}