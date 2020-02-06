package com.lgh.advertising.going;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.AutoFinder;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.MyDatabase;
import com.lgh.advertising.myclass.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainFunction {

    public static final String TAG = "MainFunction";
    public static Handler handler;
    public static Map<String, AppDescribe> appDescribeMap;
    private AppDescribe appDescribe;
    private AccessibilityService service;
    private String currentPackage;
    private String currentActivity;
    private boolean is_state_change_a, is_state_change_b, is_state_change_c;

    private int autoRetrieveNumber;
    private AccessibilityServiceInfo serviceInfo;
    private ScheduledFuture future_a, future_b, future_c;
    private ScheduledExecutorService executorService;

    private View adv_view, layout_win;
    private ImageView target_xy;
    private LayoutInflater inflater;
    private WindowManager.LayoutParams aParams, bParams, cParams;
    private DisplayMetrics metrics;
    private WindowManager windowManager;

    public MainFunction(AccessibilityService service) {
        this.service = service;
    }

    protected void onServiceConnected() {
        try {
            currentPackage = "Initialize CurrentPackage";
            currentActivity = "Initialize CurrentActivity";
            executorService = Executors.newSingleThreadScheduledExecutor();
            serviceInfo = service.getServiceInfo();
            appDescribeMap = new HashMap<>();
            updatePackage();
            future_a = future_b = future_c = executorService.schedule(new Runnable() {
                @Override
                public void run() {
                }
            }, 0, TimeUnit.MILLISECONDS);
            handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    switch (msg.what) {
                        case 0x00:
                            showAddAdvertisingFloat();
                            break;
                    }
                    return true;
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, AccessibilityEvent.eventTypeToString(event.getEventType()) + "-" + event.getPackageName() + "-" + event.getClassName());
        try {
            switch (event.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    CharSequence temPac = event.getPackageName();
                    CharSequence temClass = event.getClassName();
                    if (temPac != null && temClass != null) {
                        String pacName = temPac.toString();
                        String actName = temClass.toString();
                        boolean isActivity = !actName.startsWith("android.widget.") && !actName.startsWith("android.view.") && !actName.startsWith("android.app.");
                        if (!pacName.equals(currentPackage) && isActivity) {
                            appDescribe = appDescribeMap.get(pacName);
                            if (appDescribe != null) {
                                currentPackage = pacName;
                                if (appDescribe.on_off) {
                                    future_a.cancel(false);
                                    future_b.cancel(false);
                                    future_c.cancel(false);
                                    serviceInfo.eventTypes |= AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                                    service.setServiceInfo(serviceInfo);
                                    is_state_change_a = appDescribe.coordinateOnOff;
                                    is_state_change_b = appDescribe.widgetOnOff;
                                    is_state_change_c = appDescribe.autoFinderOnOFF;
                                    autoRetrieveNumber = 0;

                                    if (is_state_change_a && !appDescribe.autoFinderRetrieveAllTime) {
                                        future_a = executorService.schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                is_state_change_a = false;

                                            }
                                        }, appDescribe.coordinateRetrieveTime, TimeUnit.MILLISECONDS);
                                    }

                                    if (is_state_change_b && !appDescribe.coordinateRetrieveAllTime) {
                                        future_b = executorService.schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                is_state_change_b = false;
                                            }
                                        }, appDescribe.widgetRetrieveTime, TimeUnit.MILLISECONDS);
                                    }
                                    if (is_state_change_c && !appDescribe.widgetRetrieveAllTime) {
                                        future_c = executorService.schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                is_state_change_c = false;
                                            }
                                        }, appDescribe.autoFinderRetrieveTime, TimeUnit.MILLISECONDS);
                                    }
                                    if (!(appDescribe.autoFinderRetrieveAllTime || appDescribe.coordinateRetrieveAllTime || appDescribe.widgetRetrieveAllTime)) {
                                        executorService.schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                                                service.setServiceInfo(serviceInfo);
                                            }
                                        }, appDescribe.autoFinderRetrieveTime > appDescribe.coordinateRetrieveTime ?
                                                appDescribe.autoFinderRetrieveTime > appDescribe.widgetRetrieveTime ? appDescribe.autoFinderRetrieveTime : appDescribe.widgetRetrieveTime :
                                                appDescribe.coordinateRetrieveTime > appDescribe.widgetRetrieveTime ? appDescribe.coordinateRetrieveTime : appDescribe.widgetRetrieveTime, TimeUnit.MILLISECONDS);
                                    }
                                } else {
                                    if (is_state_change_a || is_state_change_b || is_state_change_c) {

                                        serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
                                        service.setServiceInfo(serviceInfo);
                                        is_state_change_a = false;
                                        is_state_change_b = false;
                                        is_state_change_c = false;
                                        future_a.cancel(false);
                                        future_b.cancel(false);
                                        future_c.cancel(false);
                                    }
                                }
                            }
                        }
                        if (isActivity) {
                            currentActivity = actName;
                            if (is_state_change_a && appDescribe != null) {
                                final Coordinate coordinate = appDescribe.coordinateMap.get(currentActivity);
                                if (coordinate != null) {
                                    executorService.scheduleAtFixedRate(new Runnable() {
                                        int num = 0;

                                        @Override
                                        public void run() {
                                            if (num < coordinate.clickNumber && currentActivity.equals(coordinate.appActivity)) {
                                                click(coordinate.xPosition, coordinate.yPosition, 0, 20);
                                                num++;
                                            } else {
                                                throw new RuntimeException();
                                            }
                                        }
                                    }, coordinate.clickDelay, coordinate.clickInterval, TimeUnit.MILLISECONDS);
                                }
                            }
                        }
                        if (!pacName.equals(currentPackage)) {
                            break;
                        }
                        if (is_state_change_b && appDescribe != null) {
                            Set<Widget> widgetSet = appDescribe.widgetSetMap.get(actName);
                            if (widgetSet != null) {
                                findSkipButtonByWidget(service.getRootInActiveWindow(), widgetSet);
                            }
                        }
                        if (is_state_change_c && appDescribe != null) {
                            AutoFinder autoFinder = appDescribe.autoFinder;
                            findSkipButtonByText(service.getRootInActiveWindow(), autoFinder);
                        }
                    }
                    break;
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                    if (event.getPackageName().equals("com.android.systemui")) {
                        break;
                    }
                    if (is_state_change_b && appDescribe != null) {
                        Set<Widget> widgetSet = appDescribe.widgetSetMap.get(currentActivity);
                        if (widgetSet != null) {
                            findSkipButtonByWidget(service.getRootInActiveWindow(), widgetSet);
                        }
                    }
                    if (is_state_change_c && appDescribe != null) {
                        AutoFinder autoFinder = appDescribe.autoFinder;
                        findSkipButtonByText(service.getRootInActiveWindow(), autoFinder);
                    }
                    break;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {

    }

    public boolean onUnbind(Intent intent) {
        return false;
    }

    /**
     * 自动查找启动广告的
     * “跳过”的控件
     */
    private void findSkipButtonByText(AccessibilityNodeInfo nodeInfo, final AutoFinder autoFinder) {
        if (nodeInfo == null) return;
        for (int n = 0; n < autoFinder.keywordList.size(); n++) {
            final List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(autoFinder.keywordList.get(n));
            if (!list.isEmpty()) {
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (autoFinder.clickOnly) {
                            for (AccessibilityNodeInfo e : list) {
                                Rect rect = new Rect();
                                e.getBoundsInScreen(rect);
                                click(rect.centerX(), rect.centerY(), 0, 20);
                                e.recycle();
                            }
                        } else {
                            for (AccessibilityNodeInfo e : list) {
                                if (!e.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                    if (!e.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                        Rect rect = new Rect();
                                        e.getBoundsInScreen(rect);
                                        click(rect.centerX(), rect.centerY(), 0, 20);
                                    }
                                }
                                e.recycle();
                            }
                        }
                    }
                }, autoFinder.clickDelay, TimeUnit.MILLISECONDS);
                if (++autoRetrieveNumber >= appDescribe.autoFinder.retrieveNumber) {
                    is_state_change_c = false;
                }
                return;
            }

        }
        nodeInfo.recycle();
    }

    /**
     * 查找并点击由
     * WidgetButtonDescribe
     * 定义的控件
     */
    private void findSkipButtonByWidget(AccessibilityNodeInfo root, Set<Widget> set) {
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
                for (final Widget e : set) {
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
                        executorService.schedule(new Runnable() {
                            @Override
                            public void run() {
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
                        }, e.clickDelay, TimeUnit.MILLISECONDS);
                        return;
                    }
                }
                for (int n = 0; n < node.getChildCount(); n++) {
                    listB.add(node.getChild(n));
                }
                node.recycle();
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
        ArrayList<AccessibilityNodeInfo> temList = new ArrayList<>();
        for (AccessibilityNodeInfo e : roots) {
            if (e == null) continue;
            list.add(e);
            for (int n = 0; n < e.getChildCount(); n++) {
                temList.add(e.getChild(n));
            }
        }
        if (!temList.isEmpty()) {
            findAllNode(temList, list);
        }
    }

    /**
     * 模拟
     * 点击
     */
    private boolean click(int X, int Y, long start_time, long duration) {
        Path path = new Path();
        path.moveTo(X, Y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, start_time, duration));
            return service.dispatchGesture(builder.build(), null, null);
        } else {
            return false;
        }
    }


    /**
     * 在安装卸载软件时触发调用，
     * 更新相关包名的集合
     */
    private void updatePackage() {

        DataDao dataDao = DataDaoFactory.getInstance(service);
        PackageManager packageManager = service.getPackageManager();
        Set<String> packageCommon = new HashSet<>();
        Set<String> packageSystem = new HashSet<>();
        Set<String> packageHome = new HashSet<>();
        Set<String> packageRemove = new HashSet<>();
        List<ResolveInfo> ResolveInfoList;
        Intent intent;
        intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo e : ResolveInfoList) {
            packageHome.add(e.activityInfo.packageName);
        }
        List<InputMethodInfo> inputMethodInfoList = ((InputMethodManager) service.getSystemService(AccessibilityService.INPUT_METHOD_SERVICE)).getInputMethodList();
        for (InputMethodInfo e : inputMethodInfoList) {
            packageRemove.add(e.getPackageName());
        }
        packageRemove.add(service.getPackageName());
        packageRemove.add("com.android.systemui");
        packageSystem.removeAll(packageRemove);
        packageSystem.addAll(packageHome);
        packageSystem.add("com.android.packageinstaller");
        packageCommon.removeAll(packageSystem);
        packageCommon.removeAll(packageRemove);
        List<AppDescribe> appDescribeList = new ArrayList<>();
        List<AutoFinder> autoFinderList = new ArrayList<>();
        intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        ResolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo e : ResolveInfoList) {
            if (!packageRemove.contains(e.activityInfo.packageName)) {
                AppDescribe appDescribe = new AppDescribe();
                appDescribe.appName = packageManager.getApplicationLabel(e.activityInfo.applicationInfo).toString();
                appDescribe.appPackage = e.activityInfo.packageName;
                if ((e.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM || packageHome.contains(e.activityInfo.packageName)) {
                    appDescribe.on_off = false;
                }
                appDescribeList.add(appDescribe);
                AutoFinder autoFinder = new AutoFinder();
                autoFinder.appPackage = e.activityInfo.packageName;
                autoFinder.keywordList = Arrays.asList("跳过");
                autoFinderList.add(autoFinder);
            }
        }
        dataDao.insertAppDescribe(appDescribeList);
        dataDao.insertAutoFinder(autoFinderList);
        appDescribeList = dataDao.getAppDescribes();
        for (AppDescribe e : appDescribeList) {
            e.autoFinder = dataDao.getAutoFinder(e.appPackage);
            e.coordinateMap = Maps.uniqueIndex(dataDao.getCoordinates(e.appPackage), new Function<Coordinate, String>() {
                @Override
                public String apply(Coordinate input) {
                    return input.appActivity;
                }
            });
            List<Widget> widgetList = dataDao.getWidgets(e.appPackage);
            e.widgetSetMap = new HashMap<>();
            for (Widget w : widgetList) {
                Set<Widget> widgetSet = e.widgetSetMap.get(w.appActivity);
                if (widgetSet == null) {
                    widgetSet = new HashSet<>();
                    widgetSet.add(w);
                    e.widgetSetMap.put(w.appActivity, widgetSet);
                } else {
                    widgetSet.add(w);
                }
            }
            appDescribeMap.put(e.appPackage, e);
        }
    }

    void showAddAdvertisingFloat() {
        if (target_xy != null || adv_view != null || layout_win != null) {
            return;
        }
        windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        final Widget widgetSelect = new Widget();
        final Coordinate coordinateSelect  = new Coordinate();
        inflater = LayoutInflater.from(service);
        adv_view = inflater.inflate(R.layout.advertise_desc, null);
        final TextView pacName = adv_view.findViewById(R.id.pacName);
        final TextView actName = adv_view.findViewById(R.id.actName);
        final TextView widget = adv_view.findViewById(R.id.widget);
        final TextView xyP = adv_view.findViewById(R.id.xy);
        Button switchWid = adv_view.findViewById(R.id.switch_wid);
        final Button saveWidgetButton = adv_view.findViewById(R.id.save_wid);
        Button switchAim = adv_view.findViewById(R.id.switch_aim);
        final Button savePositionButton = adv_view.findViewById(R.id.save_aim);
        Button quitButton = adv_view.findViewById(R.id.quit);

        layout_win = inflater.inflate(R.layout.accessibilitynode_desc, null);
        final FrameLayout layout_add = layout_win.findViewById(R.id.frame);

        target_xy = new ImageView(service);
        target_xy.setImageResource(R.drawable.p);
        metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        aParams = new WindowManager.LayoutParams();
        aParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        aParams.format = PixelFormat.TRANSPARENT;
        aParams.gravity = Gravity.START | Gravity.TOP;
        aParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        aParams.width = width;
        aParams.height = height / 5;
        aParams.x = (metrics.widthPixels - aParams.width) / 2;
        aParams.y = metrics.heightPixels - aParams.height;
        aParams.alpha = 0.8f;

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

        adv_view.setOnTouchListener(new View.OnTouchListener() {
            int x = 0, y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        aParams.x = Math.round(aParams.x + (event.getRawX() - x));
                        aParams.y = Math.round(aParams.y + (event.getRawY() - y));
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        windowManager.updateViewLayout(adv_view, aParams);
                        break;
                }
                return true;
            }
        });
        target_xy.setOnTouchListener(new View.OnTouchListener() {
            int x = 0, y = 0, width = cParams.width / 2, height = cParams.height / 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        savePositionButton.setEnabled(true);
                        cParams.alpha = 0.9f;
                        windowManager.updateViewLayout(target_xy, cParams);
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        cParams.x = Math.round(cParams.x + (event.getRawX() - x));
                        cParams.y = Math.round(cParams.y + (event.getRawY() - y));
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        windowManager.updateViewLayout(target_xy, cParams);
                        coordinateSelect.appPackage = currentPackage;
                        coordinateSelect.appActivity = currentActivity;
                        coordinateSelect.xPosition = cParams.x + width;
                        coordinateSelect.yPosition = cParams.y + height;
                        pacName.setText(coordinateSelect.appPackage);
                        actName.setText(coordinateSelect.appActivity);
                        xyP.setText("X轴：" + coordinateSelect.xPosition + "    " + "Y轴：" + coordinateSelect.yPosition + "    " + "(其他参数默认)");
                        break;
                    case MotionEvent.ACTION_UP:
                        cParams.alpha = 0.5f;
                        windowManager.updateViewLayout(target_xy, cParams);
                        break;
                }
                return true;
            }
        });
        switchWid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (bParams.alpha == 0) {
                    AccessibilityNodeInfo root = service.getRootInActiveWindow();
                    if (root == null) return;
                    widgetSelect.appPackage = currentPackage;
                    widgetSelect.appActivity = currentActivity;
                    layout_add.removeAllViews();
                    ArrayList<AccessibilityNodeInfo> roots = new ArrayList<>();
                    roots.add(root);
                    ArrayList<AccessibilityNodeInfo> nodeList = new ArrayList<>();
                    findAllNode(roots, nodeList);
                    Collections.sort(nodeList, new Comparator<AccessibilityNodeInfo>() {
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
                                    widgetSelect.widgetClass = e.getClassName().toString();
                                    CharSequence cId = e.getViewIdResourceName();
                                    widgetSelect.widgetId = cId == null ? "" : cId.toString();
                                    CharSequence cDesc = e.getContentDescription();
                                    widgetSelect.widgetDescribe = cDesc == null ? "" : cDesc.toString();
                                    CharSequence cText = e.getText();
                                    widgetSelect.widgetText = cText == null ? "" : cText.toString();
                                    saveWidgetButton.setEnabled(true);
                                    pacName.setText(widgetSelect.appPackage);
                                    actName.setText(widgetSelect.appActivity);
                                    widget.setText("click:" + (e.isClickable() ? "true" : "false") + " " + "bonus:" + temRect.toShortString() + " " + "id:" + (cId == null ? "null" : cId.toString().substring(cId.toString().indexOf("id/") + 3)) + " " + "desc:" + (cDesc == null ? "null" : cDesc.toString()) + " " + "text:" + (cText == null ? "null" : cText.toString()));
                                    v.setBackgroundResource(R.drawable.node_focus);
                                } else {
                                    v.setBackgroundResource(R.drawable.node);
                                }
                            }
                        });
                        layout_add.addView(img, params);
                    }
                    bParams.alpha = 0.5f;
                    bParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    windowManager.updateViewLayout(layout_win, bParams);
                    pacName.setText(widgetSelect.appPackage);
                    actName.setText(widgetSelect.appActivity);
                    button.setText("隐藏布局");
                } else {
                    bParams.alpha = 0f;
                    bParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    windowManager.updateViewLayout(layout_win, bParams);
                    saveWidgetButton.setEnabled(false);
                    button.setText("显示布局");
                }
            }
        });
        switchAim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (cParams.alpha == 0) {
                    coordinateSelect.appPackage = currentPackage;
                    coordinateSelect.appActivity = currentActivity;
                    cParams.alpha = 0.5f;
                    cParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    windowManager.updateViewLayout(target_xy, cParams);
                    pacName.setText(coordinateSelect.appPackage);
                    actName.setText(coordinateSelect.appActivity);
                    button.setText("隐藏准心");
                } else {
                    cParams.alpha = 0f;
                    cParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    windowManager.updateViewLayout(target_xy, cParams);
                    savePositionButton.setEnabled(false);
                    button.setText("显示准心");
                }
            }
        });
        saveWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Widget temWidget = new Widget(widgetSelect);
                AppDescribe temAppDescribe = appDescribeMap.get(temWidget.appPackage);
                if (temAppDescribe != null){
                    Set<Widget> temWidgetSet = temAppDescribe.widgetSetMap.get(temWidget.appActivity);
                    if (temWidgetSet == null){
                        temWidgetSet = new HashSet<>();
                        temWidgetSet.add(temWidget);
                        temAppDescribe.widgetSetMap.put(temWidget.appActivity,temWidgetSet);
                    } else {
                        temWidgetSet.add(temWidget);
                    }
                }
                DataDaoFactory.getInstance(service).insertWidget(temWidget);
                saveWidgetButton.setEnabled(false);
                pacName.setText(widgetSelect.appPackage + " (以下控件数据已保存)");
            }
        });
        savePositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Coordinate temCoordinate = new Coordinate(coordinateSelect);
                AppDescribe temAppDescribe = appDescribeMap.get(temCoordinate.appPackage);
                if (temAppDescribe != null){
                    temAppDescribe.coordinateMap.put(temCoordinate.appActivity,temCoordinate);
                }
                DataDaoFactory.getInstance(service).insertCoordinate(temCoordinate);
                savePositionButton.setEnabled(false);
                pacName.setText(temCoordinate.appPackage + " (以下坐标数据已保存)");
            }
        });
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeViewImmediate(layout_win);
                windowManager.removeViewImmediate(adv_view);
                windowManager.removeViewImmediate(target_xy);
                layout_win = null;
                adv_view = null;
                target_xy = null;
                aParams = null;
                bParams = null;
                cParams = null;
            }
        });
        windowManager.addView(layout_win, bParams);
        windowManager.addView(adv_view, aParams);
        windowManager.addView(target_xy, cParams);
    }
}
