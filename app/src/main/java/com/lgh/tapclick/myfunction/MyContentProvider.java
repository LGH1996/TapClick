package com.lgh.tapclick.myfunction;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.myclass.DataDao;
import com.lgh.tapclick.myclass.MyApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyContentProvider extends ContentProvider {
    private final Handler handler;
    private final DataDao dataDao;

    public MyContentProvider() {
        handler = new Handler(Looper.getMainLooper());
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
        if (TextUtils.equals(selection, "isServiceRunning")) {
            boolean isRunning = MyAccessibilityService.mainFunction != null || MyAccessibilityServiceNoGesture.mainFunction != null;
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"isServiceRunning"});
            matrixCursor.addRow(new Object[]{isRunning ? 1 : 0});
            return matrixCursor;
        }
        if (TextUtils.equals(selection, "log")) {
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"log"});
            if (MyAccessibilityService.mainFunction != null) {
                matrixCursor.addRow(new Object[]{MyAccessibilityService.mainFunction.getLog()});
            } else if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                matrixCursor.addRow(new Object[]{MyAccessibilityServiceNoGesture.mainFunction.getLog()});
            } else {
                matrixCursor.addRow(new Object[]{"无障碍服务未开启"});
            }
            return matrixCursor;
        }
        return new MatrixCursor(new String[]{});
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (MyAccessibilityService.mainFunction != null) {
            updateData(MyAccessibilityService.mainFunction.getAppDescribeMap(), values);
            updateAllDate(MyAccessibilityService.mainFunction.getAppDescribeMap(), values);
            updateKeepAlive(MyAccessibilityService.mainFunction, values);
            showDbClickSetting(MyAccessibilityService.mainFunction, values);
            showDbClickFloating(MyAccessibilityService.mainFunction, values);
            showAddDataWindow(MyAccessibilityService.mainFunction, values);
        }
        if (MyAccessibilityServiceNoGesture.mainFunction != null) {
            updateData(MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap(), values);
            updateAllDate(MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap(), values);
            updateKeepAlive(MyAccessibilityServiceNoGesture.mainFunction, values);
            showDbClickSetting(MyAccessibilityServiceNoGesture.mainFunction, values);
            showDbClickFloating(MyAccessibilityServiceNoGesture.mainFunction, values);
            showAddDataWindow(MyAccessibilityServiceNoGesture.mainFunction, values);
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
                    appDescribeNew.widgetSetMap = appDescribe.widgetSetMap;
                    appDescribeNew.coordinateMap = appDescribe.coordinateMap;
                    appDescribeMap.put(packageName, appDescribeNew);
                }
            }
        }
        if (TextUtils.equals(updateScope, "updateWidget")) {
            AppDescribe appDescribe = appDescribeMap.get(packageName);
            if (appDescribe != null) {
                appDescribe.getWidgetFromDatabase(dataDao);
            }
        }
        if (TextUtils.equals(updateScope, "updateCoordinate")) {
            AppDescribe appDescribe = appDescribeMap.get(packageName);
            if (appDescribe != null) {
                appDescribe.getCoordinateFromDatabase(dataDao);
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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mainFunction.keepAliveByFloatingWindow(value);
                }
            });
        }
    }

    private void updateAllDate(Map<String, AppDescribe> appDescribeMap, ContentValues values) {
        String updateScope = values.getAsString("updateScope");
        if (!TextUtils.equals(updateScope, "allDate")) {
            return;
        }
        Map<String, AppDescribe> newAppDescribeMap = new HashMap<>();
        for (String e : appDescribeMap.keySet()) {
            AppDescribe appDescribeTmp = dataDao.getAppDescribeByPackage(e);
            if (appDescribeTmp != null) {
                appDescribeTmp.getOtherFieldsFromDatabase(dataDao);
                newAppDescribeMap.put(e, appDescribeTmp);
            }
        }
        appDescribeMap.putAll(newAppDescribeMap);
    }

    private void showDbClickSetting(MainFunction mainFunction, ContentValues values) {
        String updateScope = values.getAsString("updateScope");
        if (!TextUtils.equals(updateScope, "showDbClickSetting")) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                mainFunction.showDbClickSetting();
            }
        });
    }

    private void showDbClickFloating(MainFunction mainFunction, ContentValues values) {
        String updateScope = values.getAsString("updateScope");
        Boolean enable = values.getAsBoolean("value");
        if (!TextUtils.equals(updateScope, "showDbClickFloating") || Objects.isNull(enable)) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                mainFunction.showDbClickFloating(enable);
            }
        });
    }

    private void showAddDataWindow(MainFunction mainFunction, ContentValues values) {
        String updateScope = values.getAsString("updateScope");
        if (!TextUtils.equals(updateScope, "showAddDataWindow")) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                mainFunction.showAddDataWindow(false);
            }
        });
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder, @Nullable CancellationSignal cancellationSignal) {
        Bundle bundle = new Bundle();
        if (MyAccessibilityService.mainFunction != null) {
            bundle.putString("log", MyAccessibilityService.mainFunction.getLog());
        } else if (MyAccessibilityServiceNoGesture.mainFunction != null) {
            bundle.putString("log", MyAccessibilityServiceNoGesture.mainFunction.getLog());
        }
        Cursor cursor = super.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
        cursor.setExtras(bundle);
        return cursor;
    }
}
