package com.lgh.advertising.going.myactivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.lgh.advertising.going.databinding.ActivityExceptionReportBinding;

public class ExceptionReportActivity extends BaseActivity {

    private ActivityExceptionReportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExceptionReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        String exceptionStr = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        binding.exception.setText(exceptionStr);
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qqUri = "mqqwpa://im/chat?chat_type=wpa&uin=2281442260";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(qqUri));
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_ALL) != null) {
                    startActivity(intent);
                    ClipboardManager clipboardManager = getSystemService(ClipboardManager.class);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(getPackageName(), exceptionStr));
                    Toast.makeText(ExceptionReportActivity.this, "已复制", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ExceptionReportActivity.this, "未安装QQ或TIM", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}