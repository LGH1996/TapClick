package com.lgh.advertising.going.myfunction;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.lgh.advertising.going.BuildConfig;

public class MyUtils {
    private static final String ACTION_SHOW_ADD_DATA_WINDOW = "action.lingh.show.add.data.window";
    private static final String contentProviderAuthority = "content://" + BuildConfig.APPLICATION_ID;
    private static MyUtils mInstance;
    private final Context mContext;

    private MyUtils(Context context) {
        mContext = context;
    }

    public static MyUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyUtils(context);
        }
        return mInstance;
    }

    public void requestShowAddDataWindow() {
        Intent intent = new Intent(ACTION_SHOW_ADD_DATA_WINDOW);
        intent.setPackage(mContext.getPackageName());
        mContext.sendBroadcast(intent);
    }

    public boolean isServiceRunning() {
        Cursor cursor = mContext.getContentResolver().query(Uri.parse(contentProviderAuthority), null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex("isRunning");
        boolean isRunning = cursor.getInt(index) == 1;
        cursor.close();
        return isRunning;
    }

    public boolean requestUpdateAppDescribe(String packageName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("updateScope", "updateAppDescribe");
        contentValues.put("packageName", packageName);
        int re = mContext.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;
    }

    public boolean requestUpdateAutoFinder(String packageName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("updateScope", "updateAutoFinder");
        contentValues.put("packageName", packageName);
        int re = mContext.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;
    }

    public boolean requestUpdateWidget(String packageName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("updateScope", "updateWidget");
        contentValues.put("packageName", packageName);
        int re = mContext.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;
    }

    public boolean requestUpdateCoordinate(String packageName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("updateScope", "updateCoordinate");
        contentValues.put("packageName", packageName);
        int re = mContext.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;
    }

    public boolean requestUpdateKeepAliveByNotification(boolean enable) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("updateScope", "keepAliveByNotification");
        contentValues.put("value", enable);
        int re = mContext.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;
    }

    public boolean requestUpdateKeepAliveByFloatingWindow(boolean enable) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("updateScope", "keepAliveByFloatingWindow");
        contentValues.put("value", enable);
        int re = mContext.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;
    }

    public boolean getKeepAliveByNotification() {
        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        return preferences.getBoolean("keepAliveByNotification", false);
    }

    public boolean setKeepAliveByNotification(boolean enable) {
        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        preferences.edit().putBoolean("keepAliveByNotification", enable).apply();
        return true;
    }

    public boolean getKeepAliveByFloatingWindow() {
        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        return preferences.getBoolean("keepAliveByFloatingWindow", false);
    }

    public boolean setKeepAliveByFloatingWindow(boolean enable) {
        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        preferences.edit().putBoolean("keepAliveByFloatingWindow", enable).apply();
        return true;
    }
}
