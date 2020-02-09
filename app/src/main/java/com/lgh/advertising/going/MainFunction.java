package com.lgh.advertising.going;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.os.Handler;
import android.os.Message;
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

import androidx.annotation.NonNull;

import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.AutoFinder;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainFunction {

    public static final String TAG = "MainFunction";
    private Map<String, AppDescribe> appDescribeMap;
    private AppDescribe appDescribe;
    private AccessibilityService service;
    private String currentPackage;
    private String currentActivity;
    private boolean is_state_change_a, is_state_change_b, is_state_change_c;
    private int autoRetrieveNumber;
    private AccessibilityServiceInfo serviceInfo;
    private ScheduledFuture future_a, future_b, future_c;
    private ScheduledExecutorService executorService;
    private MyScreenOffReceiver screenOnReceiver;
    private Set<Widget> widgetSet;

    private WindowManager.LayoutParams aParams, bParams, cParams;
    private View viewAdvertisingMessage, viewLayoutAnalyze;
    private ImageView viewClickPosition;

    public MainFunction(AccessibilityService service){
        this.service = service;
    }

    protected void onServiceConnected() {
        try {
            currentPackage = "Initialize CurrentPackage";
            currentActivity = "Initialize CurrentActivity";
            executorService = Executors.newSingleThreadScheduledExecutor();
            serviceInfo = service.getServiceInfo();
            appDescribeMap = new HashMap<>();
            screenOnReceiver = new MyScreenOffReceiver();
            service.registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
            updatePackage();
            future_a = future_b = future_c = executorService.schedule(new Runnable() {
                @Override
                public void run() {
                }
            }, 0, TimeUnit.MILLISECONDS);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.i(TAG, AccessibilityEvent.eventTypeToString(event.getEventType()) + "-" + event.getPackageName() + "-" + event.getClassName());
        try {
            switch (event.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    CharSequence temPackage = event.getPackageName();
                    CharSequence temClass = event.getClassName();
                    if (temPackage != null && temClass != null) {
                        String packageName = temPackage.toString();
                        String activityName = temClass.toString();
                        boolean isActivity = !activityName.startsWith("android.widget.") && !activityName.startsWith("android.view.") && !activityName.startsWith("android.app.");
                        if (!packageName.equals(currentPackage) && isActivity) {
                            appDescribe = appDescribeMap.get(packageName);
                            if (appDescribe != null) {
                                currentPackage = packageName;
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
                            currentActivity = activityName;
                            if (appDescribe != null) {
                                widgetSet = appDescribe.widgetSetMap.get(activityName);
                                if (is_state_change_a) {
                                    final Coordinate coordinate = appDescribe.coordinateMap.get(activityName);
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
                        }
                        if (!packageName.equals(currentPackage)) {
                            break;
                        }
                        if (is_state_change_b && appDescribe != null  && widgetSet != null) {
                            findSkipButtonByWidget(service.getRootInActiveWindow(), widgetSet);
                        }
                        if (is_state_change_c && appDescribe != null) {
                            findSkipButtonByText(service.getRootInActiveWindow(), appDescribe.autoFinder);
                        }
                    }
                    break;
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                    if (event.getPackageName().equals("com.android.systemui")) {
                        break;
                    }
                    if (is_state_change_b && appDescribe != null && widgetSet != null) {
                        findSkipButtonByWidget(service.getRootInActiveWindow(), widgetSet);
                    }
                    if (is_state_change_c && appDescribe != null) {
                        findSkipButtonByText(service.getRootInActiveWindow(), appDescribe.autoFinder);
                    }
                    break;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (viewAdvertisingMessage != null && viewClickPosition != null && viewLayoutAnalyze != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            aParams.x = (metrics.widthPixels - aParams.width) / 2;
            aParams.y = metrics.heightPixels - aParams.height;
            bParams.width = metrics.widthPixels;
            bParams.height = metrics.heightPixels;
            cParams.x = (metrics.widthPixels - cParams.width) / 2;
            cParams.y = (metrics.heightPixels - cParams.height) / 2;
            windowManager.updateViewLayout(viewAdvertisingMessage, aParams);
            windowManager.updateViewLayout(viewClickPosition, cParams);
            FrameLayout layout = viewLayoutAnalyze.findViewById(R.id.frame);
            layout.removeAllViews();
            TextView text = new TextView(service);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(0xffff0000);
            text.setText("请重新刷新布局");
            windowManager.updateViewLayout(layout,bParams);
            layout.addView(text, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }

    public boolean onUnbind(Intent intent) {
        service.unregisterReceiver(screenOnReceiver);
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

    public void  onScreenOff(){
        currentPackage = "ScreenOff Package";
        currentActivity = "ScreenOff Activity";
    }

    public  void closeService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.disableSelf();
        }
    }

    public Map<String,AppDescribe> getAppDescribeMap(){
        return  appDescribeMap;
    }

    /**
     * 在安装卸载软件时触发调用，
     * 更新相关包名的集合
     */
    private void updatePackage() {

        DataDao dataDao = DataDaoFactory.getInstance(service);
        PackageManager packageManager = service.getPackageManager();
        Set<String> packageInstall = new HashSet<>();
        Set<String> packageOff = new HashSet<>();
        Set<String> packageRemove = new HashSet<>();
        List<ResolveInfo> ResolveInfoList;
        Intent intent;
        intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo e : ResolveInfoList) {
            packageOff.add(e.activityInfo.packageName);
        }
        List<InputMethodInfo> inputMethodInfoList = ((InputMethodManager) service.getSystemService(AccessibilityService.INPUT_METHOD_SERVICE)).getInputMethodList();
        for (InputMethodInfo e : inputMethodInfoList) {
            packageRemove.add(e.getPackageName());
        }
        packageRemove.add("com.android.systemui");
        packageOff.add(service.getPackageName());
        packageOff.add("com.android.packageinstaller");
        List<AppDescribe> appDescribeList = new ArrayList<>();
        List<AutoFinder> autoFinderList = new ArrayList<>();
        intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        ResolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo e : ResolveInfoList) {
            String packageName = e.activityInfo.packageName;
            packageInstall.add(packageName);
            if (!packageRemove.contains(packageName)) {
                AppDescribe appDescribe = new AppDescribe();
                appDescribe.appName = packageManager.getApplicationLabel(e.activityInfo.applicationInfo).toString();
                appDescribe.appPackage = packageName;
                if ((e.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM || packageOff.contains(packageName)) {
                    appDescribe.on_off = false;
                }
                appDescribeList.add(appDescribe);
                AutoFinder autoFinder = new AutoFinder();
                autoFinder.appPackage = packageName;
                autoFinder.keywordList = Arrays.asList("跳过");
                autoFinderList.add(autoFinder);
            }
        }
        dataDao.deleteAppDescribeByNotIn(packageInstall);
        dataDao.insertAppDescribe(appDescribeList);
        dataDao.insertAutoFinder(autoFinderList);
        appDescribeList = dataDao.getAppDescribes();
        for (AppDescribe e : appDescribeList) {
            e.getOtherField(dataDao);
            appDescribeMap.put(e.appPackage, e);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void showAddAdvertisingFloat() {
        if (viewClickPosition != null || viewAdvertisingMessage != null || viewLayoutAnalyze != null) {
            return;
        }
        final WindowManager windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        final DataDao dataDao = DataDaoFactory.getInstance(service);
        final Widget widgetSelect = new Widget();
        final Coordinate coordinateSelect = new Coordinate();
        LayoutInflater inflater = LayoutInflater.from(service);
        viewAdvertisingMessage = inflater.inflate(R.layout.view_advertising, null);
        final TextView pacName = viewAdvertisingMessage.findViewById(R.id.pacName);
        final TextView actName = viewAdvertisingMessage.findViewById(R.id.actName);
        final TextView widget = viewAdvertisingMessage.findViewById(R.id.widget);
        final TextView xyPosition = viewAdvertisingMessage.findViewById(R.id.xy);
        Button switchWid = viewAdvertisingMessage.findViewById(R.id.switch_wid);
        final Button saveWidgetButton = viewAdvertisingMessage.findViewById(R.id.save_wid);
        Button switchAim = viewAdvertisingMessage.findViewById(R.id.switch_aim);
        final Button savePositionButton = viewAdvertisingMessage.findViewById(R.id.save_aim);
        Button quitButton = viewAdvertisingMessage.findViewById(R.id.quit);

        viewLayoutAnalyze = inflater.inflate(R.layout.view_widget_select, null);
        final FrameLayout layoutParent = viewLayoutAnalyze.findViewById(R.id.frame);

        viewClickPosition = new ImageView(service);
        viewClickPosition.setImageResource(R.drawable.p);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        final boolean b = metrics.heightPixels > metrics.widthPixels;
        int width = b ? metrics.widthPixels : metrics.heightPixels;
        int height = b ? metrics.heightPixels : metrics.widthPixels;
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

        viewAdvertisingMessage.setOnTouchListener(new View.OnTouchListener() {
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
                        windowManager.updateViewLayout(viewAdvertisingMessage, aParams);
                        break;
                }
                return true;
            }
        });
        viewClickPosition.setOnTouchListener(new View.OnTouchListener() {
            int x = 0, y = 0, width = cParams.width / 2, height = cParams.height / 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        savePositionButton.setEnabled(true);
                        cParams.alpha = 0.9f;
                        windowManager.updateViewLayout(viewClickPosition, cParams);
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        cParams.x = Math.round(cParams.x + (event.getRawX() - x));
                        cParams.y = Math.round(cParams.y + (event.getRawY() - y));
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        windowManager.updateViewLayout(viewClickPosition, cParams);
                        coordinateSelect.appPackage = currentPackage;
                        coordinateSelect.appActivity = currentActivity;
                        coordinateSelect.xPosition = cParams.x + width;
                        coordinateSelect.yPosition = cParams.y + height;
                        pacName.setText(coordinateSelect.appPackage);
                        actName.setText(coordinateSelect.appActivity);
                        xyPosition.setText("X轴：" + coordinateSelect.xPosition + "    " + "Y轴：" + coordinateSelect.yPosition + "    " + "(其他参数默认)");
                        break;
                    case MotionEvent.ACTION_UP:
                        cParams.alpha = 0.5f;
                        windowManager.updateViewLayout(viewClickPosition, cParams);
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
                    layoutParent.removeAllViews();
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
                        layoutParent.addView(img, params);
                    }
                    bParams.alpha = 0.5f;
                    bParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    windowManager.updateViewLayout(viewLayoutAnalyze, bParams);
                    pacName.setText(widgetSelect.appPackage);
                    actName.setText(widgetSelect.appActivity);
                    button.setText("隐藏布局");
                } else {
                    bParams.alpha = 0f;
                    bParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    windowManager.updateViewLayout(viewLayoutAnalyze, bParams);
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
                    windowManager.updateViewLayout(viewClickPosition, cParams);
                    pacName.setText(coordinateSelect.appPackage);
                    actName.setText(coordinateSelect.appActivity);
                    button.setText("隐藏准心");
                } else {
                    cParams.alpha = 0f;
                    cParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    windowManager.updateViewLayout(viewClickPosition, cParams);
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
                if (temAppDescribe != null) {
                    Set<Widget> temWidgetSet = temAppDescribe.widgetSetMap.get(temWidget.appActivity);
                    if (temWidgetSet == null) {
                        temWidgetSet = new HashSet<>();
                        temWidgetSet.add(temWidget);
                        temAppDescribe.widgetSetMap.put(temWidget.appActivity, temWidgetSet);
                    } else {
                        temWidgetSet.add(temWidget);
                    }
                }
                dataDao.insertWidget(temWidget);
                saveWidgetButton.setEnabled(false);
                pacName.setText(widgetSelect.appPackage + " (以下控件数据已保存)");
            }
        });
        savePositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Coordinate temCoordinate = new Coordinate(coordinateSelect);
                AppDescribe temAppDescribe = appDescribeMap.get(temCoordinate.appPackage);
                if (temAppDescribe != null) {
                    temAppDescribe.coordinateMap.put(temCoordinate.appActivity, temCoordinate);
                }
                dataDao.insertCoordinate(temCoordinate);
                savePositionButton.setEnabled(false);
                pacName.setText(temCoordinate.appPackage + " (以下坐标数据已保存)");
            }
        });
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeViewImmediate(viewLayoutAnalyze);
                windowManager.removeViewImmediate(viewAdvertisingMessage);
                windowManager.removeViewImmediate(viewClickPosition);
                viewLayoutAnalyze = null;
                viewAdvertisingMessage = null;
                viewClickPosition = null;
                aParams = null;
                bParams = null;
                cParams = null;
            }
        });
        windowManager.addView(viewLayoutAnalyze, bParams);
        windowManager.addView(viewAdvertisingMessage, aParams);
        windowManager.addView(viewClickPosition, cParams);
    }
}
