package com.lgh.tapclick.myclass;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.lgh.tapclick.BuildConfig;
import com.lgh.tapclick.R;
import com.lgh.tapclick.myactivity.ExceptionReportActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static MyUncaughtExceptionHandler instance;
    private final Context mContext;

    public MyUncaughtExceptionHandler(Context context) {
        mContext = context;
    }

    public static MyUncaughtExceptionHandler getInstance(Context context) {
        if (instance == null) {
            instance = new MyUncaughtExceptionHandler(context);
        }
        return instance;
    }

    public void run() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        handleUiThreadException();
    }

    private void handleUiThreadException() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Looper.loop();
                    } catch (Throwable e) {
                        createExceptionNotification(e);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable e) {
        createExceptionNotification(e);
        e.printStackTrace();
    }

    public void createExceptionNotification(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        String message = Build.FINGERPRINT + "\n"
                + "BuildTime: " + BuildConfig.BUILD_TIME + "\n"
                + "VersionCode: " + BuildConfig.VERSION_CODE + "\n"
                + stringWriter;
        try {
            File file = new File(mContext.getFilesDir(), "exception.txt");
            FileUtils.writeStringToFile(file, message, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
        Intent intent = new Intent(mContext, ExceptionReportActivity.class);
        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentIntent(PendingIntent.getActivity(mContext, 0x01, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.app)
                .setContentTitle(mContext.getText(R.string.appName) + "发生异常")
                .setContentText(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(mContext.getPackageName());
            NotificationChannel channel = new NotificationChannel(mContext.getPackageName(), mContext.getString(R.string.appName), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(mContext.getPackageName(), 0x01, builder.build());
    }
}
