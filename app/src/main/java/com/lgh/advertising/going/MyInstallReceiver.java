package com.lgh.advertising.going;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.AutoFinder;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;

import java.util.Arrays;

public class MyInstallReceiver extends BroadcastReceiver {

    public static final String TAG = "MyInstallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        try {
            String action = intent.getAction();
            if (action != null) {
                String dataString = intent.getDataString();
                DataDao dataDao = DataDaoFactory.getInstance(context);
                PackageManager packageManager = context.getPackageManager();
                if (action.equals(Intent.ACTION_PACKAGE_ADDED) && dataString != null) {
                    try {
                        String packageName = dataString.substring(8);
                        AppDescribe appDescribe = new AppDescribe();
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                        appDescribe.appName = packageManager.getApplicationLabel(applicationInfo).toString();
                        appDescribe.appPackage = packageName;
                        AutoFinder autoFinder = new AutoFinder();
                        autoFinder.appPackage = packageName;
                        autoFinder.keywordList = Arrays.asList("跳过");
                        dataDao.insertAppDescribe(appDescribe);
                        dataDao.insertAutoFinder(autoFinder);
                        appDescribe.getOtherField(dataDao);
                        if (MyAccessibilityService.mainFunction != null) {
                            MyAccessibilityService.mainFunction.getAppDescribeMap().put(appDescribe.appPackage, appDescribe);
                        }
                        if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                            MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap().put(appDescribe.appPackage, appDescribe);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (action.equals(Intent.ACTION_PACKAGE_REMOVED) && dataString != null) {
                    String packageName = dataString.substring(8);
                    dataDao.deleteAppDescribeByPackageNames(packageName);
                    if (MyAccessibilityService.mainFunction != null) {
                        MyAccessibilityService.mainFunction.getAppDescribeMap().remove(packageName);
                    }
                    if (MyAccessibilityServiceNoGesture.mainFunction != null) {
                        MyAccessibilityServiceNoGesture.mainFunction.getAppDescribeMap().remove(packageName);
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
