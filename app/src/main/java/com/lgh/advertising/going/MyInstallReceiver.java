package com.lgh.advertising.going;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.AutoFinder;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;

import java.util.Collections;
import java.util.List;

public class MyInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        try {
            String action = intent.getAction();
            if (action != null) {
                String dataString = intent.getDataString();
                String packageName = dataString != null ? dataString.substring(8) : null;
                if (packageName != null) {
                    DataDao dataDao = DataDaoFactory.getInstance(context.getApplicationContext());
                    PackageManager packageManager = context.getPackageManager();
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                        List<InputMethodInfo> inputMethodInfoList = inputMethodManager.getInputMethodList();
                        for (InputMethodInfo e : inputMethodInfoList) {
                            if (packageName.equals(e.getPackageName())) {
                                return;
                            }
                        }
                        AppDescribe appDescribe = dataDao.getAppDescribeByPackage(packageName);
                        if (appDescribe == null) {
                            appDescribe = new AppDescribe();
                            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                            appDescribe.appName = packageManager.getApplicationLabel(applicationInfo).toString();
                            appDescribe.appPackage = packageName;
                            List<ResolveInfo> homeLaunchList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_ALL);
                            for (ResolveInfo e : homeLaunchList) {
                                if (packageName.equals(e.activityInfo.packageName)) {
                                    appDescribe.on_off = false;
                                }
                            }
                            AutoFinder autoFinder = new AutoFinder();
                            autoFinder.appPackage = packageName;
                            autoFinder.keywordList = Collections.singletonList("跳过");
                            dataDao.insertAppDescribe(appDescribe);
                            dataDao.insertAutoFinder(autoFinder);
                            appDescribe.getOtherFieldsFromDatabase(dataDao);
                            if (MyAccessibilityService.mainFunction != null) {
                                MyAccessibilityService.mainFunction.getAppDescribeMap().put(appDescribe.appPackage, appDescribe);
                            }
                            if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                                MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap().put(appDescribe.appPackage, appDescribe);
                            }
                        }
                    }
                    if (action.equals(Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
                        dataDao.deleteAppDescribeByPackage(packageName);
                        if (MyAccessibilityService.mainFunction != null) {
                            MyAccessibilityService.mainFunction.getAppDescribeMap().remove(packageName);
                        }
                        if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                            MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap().remove(packageName);
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
//            throwable.printStackTrace();
        }
    }
}
