package com.lgh.advertising.going.myfunction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import androidx.core.util.Consumer;

import com.lgh.advertising.going.BuildConfig;

public class MyUtils {
    private static final String ACTION_SHOW_ADD_DATA_WINDOW = "action.lingh.show.add.data.window";
    private static final String ACTION_REQUEST_UPDATE_DATA = "action.lingh.request.update.data";
    private static final String ACTION_CHECK_SERVICE_STATE = "action.lingh.check.service.state";
    private static final String contentProviderAuthority = "content://" + BuildConfig.APPLICATION_ID;
    private static MyUtils mInstance;

    public static MyUtils getInstance() {
        if (mInstance == null) {
            mInstance = new MyUtils();
        }
        return mInstance;
    }

    public void requestShowAddDataWindow(Context context) {
        Intent intent = new Intent(ACTION_SHOW_ADD_DATA_WINDOW);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    public boolean isAccessibilityServiceRunning(Context context) {
        Cursor cursor = context.getContentResolver().query(Uri.parse(contentProviderAuthority), null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex("isRunning");
        boolean isRunning = cursor.getInt(index) == 1;
        cursor.close();
        return isRunning;
    }

    public boolean checkServiceState(Context context, Consumer<Boolean> stateResult) {
        Intent intent = new Intent(ACTION_CHECK_SERVICE_STATE);
        intent.setPackage(context.getPackageName());
        context.sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stateResult.accept(getResultCode() == 1);
            }
        }, null, 0, null, null);
        return true;
    }

    public boolean requestUpdateAppDescribe(Context context, String packageName) {
        Intent intent = new Intent(ACTION_REQUEST_UPDATE_DATA);
        intent.setPackage(context.getPackageName());
        intent.putExtra("updateScope", "updateAppDescribe");
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
        return true;
        /*ContentValues contentValues = new ContentValues();
        contentValues.put("action", "updateAppDescribe");
        contentValues.put("packageName", packageName);
        int re = context.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;*/
    }

    public boolean requestUpdateAutoFinder(Context context, String packageName) {
        Intent intent = new Intent(ACTION_REQUEST_UPDATE_DATA);
        intent.setPackage(context.getPackageName());
        intent.putExtra("updateScope", "updateAutoFinder");
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
        return true;
        /*ContentValues contentValues = new ContentValues();
        contentValues.put("action", "updateAutoFinder");
        contentValues.put("packageName", packageName);
        int re = context.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;*/
    }

    public boolean requestUpdateWidget(Context context, String packageName) {
        Intent intent = new Intent(ACTION_REQUEST_UPDATE_DATA);
        intent.setPackage(context.getPackageName());
        intent.putExtra("updateScope", "updateWidget");
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
        return true;
        /*ContentValues contentValues = new ContentValues();
        contentValues.put("action", "updateWidget");
        contentValues.put("packageName", packageName);
        int re = context.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;*/
    }

    public boolean requestUpdateCoordinate(Context context, String packageName) {
        Intent intent = new Intent(ACTION_REQUEST_UPDATE_DATA);
        intent.setPackage(context.getPackageName());
        intent.putExtra("updateScope", "updateCoordinate");
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
        return true;
        /*ContentValues contentValues = new ContentValues();
        contentValues.put("action", "updateCoordinate");
        contentValues.put("packageName", packageName);
        int re = context.getContentResolver().update(Uri.parse(contentProviderAuthority), contentValues, null, null);
        return re > 0;*/
    }
}
