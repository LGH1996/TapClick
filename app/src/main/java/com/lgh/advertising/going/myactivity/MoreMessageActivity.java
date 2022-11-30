package com.lgh.advertising.going.myactivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lgh.advertising.going.databinding.ActivityMoreMessageBinding;

public class MoreMessageActivity extends BaseActivity {
    private ActivityMoreMessageBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoreMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.webViewMore.setWebViewClient(new WebViewClient());
        binding.webViewMore.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    binding.progress.setVisibility(View.GONE);
                }
            }
        });
        WebSettings settings = binding.webViewMore.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.supportMultipleWindows();
        binding.webViewMore.loadUrl("https://gitee.com/lingh1996/ADGO/blob/master/README.md");
    }

    @Override
    public void onBackPressed() {
        if (binding.webViewMore.canGoBack()) {
            binding.webViewMore.goBack();
            return;
        }
        super.onBackPressed();
    }
}
