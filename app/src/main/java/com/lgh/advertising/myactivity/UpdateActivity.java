package com.lgh.advertising.myactivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.lgh.advertising.going.R;

public class UpdateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        setFinishOnTouchOutside(false);
        WebView webView = findViewById(R.id.webView_update);
        Button cancel = findViewById(R.id.cancel);
        Button sure = findViewById(R.id.sure);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadData(getIntent().getStringExtra("updateMessage"), "text/html", "utf-8");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                UpdateActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                UpdateActivity.this.finishAndRemoveTask();
                return true;
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateActivity.this.finishAndRemoveTask();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getIntent().getStringExtra("updateUrl"))));
                UpdateActivity.this.finishAndRemoveTask();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
