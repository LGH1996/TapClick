package com.lgh.tapclick.myactivity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.lgh.tapclick.BuildConfig;
import com.lgh.tapclick.databinding.ActivityExceptionReportBinding;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExceptionReportActivity extends BaseActivity {
    private ActivityExceptionReportBinding exceptionReportBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exceptionReportBinding = ActivityExceptionReportBinding.inflate(getLayoutInflater());
        setContentView(exceptionReportBinding.getRoot());
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            File file = new File(getFilesDir(), "exception.txt");
            String exceptionMsg = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            exceptionReportBinding.exception.setText(exceptionMsg);
            exceptionReportBinding.export.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                    intent.setDataAndType(uri, getContentResolver().getType(uri));
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setClipData(new ClipData(ClipData.newUri(getContentResolver(), "exception", uri)));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "导出"));
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
