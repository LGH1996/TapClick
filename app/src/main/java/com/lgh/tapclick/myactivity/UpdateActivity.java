package com.lgh.tapclick.myactivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lgh.tapclick.databinding.ActivityUpdateBinding;

public class UpdateActivity extends BaseActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUpdateBinding updateBinding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(updateBinding.getRoot());
        setFinishOnTouchOutside(false);
        updateBinding.webViewUpdate.getSettings().setJavaScriptEnabled(true);
        updateBinding.webViewUpdate.loadData(getIntent().getStringExtra("updateMessage"), "text/html", "utf-8");
        updateBinding.webViewUpdate.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                UpdateActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                UpdateActivity.this.finishAndRemoveTask();
                return true;
            }
        });
        updateBinding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateActivity.this.finishAndRemoveTask();
            }
        });
        updateBinding.sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getIntent().getStringExtra("updateUrl"))));
                UpdateActivity.this.finishAndRemoveTask();
            }
        });
    }
}
