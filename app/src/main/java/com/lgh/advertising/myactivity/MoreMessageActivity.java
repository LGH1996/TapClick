package com.lgh.advertising.myactivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lgh.advertising.going.databinding.ActivityMoreMessageBinding;

public class MoreMessageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMoreMessageBinding moreMessageBinding = ActivityMoreMessageBinding.inflate(getLayoutInflater());
        setContentView(moreMessageBinding.getRoot());

        moreMessageBinding.webViewMore.setWebViewClient(new WebViewClient());
        moreMessageBinding.webViewMore.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    moreMessageBinding.progress.setVisibility(View.GONE);
                }
            }
        });
        WebSettings settings = moreMessageBinding.webViewMore.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.supportMultipleWindows();
        moreMessageBinding.webViewMore.loadUrl("https://gitee.com/lingh1996/ADGO/blob/master/README.md");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
