package com.lgh.advertising.going.myfunction;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.lgh.advertising.going.R;
import com.lgh.advertising.going.databinding.ViewAddDataBinding;
import com.lgh.advertising.going.databinding.ViewAddWarningBinding;
import com.lgh.advertising.going.databinding.ViewWidgetSelectBinding;
import com.lgh.advertising.going.myactivity.EditDataActivity;
import com.lgh.advertising.going.myactivity.MainActivity;
import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.mybean.AutoFinder;
import com.lgh.advertising.going.mybean.Coordinate;
import com.lgh.advertising.going.mybean.Widget;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * adb shell pm grant com.lgh.advertising.going android.permission.WRITE_SECURE_SETTINGS
 * adb shell settings put secure enabled_accessibility_services com.lgh.advertising.going/com.lgh.advertising.going.myfunction.MyAccessibilityService
 * adb shell settings put secure accessibility_enabled 1
 * <p>
 * Settings.Secure.putString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, getPackageName()+"/"+MyAccessibilityService.class.getName());
 * Settings.Secure.putString(getContentResolver(),Settings.Secure.ACCESSIBILITY_ENABLED, "1");
 */

public class MainFunction {

    private final AccessibilityService service;
    private WindowManager windowManager;
    private PackageManager packageManager;
    private InputMethodManager inputMethodManager;
    private DataDao dataDao;
    private Map<String, AppDescribe> appDescribeMap;
    private AppDescribe appDescribe;
    private String currentPackage;
    private String currentPackageSub;
    private String currentActivity;
    private volatile boolean onOffAutoFinder;
    private volatile boolean onOffWidget;
    private volatile boolean onOffWidgetSub;
    private volatile boolean onOffCoordinate;
    private volatile boolean onOffCoordinateSub;
    private volatile boolean widgetAllNoRepeat;
    private volatile int autoRetrieveNumber;
    private AccessibilityServiceInfo serviceInfo;
    private ScheduledFuture<?> futureAutoFinder;
    private ScheduledFuture<?> futureWidget;
    private ScheduledFuture<?> futureCoordinate;
    private ScheduledExecutorService executorService;
    private Set<Widget> widgetSet;
    private MyBroadcastReceiver myBroadcastReceiver;
    private MyPackageReceiver myPackageReceiver;
    private Set<Widget> alreadyClickSet;
    private Map<String, Coordinate> coordinateMap;
    private List<String> keywordList;
    private Map<String, Set<Widget>> widgetSetMap;
    private Coordinate coordinate;
    private ScheduledFuture<?> futureCoordinateClick;
    private WindowManager.LayoutParams aParams, bParams, cParams;
    private ViewAddDataBinding addDataBinding;
    private ViewWidgetSelectBinding widgetSelectBinding;
    private ImageView viewClickPosition;
    private Set<String> pkgSuggestNotOnList;
    private View ignoreView;
    private static final String ACTION_SHOW_ADD_DATA_WINDOW = "action.lingh.show.add.data.window";
    private static final String isScreenOffPre = "isScreenOffPre";

    public MainFunction(AccessibilityService service) {
        this.service = service;
        inputMethodManager = service.getSystemService(InputMethodManager.class);
        windowManager = service.getSystemService(WindowManager.class);
    }

    protected void onServiceConnected() {
        packageManager = service.getPackageManager();
        currentPackage = "Initialize CurrentPackage";
        currentActivity = "Initialize CurrentActivity";
        currentPackageSub = currentPackage;
        executorService = Executors.newSingleThreadScheduledExecutor();
        serviceInfo = service.getServiceInfo();
        dataDao = MyApplication.dataDao;
        appDescribeMap = new HashMap<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(ACTION_SHOW_ADD_DATA_WINDOW);
        myBroadcastReceiver = new MyBroadcastReceiver();
        service.registerReceiver(myBroadcastReceiver, intentFilter);
        IntentFilter filterPackage = new IntentFilter();
        filterPackage.addAction(Intent.ACTION_PACKAGE_ADDED);
        filterPackage.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filterPackage.addDataScheme("package");
        myPackageReceiver = new MyPackageReceiver();
        service.registerReceiver(myPackageReceiver, filterPackage);
        keepAliveByNotification(MyUtils.getInstance(service).getKeepAliveByNotification());
        keepAliveByFloatingWindow(MyUtils.getInstance(service).getKeepAliveByFloatingWindow());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                getRunningData();
            }
        });
        futureCoordinate = futureWidget = futureAutoFinder = futureCoordinateClick = executorService.schedule(new Runnable() {
            @Override
            public void run() {
            }
        }, 0, TimeUnit.MILLISECONDS);

        /*executorService.schedule(new Runnable() {
            @Override
            public void run() {
                NotificationManager notificationManager = service.getSystemService(NotificationManager.class);
                Notification.Builder builder = new Notification.Builder(service)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.app)
                        .setContentTitle(service.getText(R.string.app_name))
                        .setContentText("占用内存" + Debug.getPss() + "kb");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setChannelId(service.getPackageName());
                    NotificationChannel channel = new NotificationChannel(service.getPackageName(), service.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(service.getPackageName(), 0x01, builder.build());
                executorService.schedule(this, 5000, TimeUnit.MILLISECONDS);
            }
        }, 0, TimeUnit.MILLISECONDS);*/
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                AccessibilityNodeInfo root = service.getRootInActiveWindow();
                String packageName = root != null ? root.getPackageName().toString() : null;
                String activityName = event.getClassName() != null ? event.getClassName().toString() : null;

                if (packageName == null) {
                    break;
                }
                if (!packageName.equals(currentPackage)) {
                    currentPackage = packageName;
                    appDescribe = appDescribeMap.get(packageName);
                }
                if (appDescribe == null) {
                    break;
                }
                if (!event.isFullScreen()
                        && !appDescribe.onOff
                        && !currentPackageSub.equals(isScreenOffPre)) {
                    break;
                }
                if (!packageName.equals(currentPackageSub)) {
                    currentPackageSub = packageName;
                    futureAutoFinder.cancel(false);
                    futureWidget.cancel(false);
                    futureCoordinate.cancel(false);
                    serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                    service.setServiceInfo(serviceInfo);
                    onOffAutoFinder = false;
                    onOffWidget = false;
                    onOffCoordinate = false;
                    onOffWidgetSub = false;
                    onOffCoordinateSub = false;
                    keywordList = null;
                    widgetSetMap = null;
                    coordinateMap = null;
                    autoRetrieveNumber = 0;
                    if (appDescribe != null && appDescribe.onOff) {
                        keywordList = appDescribe.autoFinder.keywordList;
                        widgetSetMap = appDescribe.widgetSetMap;
                        coordinateMap = appDescribe.coordinateMap;
                        onOffAutoFinder = appDescribe.autoFinderOnOFF && !keywordList.isEmpty();
                        onOffWidget = appDescribe.widgetOnOff && !widgetSetMap.isEmpty();
                        onOffCoordinate = appDescribe.coordinateOnOff && !coordinateMap.isEmpty();

                        if (onOffAutoFinder) {
                            serviceInfo.eventTypes |= AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                            service.setServiceInfo(serviceInfo);
                        }

                        if (onOffAutoFinder && !appDescribe.autoFinderRetrieveAllTime) {
                            futureAutoFinder = executorService.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    onOffAutoFinder = false;
                                    if (!onOffWidgetSub) {
                                        serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                                        service.setServiceInfo(serviceInfo);
                                    }
                                }
                            }, appDescribe.autoFinderRetrieveTime, TimeUnit.MILLISECONDS);
                        }

                        if (onOffWidget && !appDescribe.widgetRetrieveAllTime) {
                            futureWidget = executorService.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    onOffWidget = false;
                                    onOffWidgetSub = false;
                                    if (!onOffAutoFinder) {
                                        serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                                        service.setServiceInfo(serviceInfo);
                                    }
                                }
                            }, appDescribe.widgetRetrieveTime, TimeUnit.MILLISECONDS);
                        }

                        if (onOffCoordinate && !appDescribe.coordinateRetrieveAllTime) {
                            futureCoordinate = executorService.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    onOffCoordinate = false;
                                    onOffCoordinateSub = false;
                                }
                            }, appDescribe.coordinateRetrieveTime, TimeUnit.MILLISECONDS);
                        }
                    }
                }

                if (activityName == null) {
                    break;
                }
                if (activityName.startsWith("android.widget.")
                        || activityName.startsWith("android.view.")
                        || activityName.equals("android.inputmethodservice.SoftInputWindow")) {
                    break;
                }
                if (!activityName.equals(currentActivity)) {
                    currentActivity = activityName;
                    alreadyClickSet = new HashSet<>();
                    onOffWidgetSub = false;
                    onOffCoordinateSub = false;
                    widgetAllNoRepeat = true;
                    widgetSet = null;
                    coordinate = null;

                    if (!onOffAutoFinder) {
                        serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                        service.setServiceInfo(serviceInfo);
                    }

                    if (appDescribe != null) {
                        coordinate = coordinateMap != null ? coordinateMap.get(activityName) : null;
                        widgetSet = widgetSetMap != null ? widgetSetMap.get(activityName) : null;
                        onOffCoordinateSub = onOffCoordinate && coordinate != null;
                        onOffWidgetSub = onOffWidget && widgetSet != null;

                        if (widgetSet != null && !widgetSet.isEmpty()) {
                            for (Widget e : widgetSet) {
                                if (!e.noRepeat) {
                                    widgetAllNoRepeat = false;
                                    break;
                                }
                            }
                        }

                        if (onOffWidgetSub) {
                            serviceInfo.eventTypes |= AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                            service.setServiceInfo(serviceInfo);
                        }

                        if (onOffCoordinateSub) {
                            futureCoordinateClick.cancel(false);
                            futureCoordinateClick = executorService.scheduleAtFixedRate(new Runnable() {
                                private final Coordinate coordinateSub = coordinate;
                                private int num = 0;

                                @Override
                                public void run() {
                                    if (onOffCoordinateSub && ++num <= coordinateSub.clickNumber && currentActivity.equals(coordinateSub.appActivity)) {
                                        click(coordinateSub.xPosition, coordinateSub.yPosition, 0, 20);
                                    } else {
                                        throw new RuntimeException();
                                    }
                                }
                            }, coordinate.clickDelay, coordinate.clickInterval, TimeUnit.MILLISECONDS);
                        }
                    }
                }
                if (onOffAutoFinder && appDescribe != null) {
                    findButtonByText(root, appDescribe.autoFinder);
                }
                if (onOffWidgetSub && widgetSet != null) {
                    findButtonByWidget(root, widgetSet);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (!TextUtils.equals(event.getPackageName(), currentPackageSub)) {
                    break;
                }
                AccessibilityNodeInfo source = event.getSource();
                if (source == null) {
                    break;
                }
                if (onOffAutoFinder && appDescribe != null) {
                    findButtonByText(source, appDescribe.autoFinder);
                }
                if (onOffWidgetSub && widgetSet != null) {
                    findButtonByWidget(source, widgetSet);
                }
                break;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (addDataBinding != null && viewClickPosition != null && widgetSelectBinding != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            aParams.x = (metrics.widthPixels - aParams.width) / 2;
            aParams.y = metrics.heightPixels - aParams.height;
            bParams.width = metrics.widthPixels;
            bParams.height = metrics.heightPixels;
            cParams.x = (metrics.widthPixels - cParams.width) / 2;
            cParams.y = (metrics.heightPixels - cParams.height) / 2;
            windowManager.updateViewLayout(addDataBinding.getRoot(), aParams);
            windowManager.updateViewLayout(viewClickPosition, cParams);
            widgetSelectBinding.frame.removeAllViews();
            TextView text = new TextView(service);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(0xffff0000);
            text.setText("请重新刷新布局");
            widgetSelectBinding.frame.addView(text, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            windowManager.updateViewLayout(widgetSelectBinding.frame, bParams);
        }
    }

    public boolean onUnbind(Intent intent) {
        service.unregisterReceiver(myBroadcastReceiver);
        service.unregisterReceiver(myPackageReceiver);
        return true;
    }

    /**
     * 将数据暴露给其他组件
     * 方便修改
     */
    public Map<String, AppDescribe> getAppDescribeMap() {
        return appDescribeMap;
    }

    /**
     * 自动查找启动广告的
     * “跳过”的控件
     */
    private void findButtonByText(AccessibilityNodeInfo nodeInfo, final AutoFinder autoFinder) {
        for (int n = 0; n < autoFinder.keywordList.size(); n++) {
            final List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(autoFinder.keywordList.get(n));
            if (!list.isEmpty()) {
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (autoFinder.clickOnly) {
                            for (AccessibilityNodeInfo e : list) {
                                if (e.refresh()) {
                                    Rect rect = new Rect();
                                    e.getBoundsInScreen(rect);
                                    click(rect.centerX(), rect.centerY(), 0, 20);
                                }
                            }
                        } else {
                            for (AccessibilityNodeInfo e : list) {
                                if (e.refresh()) {
                                    if (!e.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                        if (!e.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                            Rect rect = new Rect();
                                            e.getBoundsInScreen(rect);
                                            click(rect.centerX(), rect.centerY(), 0, 20);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, autoFinder.clickDelay, TimeUnit.MILLISECONDS);
                if (++autoRetrieveNumber >= autoFinder.retrieveNumber) {
                    onOffAutoFinder = false;
                    if (!onOffWidgetSub) {
                        serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                        service.setServiceInfo(serviceInfo);
                    }
                }
            }
        }
    }

    /**
     * 查找并点击由
     * Widget
     * 定义的控件
     */
    private void findButtonByWidget(AccessibilityNodeInfo root, Set<Widget> widgetSet) {
        int a = 0;
        int b = 1;
        ArrayList<AccessibilityNodeInfo> listA = new ArrayList<>();
        ArrayList<AccessibilityNodeInfo> listB = new ArrayList<>();
        listA.add(root);
        while (a < b) {
            final AccessibilityNodeInfo node = listA.get(a++);
            if (node != null) {
                final Rect temRect = new Rect();
                node.getBoundsInScreen(temRect);
                CharSequence cId = node.getViewIdResourceName();
                CharSequence cDescribe = node.getContentDescription();
                CharSequence cText = node.getText();
                for (Widget e : widgetSet) {
                    boolean isFind = false;
                    if (temRect.equals(e.widgetRect)) {
                        isFind = true;
                    } else if (cId != null && !e.widgetId.isEmpty() && cId.toString().equals(e.widgetId)) {
                        isFind = true;
                    } else if (cDescribe != null && !e.widgetDescribe.isEmpty() && cDescribe.toString().contains(e.widgetDescribe)) {
                        isFind = true;
                    } else if (cText != null && !e.widgetText.isEmpty() && cText.toString().contains(e.widgetText)) {
                        isFind = true;
                    }
                    if (isFind) {
                        if (!e.noRepeat || !alreadyClickSet.contains(e)) {
                            alreadyClickSet.add(e);
                            executorService.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    if (node.refresh()) {
                                        if (e.clickOnly) {
                                            click(temRect.centerX(), temRect.centerY(), 0, 20);
                                        } else {
                                            if (!node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                                if (!node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                                    click(temRect.centerX(), temRect.centerY(), 0, 20);
                                                }
                                            }
                                        }
                                    }
                                }
                            }, e.clickDelay, TimeUnit.MILLISECONDS);
                            if (widgetAllNoRepeat && alreadyClickSet.size() >= widgetSet.size()) {
                                onOffWidget = false;
                                onOffWidgetSub = false;
                                if (!onOffAutoFinder) {
                                    serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                                    service.setServiceInfo(serviceInfo);
                                }
                            }
                        }
                        break;
                    }
                }
                for (int n = 0; n < node.getChildCount(); n++) {
                    listB.add(node.getChild(n));
                }
            }
            if (a == b) {
                a = 0;
                b = listB.size();
                listA = listB;
                listB = new ArrayList<>();
            }
        }
    }

    /**
     * 查找所有
     * 的控件
     */
    private void findAllNode(List<AccessibilityNodeInfo> roots, List<AccessibilityNodeInfo> list) {
        ArrayList<AccessibilityNodeInfo> tem = new ArrayList<>();
        for (AccessibilityNodeInfo e : roots) {
            if (e == null) continue;
            Rect rect = new Rect();
            e.getBoundsInScreen(rect);
            if (rect.width() <= 0 || rect.height() <= 0) continue;
            list.add(e);
            for (int n = 0; n < e.getChildCount(); n++) {
                tem.add(e.getChild(n));
            }
        }
        if (!tem.isEmpty()) {
            findAllNode(tem, list);
        }
    }

    /**
     * 模拟
     * 点击
     */
    private boolean click(int X, int Y, long startTime, long duration) {
        Path path = new Path();
        path.moveTo(X, Y);
        GestureDescription.Builder builder = new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, startTime, duration));
        return service.dispatchGesture(builder.build(), null, null);
    }

    /**
     * android 7.0 以上
     * 避免无障碍服务冲突
     */
    public void closeService() {
        service.disableSelf();
    }

    /**
     * 开启无障碍服务时调用
     * 获取运行时需要的数据
     */
    private void getRunningData() {
        Set<String> pkgNormalSet = new HashSet<>();
        Set<String> pkgOffSet = new HashSet<>();
        Set<String> pkgOnSet = new HashSet<>();
        //所有已安装的应用
        Set<String> pkgInstalledSet = packageManager
                .getInstalledPackages(PackageManager.GET_META_DATA)
                .stream()
                .map(e -> e.packageName)
                .collect(Collectors.toSet());
        pkgNormalSet.addAll(pkgInstalledSet);
        //输入法、桌面、本应用需要默认关闭
        Set<String> pkgInputMethodSet = inputMethodManager
                .getInputMethodList()
                .stream()
                .map(InputMethodInfo::getPackageName)
                .collect(Collectors.toSet());
        Set<String> pkgHasHomeSet = packageManager
                .queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_ALL)
                .stream()
                .map(e -> e.activityInfo.packageName)
                .collect(Collectors.toSet());
        pkgOffSet.addAll(pkgInputMethodSet);
        pkgOffSet.addAll(pkgHasHomeSet);
        pkgOffSet.add(service.getPackageName());
        //MIUI系统自带的广告服务app，需要开启
        pkgOnSet.add("com.miui.systemAdSolution");

        List<AppDescribe> appDescribeList = new ArrayList<>();
        List<AutoFinder> autoFinderList = new ArrayList<>();
        for (String e : pkgNormalSet) {
            try {
                ApplicationInfo info = packageManager.getApplicationInfo(e, PackageManager.GET_META_DATA);
                AppDescribe appDescribe = new AppDescribe();
                appDescribe.appName = packageManager.getApplicationLabel(info).toString();
                appDescribe.appPackage = info.packageName;
                if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM || pkgOffSet.contains(info.packageName)) {
                    appDescribe.onOff = false;
                    appDescribe.autoFinderOnOFF = false;
                    appDescribe.coordinateOnOff = false;
                    appDescribe.widgetOnOff = false;
                }
                if (pkgOnSet.contains(info.packageName)) {
                    appDescribe.onOff = true;
                    appDescribe.autoFinderOnOFF = true;
                    appDescribe.coordinateOnOff = true;
                    appDescribe.widgetOnOff = true;
                }
                appDescribeList.add(appDescribe);
                AutoFinder autoFinder = new AutoFinder();
                autoFinder.appPackage = info.packageName;
                autoFinder.keywordList = Collections.singletonList("跳过");
                autoFinderList.add(autoFinder);
            } catch (PackageManager.NameNotFoundException exception) {
                // exception.printStackTrace();
            }
        }
        dataDao.insertAppDescribe(appDescribeList);
        dataDao.insertAutoFinder(autoFinderList);
        for (String e : pkgNormalSet) {
            AppDescribe appDescribeTmp = dataDao.getAppDescribeByPackage(e);
            appDescribeTmp.getOtherFieldsFromDatabase(dataDao);
            appDescribeMap.put(e, appDescribeTmp);
        }
    }

    /**
     * 创建规则时调用
     */
    @SuppressLint("ClickableViewAccessibility")
    public void showAddDataWindow() {
        if (pkgSuggestNotOnList == null) {
            Set<String> pkgSysSet = packageManager
                    .getInstalledPackages(PackageManager.MATCH_SYSTEM_ONLY)
                    .stream().map(e -> e.packageName)
                    .collect(Collectors.toSet());
            Set<String> pkgInputMethodSet = inputMethodManager
                    .getInputMethodList()
                    .stream()
                    .map(InputMethodInfo::getPackageName)
                    .collect(Collectors.toSet());
            Set<String> pkgHasHomeSet = packageManager
                    .queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_ALL)
                    .stream()
                    .map(e -> e.activityInfo.packageName)
                    .collect(Collectors.toSet());
            pkgSuggestNotOnList = new HashSet<>();
            pkgSuggestNotOnList.addAll(pkgSysSet);
            pkgSuggestNotOnList.addAll(pkgInputMethodSet);
            pkgSuggestNotOnList.addAll(pkgHasHomeSet);
        }
        if (viewClickPosition != null || addDataBinding != null || widgetSelectBinding != null) {
            return;
        }
        final Widget widgetSelect = new Widget();
        final Coordinate coordinateSelect = new Coordinate();
        final LayoutInflater inflater = LayoutInflater.from(service);

        addDataBinding = ViewAddDataBinding.inflate(inflater);
        widgetSelectBinding = ViewWidgetSelectBinding.inflate(inflater);

        viewClickPosition = new ImageView(service);
        viewClickPosition.setImageResource(R.drawable.p);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        int width = Math.min(metrics.heightPixels, metrics.widthPixels);
        int height = Math.max(metrics.heightPixels, metrics.widthPixels);

        aParams = new WindowManager.LayoutParams();
        aParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        aParams.format = PixelFormat.TRANSPARENT;
        aParams.gravity = Gravity.START | Gravity.TOP;
        aParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        aParams.width = width;
        aParams.height = height / 5;
        aParams.x = (metrics.widthPixels - aParams.width) / 2;
        aParams.y = metrics.heightPixels - aParams.height;
        aParams.alpha = 0.9f;

        bParams = new WindowManager.LayoutParams();
        bParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        bParams.format = PixelFormat.TRANSPARENT;
        bParams.gravity = Gravity.START | Gravity.TOP;
        bParams.width = metrics.widthPixels;
        bParams.height = metrics.heightPixels;
        bParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        bParams.alpha = 0f;

        cParams = new WindowManager.LayoutParams();
        cParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        cParams.format = PixelFormat.TRANSPARENT;
        cParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        cParams.gravity = Gravity.START | Gravity.TOP;
        cParams.width = cParams.height = width / 4;
        cParams.x = (metrics.widthPixels - cParams.width) / 2;
        cParams.y = (metrics.heightPixels - cParams.height) / 2;
        cParams.alpha = 0f;

        addDataBinding.getRoot().setOnTouchListener(new View.OnTouchListener() {
            int startRowX = 0, startRowY = 0, startLpX = 0, startLpY = 0;
            ScheduledFuture<?> future = executorService.schedule(new Runnable() {
                @Override
                public void run() {
                }
            }, 0, TimeUnit.MILLISECONDS);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRowX = Math.round(event.getRawX());
                        startRowY = Math.round(event.getRawY());
                        startLpX = aParams.x;
                        startLpY = aParams.y;
                        future = executorService.schedule(new Runnable() {
                            @Override
                            public void run() {
                                if (Math.abs(aParams.x - startLpX) < 10 && Math.abs(aParams.y - startLpY) < 10) {
                                    Matcher matcher = Pattern.compile("(\\w|\\.)+").matcher(addDataBinding.pacName.getText().toString());
                                    if (matcher.find()) {
                                        if (appDescribeMap.containsKey(matcher.group())) {
                                            Intent intent = new Intent(service, EditDataActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.putExtra("packageName", matcher.group());
                                            service.startActivity(intent);
                                        }
                                    }
                                }
                            }
                        }, 800, TimeUnit.MILLISECONDS);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        aParams.x = startLpX + (Math.round(event.getRawX()) - startRowX);
                        aParams.y = startLpY + (Math.round(event.getRawY()) - startRowY);
                        windowManager.updateViewLayout(addDataBinding.getRoot(), aParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        DisplayMetrics metrics = new DisplayMetrics();
                        windowManager.getDefaultDisplay().getRealMetrics(metrics);
                        aParams.x = Math.max(aParams.x, 0);
                        aParams.x = Math.min(aParams.x, metrics.widthPixels - aParams.width);
                        aParams.y = Math.max(aParams.y, 0);
                        aParams.y = Math.min(aParams.y, metrics.heightPixels - aParams.height);
                        windowManager.updateViewLayout(addDataBinding.getRoot(), aParams);
                        future.cancel(false);
                        break;
                }
                return true;
            }
        });
        viewClickPosition.setOnTouchListener(new View.OnTouchListener() {
            final int width = cParams.width / 2;
            final int height = cParams.height / 2;
            int startRowX = 0, startRowY = 0, startLpX = 0, startLpY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        addDataBinding.saveAim.setEnabled(appDescribeMap.containsKey(currentPackage));
                        cParams.alpha = 0.9f;
                        windowManager.updateViewLayout(viewClickPosition, cParams);
                        startRowX = Math.round(event.getRawX());
                        startRowY = Math.round(event.getRawY());
                        startLpX = cParams.x;
                        startLpY = cParams.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        cParams.x = startLpX + (Math.round(event.getRawX()) - startRowX);
                        cParams.y = startLpY + (Math.round(event.getRawY()) - startRowY);
                        windowManager.updateViewLayout(viewClickPosition, cParams);
                        coordinateSelect.appPackage = currentPackage;
                        coordinateSelect.appActivity = currentActivity;
                        coordinateSelect.xPosition = cParams.x + width;
                        coordinateSelect.yPosition = cParams.y + height;
                        addDataBinding.pacName.setText(coordinateSelect.appPackage);
                        addDataBinding.actName.setText(coordinateSelect.appActivity);
                        addDataBinding.xy.setText("X轴：" + String.format("%-4d", coordinateSelect.xPosition) + "    " + "Y轴：" + String.format("%-4d", coordinateSelect.yPosition));
                        break;
                    case MotionEvent.ACTION_UP:
                        cParams.alpha = 0.5f;
                        windowManager.updateViewLayout(viewClickPosition, cParams);
                        break;
                }
                return true;
            }
        });
        addDataBinding.switchWid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (bParams.alpha == 0) {
                    AccessibilityNodeInfo root = service.getRootInActiveWindow();
                    if (root == null) return;
                    widgetSelect.appPackage = currentPackage;
                    widgetSelect.appActivity = currentActivity;
                    widgetSelectBinding.frame.removeAllViews();
                    ArrayList<AccessibilityNodeInfo> roots = new ArrayList<>();
                    roots.add(root);
                    ArrayList<AccessibilityNodeInfo> nodeList = new ArrayList<>();
                    findAllNode(roots, nodeList);
                    nodeList.sort(new Comparator<AccessibilityNodeInfo>() {
                        @Override
                        public int compare(AccessibilityNodeInfo a, AccessibilityNodeInfo b) {
                            Rect rectA = new Rect();
                            Rect rectB = new Rect();
                            a.getBoundsInScreen(rectA);
                            b.getBoundsInScreen(rectB);
                            return rectB.width() * rectB.height() - rectA.width() * rectA.height();
                        }
                    });
                    for (final AccessibilityNodeInfo e : nodeList) {
                        final Rect temRect = new Rect();
                        e.getBoundsInScreen(temRect);
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(temRect.width(), temRect.height());
                        params.leftMargin = temRect.left;
                        params.topMargin = temRect.top;
                        final ImageView img = new ImageView(service);
                        img.setBackgroundResource(R.drawable.node);
                        img.setFocusableInTouchMode(true);
                        img.setFocusable(true);
                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                v.requestFocus();
                            }
                        });
                        img.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (hasFocus) {
                                    widgetSelect.widgetRect = temRect;
                                    widgetSelect.widgetClickable = e.isClickable();
                                    CharSequence cId = e.getViewIdResourceName();
                                    widgetSelect.widgetId = cId == null ? "" : cId.toString();
                                    CharSequence cDesc = e.getContentDescription();
                                    widgetSelect.widgetDescribe = cDesc == null ? "" : cDesc.toString();
                                    CharSequence cText = e.getText();
                                    widgetSelect.widgetText = cText == null ? "" : cText.toString();
                                    addDataBinding.saveWid.setEnabled(appDescribeMap.containsKey(currentPackage));
                                    addDataBinding.pacName.setText(widgetSelect.appPackage);
                                    addDataBinding.actName.setText(widgetSelect.appActivity);
                                    String click = e.isClickable() ? "true" : "false";
                                    String bonus = temRect.toShortString();
                                    String id = cId == null || !cId.toString().contains(":id/") ? "" : cId.toString().substring(cId.toString().indexOf(":id/") + 4);
                                    String desc = cDesc == null ? "" : cDesc.toString();
                                    String text = cText == null ? "" : cText.toString();
                                    addDataBinding.widget.setText("click:" + click + " " + "bonus:" + bonus + (id.isEmpty() ? "" : " " + "id:" + id) + (desc.isEmpty() ? "" : " " + "desc:" + desc) + (text.isEmpty() ? "" : " " + "text:" + text));
                                    v.setBackgroundResource(R.drawable.node_focus);
                                } else {
                                    v.setBackgroundResource(R.drawable.node);
                                }
                            }
                        });
                        widgetSelectBinding.frame.addView(img, params);
                    }
                    bParams.alpha = 0.5f;
                    bParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    windowManager.updateViewLayout(widgetSelectBinding.getRoot(), bParams);
                    addDataBinding.pacName.setText(widgetSelect.appPackage);
                    addDataBinding.actName.setText(widgetSelect.appActivity);
                    button.setText("隐藏布局");
                } else {
                    bParams.alpha = 0f;
                    bParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    windowManager.updateViewLayout(widgetSelectBinding.getRoot(), bParams);
                    addDataBinding.saveWid.setEnabled(false);
                    button.setText("显示布局");
                }
            }
        });
        addDataBinding.switchAim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (cParams.alpha == 0) {
                    coordinateSelect.appPackage = currentPackage;
                    coordinateSelect.appActivity = currentActivity;
                    cParams.alpha = 0.5f;
                    cParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    windowManager.updateViewLayout(viewClickPosition, cParams);
                    addDataBinding.pacName.setText(coordinateSelect.appPackage);
                    addDataBinding.actName.setText(coordinateSelect.appActivity);
                    button.setText("隐藏准心");
                } else {
                    cParams.alpha = 0f;
                    cParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    windowManager.updateViewLayout(viewClickPosition, cParams);
                    addDataBinding.saveAim.setEnabled(false);
                    button.setText("显示准心");
                }
            }
        });
        addDataBinding.saveWid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        AppDescribe temAppDescribe = appDescribeMap.get(widgetSelect.appPackage);
                        if (temAppDescribe != null) {
                            Widget temWidget = new Widget(widgetSelect);
                            temWidget.createTime = System.currentTimeMillis();
                            dataDao.insertWidget(temWidget);
                            addDataBinding.saveWid.setEnabled(false);
                            addDataBinding.pacName.setText(widgetSelect.appPackage + " (以下控件数据已保存)");
                            temAppDescribe.getWidgetSetMapFromDatabase(dataDao);
                        }
                    }
                };
                if (pkgSuggestNotOnList.contains(widgetSelect.appPackage)) {
                    String prePackage = currentPackage;
                    String preActivity = currentActivity;
                    View view = ViewAddWarningBinding.inflate(inflater).getRoot();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(service);
                    alertDialogBuilder.setView(view);
                    alertDialogBuilder.setNegativeButton("取消", null);
                    alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            runnable.run();
                        }
                    });
                    alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            currentPackage = prePackage;
                            currentActivity = preActivity;
                        }
                    });
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                    dialog.show();
                } else {
                    runnable.run();
                }
            }
        });
        addDataBinding.saveAim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        AppDescribe temAppDescribe = appDescribeMap.get(coordinateSelect.appPackage);
                        if (temAppDescribe != null) {
                            Coordinate temCoordinate = new Coordinate(coordinateSelect);
                            temCoordinate.createTime = System.currentTimeMillis();
                            dataDao.insertCoordinate(temCoordinate);
                            addDataBinding.saveAim.setEnabled(false);
                            addDataBinding.pacName.setText(coordinateSelect.appPackage + " (以下坐标数据已保存)");
                            temAppDescribe.getCoordinateMapFromDatabase(dataDao);
                        }
                    }
                };
                if (pkgSuggestNotOnList.contains(coordinateSelect.appPackage)) {
                    String prePackage = currentPackage;
                    String preActivity = currentActivity;
                    View view = ViewAddWarningBinding.inflate(inflater).getRoot();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(service);
                    alertDialogBuilder.setView(view);
                    alertDialogBuilder.setNegativeButton("取消", null);
                    alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            runnable.run();
                        }
                    });
                    alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            currentPackage = prePackage;
                            currentActivity = preActivity;
                        }
                    });
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                    dialog.show();
                } else {
                    runnable.run();
                }
            }
        });
        addDataBinding.quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeViewImmediate(widgetSelectBinding.getRoot());
                windowManager.removeViewImmediate(addDataBinding.getRoot());
                windowManager.removeViewImmediate(viewClickPosition);
                pkgSuggestNotOnList = null;
                widgetSelectBinding = null;
                addDataBinding = null;
                viewClickPosition = null;
                aParams = null;
                bParams = null;
                cParams = null;
            }
        });
        windowManager.addView(widgetSelectBinding.getRoot(), bParams);
        windowManager.addView(addDataBinding.getRoot(), aParams);
        windowManager.addView(viewClickPosition, cParams);
    }

    public void keepAliveByNotification(boolean enable) {
        if (enable) {
            NotificationManager notificationManager = service.getSystemService(NotificationManager.class);
            Intent intent = new Intent(service, MainActivity.class);
            Notification.Builder builder = new Notification.Builder(service);
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            builder.setSmallIcon(R.drawable.app);
            builder.setContentTitle(service.getText(R.string.app_name));
            builder.setContentIntent(PendingIntent.getActivity(service, 0x01, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(service.getPackageName());
                NotificationChannel channel = new NotificationChannel(service.getPackageName(), service.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            service.startForeground(0x01, builder.build());
        } else {
            service.stopForeground(true);
        }
    }

    public void keepAliveByFloatingWindow(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
            lp.gravity = Gravity.START | Gravity.TOP;
            lp.format = PixelFormat.TRANSPARENT;
            lp.alpha = 0;
            lp.width = 0;
            lp.height = 0;
            lp.x = 0;
            lp.y = 0;
            ignoreView = new View(service);
            windowManager.addView(ignoreView, lp);
        } else if (ignoreView != null) {
            windowManager.removeView(ignoreView);
            ignoreView = null;
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
                currentPackageSub = isScreenOffPre;
            }
            if (TextUtils.equals(intent.getAction(), ACTION_SHOW_ADD_DATA_WINDOW)) {
                showAddDataWindow();
            }
        }
    }

    public class MyPackageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {
                String dataString = intent.getDataString();
                String packageName = dataString != null ? dataString.substring(8) : null;
                if (!TextUtils.isEmpty(packageName)) {
                    executorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            AppDescribe appDescribe = dataDao.getAppDescribeByPackage(packageName);
                            if (appDescribe == null) {
                                appDescribe = new AppDescribe();
                                try {
                                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                                    appDescribe.appName = packageManager.getApplicationLabel(applicationInfo).toString();
                                } catch (PackageManager.NameNotFoundException e) {
                                    appDescribe.appName = "unknown";
                                    // e.printStackTrace();
                                }
                                appDescribe.appPackage = packageName;
                                List<ResolveInfo> homeLaunchList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_ALL);
                                for (ResolveInfo e : homeLaunchList) {
                                    if (packageName.equals(e.activityInfo.packageName)) {
                                        appDescribe.onOff = false;
                                        appDescribe.autoFinderOnOFF = false;
                                        appDescribe.widgetOnOff = false;
                                        appDescribe.coordinateOnOff = false;
                                        break;
                                    }
                                }
                                List<InputMethodInfo> inputMethodInfoList = inputMethodManager.getInputMethodList();
                                for (InputMethodInfo e : inputMethodInfoList) {
                                    if (packageName.equals(e.getPackageName())) {
                                        appDescribe.onOff = false;
                                        appDescribe.autoFinderOnOFF = false;
                                        appDescribe.widgetOnOff = false;
                                        appDescribe.coordinateOnOff = false;
                                        break;
                                    }
                                }
                                AutoFinder autoFinder = new AutoFinder();
                                autoFinder.appPackage = packageName;
                                autoFinder.keywordList = Collections.singletonList("跳过");
                                dataDao.insertAppDescribe(appDescribe);
                                dataDao.insertAutoFinder(autoFinder);
                                appDescribe.getOtherFieldsFromDatabase(dataDao);
                                appDescribeMap.put(appDescribe.appPackage, appDescribe);
                            }
                        }
                    }, 2000, TimeUnit.MILLISECONDS);
                }
            }
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
                String dataString = intent.getDataString();
                String packageName = dataString != null ? dataString.substring(8) : null;
                if (!TextUtils.isEmpty(packageName)) {
                    appDescribeMap.remove(packageName);
                }
            }
        }
    }
}
