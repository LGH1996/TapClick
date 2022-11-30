package com.lgh.advertising.going.myfunction;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.myclass.MyApplication;

import java.util.Map;

public class MyContentProvider extends ContentProvider {
    public static Map<String, AppDescribe> appDescribeMap;

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
        if (appDescribeMap == null) {
            return 0;
        }
        String action = values.getAsString("action");
        String packageName = values.getAsString("packageName");
        if (!TextUtils.isEmpty(action) && !TextUtils.isEmpty(packageName)) {
            if (action.equals("updateAppDescribe")) {
                AppDescribe appDescribe = appDescribeMap.get(packageName);
                if (appDescribe != null) {
                    AppDescribe appDescribeNew = MyApplication.dataDao.getAppDescribeByPackage(packageName);
                    if (appDescribeNew != null) {
                        appDescribeNew.autoFinder = appDescribe.autoFinder;
                        appDescribeNew.widgetSetMap = appDescribe.widgetSetMap;
                        appDescribeNew.coordinateMap = appDescribe.coordinateMap;
                        appDescribeMap.put(packageName, appDescribeNew);
                        return 1;
                    }
                }
            }
            if (action.equals("updateAutoFinder")) {
                AppDescribe appDescribe = appDescribeMap.get(packageName);
                if (appDescribe != null) {
                    appDescribe.getAutoFinderFromDatabase(MyApplication.dataDao);
                    return 1;
                }
            }
            if (action.equals("updateWidget")) {
                AppDescribe appDescribe = appDescribeMap.get(packageName);
                if (appDescribe != null) {
                    appDescribe.getWidgetSetMapFromDatabase(MyApplication.dataDao);
                    return 1;
                }
            }
            if (action.equals("updateCoordinate")) {
                AppDescribe appDescribe = appDescribeMap.get(packageName);
                if (appDescribe != null) {
                    appDescribe.getCoordinateMapFromDatabase(MyApplication.dataDao);
                    return 1;
                }
            }
        }
        return 0;
    }
}