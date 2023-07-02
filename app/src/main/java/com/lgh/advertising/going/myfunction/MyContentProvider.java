package com.lgh.advertising.going.myfunction;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;

import java.util.Map;
import java.util.Objects;

public class MyContentProvider extends ContentProvider {

    private final DataDao dataDao;

    public MyContentProvider() {
        dataDao = MyApplication.dataDao;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return -1;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        boolean isRunning = MyAccessibilityService.mainFunction != null || MyAccessibilityServiceNoGesture.mainFunction != null;
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"isRunning"});
        matrixCursor.addRow(new Object[]{isRunning ? 1 : 0});
        return matrixCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (MyAccessibilityService.mainFunction != null) {
            updateData(MyAccessibilityService.mainFunction.getAppDescribeMap(), values);
            updateKeepAlive(MyAccessibilityService.mainFunction, values);
        }
        if (MyAccessibilityServiceNoGesture.mainFunction != null) {
            updateData(MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap(), values);
            updateKeepAlive(MyAccessibilityServiceNoGesture.mainFunction, values);
        }
        return 1;
    }

    private void updateData(Map<String, AppDescribe> appDescribeMap, ContentValues values) {
        String updateScope = values.getAsString("updateScope");
        String packageName = values.getAsString("packageName");
        if (TextUtils.isEmpty(updateScope) || TextUtils.isEmpty(packageName)) {
            return;
        }
        if (TextUtils.equals(updateScope, "updateAppDescribe")) {
            AppDescribe appDescribe = appDescribeMap.get(packageName);
            if (appDescribe != null) {
                AppDescribe appDescribeNew = dataDao.getAppDescribeByPackage(packageName);
                if (appDescribeNew != null) {
                    appDescribeNew.autoFinder = appDescribe.autoFinder;
                    appDescribeNew.widgetSetMap = appDescribe.widgetSetMap;
                    appDescribeNew.coordinateMap = appDescribe.coordinateMap;
                    appDescribeMap.put(packageName, appDescribeNew);
                }
            }
        }
        if (TextUtils.equals(updateScope, "updateAutoFinder")) {
            AppDescribe appDescribe = appDescribeMap.get(packageName);
            if (appDescribe != null) {
                appDescribe.getAutoFinderFromDatabase(dataDao);
            }
        }
        if (TextUtils.equals(updateScope, "updateWidget")) {
            AppDescribe appDescribe = appDescribeMap.get(packageName);
            if (appDescribe != null) {
                appDescribe.getWidgetSetMapFromDatabase(dataDao);
            }
        }
        if (TextUtils.equals(updateScope, "updateCoordinate")) {
            AppDescribe appDescribe = appDescribeMap.get(packageName);
            if (appDescribe != null) {
                appDescribe.getCoordinateMapFromDatabase(dataDao);
            }
        }
    }

    private void updateKeepAlive(MainFunction mainFunction, ContentValues values) {
        String updateScope = values.getAsString("updateScope");
        Boolean value = values.getAsBoolean("value");
        if (TextUtils.isEmpty(updateScope) || Objects.isNull(value)) {
            return;
        }
        if (TextUtils.equals(updateScope, "keepAliveByNotification")) {
            mainFunction.keepAliveByNotification(value);
        }
        if (TextUtils.equals(updateScope, "keepAliveByFloatingWindow")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mainFunction.keepAliveByFloatingWindow(value);
                }
            });
        }
    }
}
