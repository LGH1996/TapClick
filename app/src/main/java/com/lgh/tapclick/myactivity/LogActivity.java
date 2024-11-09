package com.lgh.tapclick.myactivity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.lgh.tapclick.BuildConfig;
import com.lgh.tapclick.databinding.ActivityLogBinding;
import com.lgh.tapclick.myfunction.MyUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LogActivity extends BaseActivity {
    private ActivityLogBinding logBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logBinding = ActivityLogBinding.inflate(getLayoutInflater());
        setContentView(logBinding.getRoot());
        logBinding.export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileUtils.cleanDirectory(getCacheDir());
                    File file = new File(getCacheDir(), "log.txt");
                    FileUtils.writeStringToFile(file, logBinding.log.getText().toString(), StandardCharsets.UTF_8);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                    intent.setDataAndType(uri, getContentResolver().getType(uri));
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setClipData(new ClipData(ClipData.newUri(getContentResolver(), "log", uri)));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "导出"));
                } catch (IOException ex) {
                    Toast.makeText(getApplicationContext(), "生成日志文件时发生错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        logBinding.log.setText(MyUtils.getLog());
        logBinding.scroll.post(new Runnable() {
            @Override
            public void run() {
                logBinding.scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
