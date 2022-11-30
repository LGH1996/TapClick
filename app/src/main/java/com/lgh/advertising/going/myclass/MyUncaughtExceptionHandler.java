package com.lgh.advertising.going.myclass;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.lgh.advertising.going.BuildConfig;
import com.lgh.advertising.going.R;
import com.lgh.advertising.going.myactivity.ExceptionReportActivity;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e) {
                createExceptionNotification(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, Throwable e) {
        createExceptionNotification(e);
        e.printStackTrace();
    }

    public void createExceptionNotification(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
        Intent intent = new Intent(mContext, ExceptionReportActivity.class);
        String message = Build.FINGERPRINT + "\n"
                + "BuildTime: " + BuildConfig.BUILD_TIME + "\n"
                + "VersionCode: " + BuildConfig.VERSION_CODE + "\n"
                + stringWriter;
        intent.putExtra(Intent.EXTRA_TEXT, message);
        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentIntent(PendingIntent.getActivity(mContext, 0x01, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.app)
                .setContentTitle(mContext.getText(R.string.app_name) + "发生异常")
                .setContentText("点击查看");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(mContext.getPackageName());
            NotificationChannel channel = new NotificationChannel(mContext.getPackageName(), mContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(mContext.getPackageName(), 0x01, builder.build());
    }
}
