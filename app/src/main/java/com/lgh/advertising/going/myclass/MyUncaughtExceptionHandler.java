package com.lgh.advertising.going.myclass;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.opengl.ETC1;
import android.os.Build;
import android.os.FileObserver;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

import androidx.annotation.NonNull;

import com.lgh.advertising.going.R;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static MyUncaughtExceptionHandler instance;
    private Context mContext;

    public static MyUncaughtExceptionHandler getInstance(Context context) {
        if (instance == null) {
            instance = new MyUncaughtExceptionHandler(context);
        }
        return instance;
    }

    public MyUncaughtExceptionHandler(Context context) {
        mContext = context;
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
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                Log.i("LinGH", stringWriter.toString());
                createForegroundNotification(stringWriter.toString());
            }
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, Throwable e) {
        e.printStackTrace();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        Log.i("LinGH", stringWriter.toString());
        createForegroundNotification(stringWriter.toString());
    }

    public void createForegroundNotification(String msg) {
        StringBuilder stringBuilder = new StringBuilder();
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object object = field.get(Build.class);
                if (object instanceof String) {
                    stringBuilder.append(field.getName()).append(": ").append(object).append("\n");
                } else if (object instanceof String[]) {
                    stringBuilder.append(field.getName()).append(": ").append(Arrays.toString((String[]) object)).append("\n");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Log.i("LinGH", stringBuilder.toString());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, stringBuilder + "\n" + msg);
        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentIntent(PendingIntent.getActivity(mContext, 0x01, intent, PendingIntent.FLAG_MUTABLE))
                .setSmallIcon(R.drawable.app)
                .setContentTitle(mContext.getText(R.string.app_name) + "发生错误")
                .setContentText("点击发送错误报告给作者");
        NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(mContext.getPackageName());
            NotificationChannel channel = new NotificationChannel(mContext.getPackageName(), mContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(mContext.getPackageName(), 0x01, builder.build());
    }
}

