package com.lgh.tapclick.myactivity;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.lgh.tapclick.BuildConfig;
import com.lgh.tapclick.databinding.ActivityEditDataBinding;
import com.lgh.tapclick.databinding.ViewBaseSettingBinding;
import com.lgh.tapclick.databinding.ViewCoordinateBinding;
import com.lgh.tapclick.databinding.ViewEditFileNameBinding;
import com.lgh.tapclick.databinding.ViewOnOffWarningBinding;
import com.lgh.tapclick.databinding.ViewWidgetBinding;
import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.mybean.BasicContent;
import com.lgh.tapclick.mybean.Coordinate;
import com.lgh.tapclick.mybean.CoordinateShare;
import com.lgh.tapclick.mybean.MyAppConfig;
import com.lgh.tapclick.mybean.Widget;
import com.lgh.tapclick.mybean.WidgetShare;
import com.lgh.tapclick.myclass.DataDao;
import com.lgh.tapclick.myclass.MyApplication;
import com.lgh.tapclick.myfunction.MyUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;

public class EditDataActivity extends BaseActivity {
    private AppDescribe appDescribe;
    private Context context;
    private LayoutInflater inflater;
    private DataDao dataDao;
    private MyAppConfig myAppConfig;
    private DisplayMetrics metrics;
    private SimpleDateFormat dateFormatModify;
    private SimpleDateFormat dateFormat;
    private ActivityEditDataBinding editDataBinding;
    private ViewBaseSettingBinding baseSettingBinding;
    private Set<String> pkgSuggestNotOnList;
    private String packageName;
    private Gson gson;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = getLayoutInflater();
        editDataBinding = ActivityEditDataBinding.inflate(inflater);
        setContentView(editDataBinding.getRoot());

        context = getApplicationContext();
        dataDao = MyApplication.dataDao;
        myAppConfig = dataDao.getMyAppConfig();
        gson = new GsonBuilder().create();
        dateFormatModify = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        Set<String> pkgSysSet = getPackageManager().
                getInstalledPackages(PackageManager.MATCH_SYSTEM_ONLY)
                .stream().map(e -> e.packageName)
                .collect(Collectors.toSet());
        Set<String> pkgInputMethodSet = getSystemService(InputMethodManager.class)
                .getInputMethodList()
                .stream()
                .map(InputMethodInfo::getPackageName)
                .collect(Collectors.toSet());
        Set<String> pkgHasHomeSet = getPackageManager()
                .queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_ALL)
                .stream()
                .map(e -> e.activityInfo.packageName)
                .collect(Collectors.toSet());
        pkgSuggestNotOnList = new HashSet<>();
        pkgSuggestNotOnList.addAll(pkgSysSet);
        pkgSuggestNotOnList.addAll(pkgInputMethodSet);
        pkgSuggestNotOnList.addAll(pkgHasHomeSet);

        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        editDataBinding.rootLayout.setLayoutTransition(transition);
        editDataBinding.baseSettingLayout.setLayoutTransition(transition);
        editDataBinding.coordinateLayout.setLayoutTransition(transition);
        editDataBinding.widgetLayout.setLayoutTransition(transition);

        editDataBinding.scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_MOVE) {
                    editDataBinding.getRoot().requestFocus();
                }
                return false;
            }
        });

        if (myAppConfig.autoHideOnTaskList) {
            MyUtils.setExcludeFromRecents(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        String extraPackage = getIntent().getStringExtra("packageName");
        if (!TextUtils.isEmpty(extraPackage)) {
            packageName = extraPackage;
        }
        if (!TextUtils.isEmpty(packageName)) {
            AppDescribe appDescribeTemp = dataDao.getAppDescribeByPackage(packageName);
            if (appDescribeTemp != null) {
                appDescribeTemp.getOtherFieldsFromDatabase(dataDao);
                appDescribe = appDescribeTemp;
            }
        }
        if (appDescribe == null) {
            finishAndRemoveTask();
            return;
        }

        if (baseSettingBinding != null) {
            editDataBinding.baseSettingLayout.removeView(baseSettingBinding.getRoot());
        }
        baseSettingBinding = ViewBaseSettingBinding.inflate(inflater);
        baseSettingBinding.appName.setText(StrUtil.blankToDefault(appDescribe.appName, "读取失败，无权限或未安装"));
        baseSettingBinding.appPackage.setText(appDescribe.appPackage);

        baseSettingBinding.coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        baseSettingBinding.coordinateSustainTime.setText(appDescribe.coordinateRetrieveAllTime ? "∞" : String.valueOf(appDescribe.coordinateRetrieveTime));
        baseSettingBinding.coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        baseSettingBinding.widgetSwitch.setChecked(appDescribe.widgetOnOff);
        baseSettingBinding.widgetSustainTime.setText(appDescribe.widgetRetrieveAllTime ? "∞" : String.valueOf(appDescribe.widgetRetrieveTime));
        baseSettingBinding.widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        Runnable baseSettingSaveRun = new Runnable() {
            @Override
            public void run() {
                String coordinateTime = StrUtil.trimToEmpty(baseSettingBinding.coordinateSustainTime.getText());
                String widgetTime = StrUtil.trimToEmpty(baseSettingBinding.widgetSustainTime.getText());
                editDataBinding.baseSettingModify.setTextColor(0xfff20000);
                if (coordinateTime.isEmpty()) {
                    editDataBinding.baseSettingModify.setText("坐标检索持续时间不能为空");
                    return;
                }
                if (widgetTime.isEmpty()) {
                    editDataBinding.baseSettingModify.setText("控件检索持续时间不能为空");
                    return;
                }
                appDescribe.coordinateOnOff = baseSettingBinding.coordinateSwitch.isChecked();
                appDescribe.coordinateRetrieveTime = coordinateTime.equals("∞") ? appDescribe.coordinateRetrieveTime : Integer.parseInt(coordinateTime);
                appDescribe.coordinateRetrieveAllTime = baseSettingBinding.coordinateRetrieveAllTime.isChecked();
                appDescribe.widgetOnOff = baseSettingBinding.widgetSwitch.isChecked();
                appDescribe.widgetRetrieveTime = widgetTime.equals("∞") ? appDescribe.widgetRetrieveTime : Integer.parseInt(widgetTime);
                appDescribe.widgetRetrieveAllTime = baseSettingBinding.widgetRetrieveAllTime.isChecked();
                dataDao.updateAppDescribe(appDescribe);
                editDataBinding.baseSettingModify.setTextColor(0xff000000);
                editDataBinding.baseSettingModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
            }
        };

        View.OnClickListener onOffClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchCompat switchCompat = (SwitchCompat) v;
                if (switchCompat.isChecked() && pkgSuggestNotOnList.contains(appDescribe.appPackage)) {
                    switchCompat.setChecked(false);
                    View view = ViewOnOffWarningBinding.inflate(getLayoutInflater()).getRoot();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditDataActivity.this);
                    alertDialogBuilder.setView(view);
                    alertDialogBuilder.setNegativeButton("取消", null);
                    alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            baseSettingSaveRun.run();
                        }
                    });
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.show();
                } else {
                    baseSettingSaveRun.run();
                }
            }
        };
        baseSettingBinding.widgetSwitch.setOnClickListener(onOffClickListener);
        baseSettingBinding.coordinateSwitch.setOnClickListener(onOffClickListener);

        TextWatcher sustainTimeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                baseSettingSaveRun.run();
            }
        };
        baseSettingBinding.widgetSustainTime.addTextChangedListener(sustainTimeTextWatcher);
        baseSettingBinding.coordinateSustainTime.addTextChangedListener(sustainTimeTextWatcher);

        View.OnClickListener allTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseSettingSaveRun.run();
                baseSettingBinding.coordinateSustainTime.setText(appDescribe.coordinateRetrieveAllTime ? "∞" : String.valueOf(appDescribe.coordinateRetrieveTime));
                baseSettingBinding.widgetSustainTime.setText(appDescribe.widgetRetrieveAllTime ? "∞" : String.valueOf(appDescribe.widgetRetrieveTime));
            }
        };
        baseSettingBinding.widgetRetrieveAllTime.setOnClickListener(allTimeClickListener);
        baseSettingBinding.coordinateRetrieveAllTime.setOnClickListener(allTimeClickListener);
        editDataBinding.baseSettingLayout.addView(baseSettingBinding.getRoot());

        List<Coordinate> latestTriggerCoordinateList = appDescribe.coordinateList.stream()
                .filter(e -> e.lastTriggerTime > System.currentTimeMillis() - 1000 * 60 * 5)
                .sorted((e1, e2) -> Long.compare(e2.lastTriggerTime, e1.lastTriggerTime))
                .collect(Collectors.toList());
        appDescribe.coordinateList.removeIf(e -> latestTriggerCoordinateList.stream().anyMatch(n -> n == e));
        appDescribe.coordinateList.sort((e1, e2) -> Long.compare(e2.createTime, e1.createTime));
        appDescribe.coordinateList.addAll(0, latestTriggerCoordinateList);
        if (appDescribe.coordinateList.isEmpty()) {
            editDataBinding.coordinateLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.coordinateLayout.setVisibility(View.VISIBLE);
        }
        if (editDataBinding.coordinateLayout.getChildCount() > 2) {
            editDataBinding.coordinateLayout.removeViews(2, editDataBinding.coordinateLayout.getChildCount() - 2);
        }
        for (int n = 0; n < appDescribe.coordinateList.size(); n++) {
            Coordinate coordinate = appDescribe.coordinateList.get(n);
            ViewCoordinateBinding coordinateBinding = ViewCoordinateBinding.inflate(inflater);
            coordinateBinding.coordinateActivity.setText(coordinate.appActivity);
            coordinateBinding.coordinateXPosition.setText(String.valueOf(coordinate.xPosition));
            coordinateBinding.coordinateYPosition.setText(String.valueOf(coordinate.yPosition));
            coordinateBinding.coordinateClickDelay.setText(String.valueOf(coordinate.clickDelay));
            coordinateBinding.coordinateClickInterval.setText(String.valueOf(coordinate.clickInterval));
            coordinateBinding.coordinateClickNumber.setText(String.valueOf(coordinate.clickNumber));
            coordinateBinding.coordinateTriggerCount.setText(String.valueOf(coordinate.triggerCount));
            coordinateBinding.coordinateComment.setText(coordinate.comment);
            long day1 = (System.currentTimeMillis() - coordinate.createTime) / (1000 * 60 * 60 * 24);
            long day2 = (System.currentTimeMillis() - coordinate.lastTriggerTime) / (1000 * 60 * 60 * 24);
            coordinateBinding.coordinateCreateTime.setText(String.format("%s (%s天前)", dateFormat.format(new Date(coordinate.createTime)), day1));
            coordinateBinding.coordinateLastTriggerTime.setTextColor(day1 >= 60 && day2 >= 60 ? Color.RED : coordinateBinding.coordinateLastTriggerTime.getCurrentTextColor());
            if (coordinate.lastTriggerTime <= 0) {
                coordinateBinding.coordinateLastTriggerTime.setText("无触发记录");
            } else {
                coordinateBinding.coordinateLastTriggerTime.setText(String.format("%s (%s天前)", dateFormat.format(coordinate.lastTriggerTime), day2));
            }
            if (n < latestTriggerCoordinateList.size()) {
                coordinateBinding.coordinateModify.setTextColor(0xff00c507);
                if (n == 0) {
                    coordinateBinding.coordinateModify.setText("该坐标为最新触发坐标");
                } else {
                    coordinateBinding.coordinateModify.setText("该坐标最近5分钟内有被触发");
                }
            }
            Runnable coordinateSaveRun = new Runnable() {
                @Override
                public void run() {
                    String sX = StrUtil.trimToEmpty(coordinateBinding.coordinateXPosition.getText());
                    String sY = StrUtil.trimToEmpty(coordinateBinding.coordinateYPosition.getText());
                    String sDelay = StrUtil.trimToEmpty(coordinateBinding.coordinateClickDelay.getText());
                    String sInterval = StrUtil.trimToEmpty(coordinateBinding.coordinateClickInterval.getText());
                    String sNumber = StrUtil.trimToEmpty(coordinateBinding.coordinateClickNumber.getText());
                    coordinateBinding.coordinateModify.setTextColor(0xfff20000);
                    if (sX.isEmpty()) {
                        coordinateBinding.coordinateModify.setText("X轴坐标不能为空");
                        return;
                    }
                    if (Integer.parseInt(sX) > metrics.widthPixels) {
                        coordinateBinding.coordinateModify.setText("X轴坐标超出屏幕寸");
                        return;
                    }
                    if (sY.isEmpty()) {
                        coordinateBinding.coordinateModify.setText("Y轴坐标不能为空");
                        return;
                    }
                    if (Integer.parseInt(sY) > metrics.heightPixels) {
                        coordinateBinding.coordinateModify.setText("Y轴坐标超出屏幕寸");
                        return;
                    }
                    if (sDelay.isEmpty()) {
                        coordinateBinding.coordinateModify.setText("延迟点击不能为空");
                        return;
                    }
                    if (sInterval.isEmpty()) {
                        coordinateBinding.coordinateModify.setText("点击间隔不能为空");
                        return;
                    }
                    if (sNumber.isEmpty()) {
                        coordinateBinding.coordinateModify.setText("点击次数不能为空");
                        return;
                    }
                    coordinate.xPosition = Integer.parseInt(sX);
                    coordinate.yPosition = Integer.parseInt(sY);
                    coordinate.clickDelay = Integer.parseInt(sDelay);
                    coordinate.clickInterval = Integer.parseInt(sInterval);
                    coordinate.clickNumber = Integer.parseInt(sNumber);
                    coordinate.comment = StrUtil.trimToEmpty(coordinateBinding.coordinateComment.getText());
                    dataDao.updateCoordinate(coordinate);
                    coordinateBinding.coordinateModify.setTextColor(0xff000000);
                    coordinateBinding.coordinateModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
                }
            };

            TextWatcher coordinateTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    coordinateSaveRun.run();
                }
            };

            coordinateBinding.coordinateXPosition.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateYPosition.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateClickDelay.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateClickInterval.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateClickNumber.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateComment.addTextChangedListener(coordinateTextWatcher);

            coordinateBinding.coordinateShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CoordinateShare coordinateShare = new CoordinateShare();
                    coordinateShare.coordinate = coordinate;
                    coordinateShare.basicContent = new BasicContent();
                    coordinateShare.basicContent.fingerPrint = Build.FINGERPRINT;
                    coordinateShare.basicContent.displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(coordinateShare.basicContent.displayMetrics);
                    coordinateShare.basicContent.packageName = coordinate.appPackage;
                    try {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(coordinate.appPackage, PackageManager.GET_META_DATA);
                        coordinateShare.basicContent.versionCode = packageInfo.versionCode;
                        coordinateShare.basicContent.versionName = packageInfo.versionName;
                    } catch (PackageManager.NameNotFoundException ex) {
                        // ex.printStackTrace();
                    }
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String strRule = '"' + CoordinateShare.class.getSimpleName() + '"' + ": " + gson.toJson(coordinateShare);
                    showEditShareFileNameDialog(strRule);
                }
            });

            coordinateBinding.coordinateDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(EditDataActivity.this)
                            .setTitle("确定删除？")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dataDao.deleteCoordinate(coordinate);
                                    appDescribe.coordinateList.remove(coordinate);
                                    editDataBinding.coordinateLayout.removeView(coordinateBinding.getRoot());
                                    if (appDescribe.coordinateList.isEmpty()) {
                                        editDataBinding.coordinateLayout.setVisibility(View.GONE);
                                        baseSettingBinding.coordinateSwitch.setChecked(false);
                                        appDescribe.coordinateOnOff = false;
                                        if (appDescribe.widgetList.isEmpty()) {
                                            baseSettingBinding.widgetSwitch.setChecked(false);
                                            appDescribe.widgetOnOff = false;
                                        }
                                        dataDao.updateAppDescribe(appDescribe);
                                    }
                                }
                            }).create().show();
                }
            });
            editDataBinding.coordinateLayout.addView(coordinateBinding.getRoot());
        }

        List<Widget> latestTriggerWidgetList = appDescribe.widgetList.stream()
                .filter(e -> e.lastTriggerTime > System.currentTimeMillis() - 1000 * 60 * 5)
                .sorted((e1, e2) -> Long.compare(e2.lastTriggerTime, e1.lastTriggerTime))
                .collect(Collectors.toList());
        appDescribe.widgetList.removeIf(e -> latestTriggerWidgetList.stream().anyMatch(n -> n == e));
        appDescribe.widgetList.sort((e1, e2) -> Long.compare(e2.createTime, e1.createTime));
        appDescribe.widgetList.addAll(0, latestTriggerWidgetList);
        if (appDescribe.widgetList.isEmpty()) {
            editDataBinding.widgetLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.widgetLayout.setVisibility(View.VISIBLE);
        }
        if (editDataBinding.widgetLayout.getChildCount() > 2) {
            editDataBinding.widgetLayout.removeViews(2, editDataBinding.widgetLayout.getChildCount() - 2);
        }
        for (int n = 0; n < appDescribe.widgetList.size(); n++) {
            Widget widget = appDescribe.widgetList.get(n);
            ViewWidgetBinding widgetBinding = ViewWidgetBinding.inflate(inflater);
            widgetBinding.widgetActivity.setText(widget.appActivity);
            widgetBinding.widgetClickable.setText(String.valueOf(widget.widgetClickable));
            widgetBinding.widgetRect.setText(widget.widgetRect != null ? gson.toJson(widget.widgetRect) : null);
            widgetBinding.widgetNodeId.setText(widget.widgetNodeId != null ? String.valueOf(widget.widgetNodeId) : null);
            widgetBinding.widgetViewId.setText(widget.widgetViewId);
            widgetBinding.widgetDescribe.setText(widget.widgetDescribe);
            widgetBinding.widgetText.setText(widget.widgetText);
            widgetBinding.widgetClickDelay.setText(String.valueOf(widget.clickDelay));
            widgetBinding.widgetDebounceDelay.setText(String.valueOf(widget.debounceDelay));
            widgetBinding.widgetNoRepeat.setChecked(widget.noRepeat);
            widgetBinding.widgetClickOnly.setChecked(widget.clickOnly);
            widgetBinding.widgetComment.setText(widget.comment);
            widgetBinding.widgetClickNumber.setText(String.valueOf(widget.clickNumber));
            widgetBinding.widgetClickInterval.setText(String.valueOf(widget.clickInterval));
            widgetBinding.widgetTriggerCount.setText(String.valueOf(widget.triggerCount));
            widgetBinding.widgetActionClick.setChecked(widget.action == Widget.ACTION_CLICK);
            widgetBinding.widgetActionBack.setChecked(widget.action == Widget.ACTION_BACK);
            widgetBinding.widgetActionClick.setEnabled(widget.action != Widget.ACTION_CLICK);
            widgetBinding.widgetActionBack.setEnabled(widget.action != Widget.ACTION_BACK);
            widgetBinding.llClickProp.setVisibility(widget.action == Widget.ACTION_CLICK ? View.VISIBLE : View.GONE);
            widgetBinding.widgetConditionOr.setChecked(widget.condition == Widget.CONDITION_OR);
            widgetBinding.widgetConditionAnd.setChecked(widget.condition == Widget.CONDITION_AND);
            widgetBinding.widgetConditionOr.setEnabled(widget.condition != Widget.CONDITION_OR);
            widgetBinding.widgetConditionAnd.setEnabled(widget.condition != Widget.CONDITION_AND);
            widgetBinding.widgetTriggerReason.setText(StrUtil.blankToDefault(widget.triggerReason, "无触发记录"));
            long day1 = (System.currentTimeMillis() - widget.createTime) / (1000 * 60 * 60 * 24);
            long day2 = (System.currentTimeMillis() - widget.lastTriggerTime) / (1000 * 60 * 60 * 24);
            widgetBinding.widgetCreateTime.setText(String.format("%s (%s天前)", dateFormat.format(new Date(widget.createTime)), day1));
            widgetBinding.widgetLastTriggerTime.setTextColor(day1 >= 60 && day2 >= 60 ? Color.RED : widgetBinding.widgetLastTriggerTime.getCurrentTextColor());
            if (widget.lastTriggerTime <= 0) {
                widgetBinding.widgetLastTriggerTime.setText("无触发记录");
            } else {
                widgetBinding.widgetLastTriggerTime.setText(String.format("%s (%s天前)", dateFormat.format(widget.lastTriggerTime), day2));
            }
            if (n < latestTriggerWidgetList.size()) {
                widgetBinding.widgetModify.setTextColor(0xff00c507);
                if (n == 0) {
                    widgetBinding.widgetModify.setText("该控件为最新触发控件");
                } else {
                    widgetBinding.widgetModify.setText("该控件最近5分钟内有被触发");
                }
            }
            Runnable widgetSaveRun = new Runnable() {
                @Override
                public void run() {
                    String clickNumber = StrUtil.trimToEmpty(widgetBinding.widgetClickNumber.getText());
                    String clickInterval = StrUtil.trimToEmpty(widgetBinding.widgetClickInterval.getText());
                    String clickDelay = StrUtil.trimToEmpty(widgetBinding.widgetClickDelay.getText());
                    String debounceDelay = StrUtil.trimToEmpty(widgetBinding.widgetDebounceDelay.getText());
                    widgetBinding.widgetModify.setTextColor(0xfff20000);
                    if (clickNumber.isEmpty()) {
                        widgetBinding.widgetModify.setText("点击次数不能为空");
                        return;
                    }
                    if (clickInterval.isEmpty()) {
                        widgetBinding.widgetModify.setText("点击间隔不能为空");
                        return;
                    }
                    if (clickDelay.isEmpty()) {
                        widgetBinding.widgetModify.setText("最小触发间隔不能为空");
                        return;
                    }
                    if (debounceDelay.isEmpty()) {
                        widgetBinding.widgetModify.setText("防抖延迟不能为空");
                        return;
                    }
                    try {
                        widget.widgetRect = gson.fromJson(StrUtil.trimToEmpty(widgetBinding.widgetRect.getText()), Rect.class);
                    } catch (JsonSyntaxException jsonSyntaxException) {
                        widgetBinding.widgetModify.setText("Bonus格式错误");
                        return;
                    }
                    try {
                        widget.widgetNodeId = Long.valueOf(StrUtil.trimToEmpty(widgetBinding.widgetNodeId.getText()));
                    } catch (NumberFormatException numberFormatException) {
                        widget.widgetNodeId = null;
                    }
                    widget.widgetViewId = StrUtil.toStringOrEmpty(widgetBinding.widgetViewId.getText());
                    widget.widgetDescribe = StrUtil.toStringOrEmpty(widgetBinding.widgetDescribe.getText());
                    widget.widgetText = StrUtil.toStringOrEmpty(widgetBinding.widgetText.getText());
                    widget.comment = StrUtil.trimToEmpty(widgetBinding.widgetComment.getText());
                    widget.clickNumber = Integer.parseInt(clickNumber);
                    widget.clickInterval = Integer.parseInt(clickInterval);
                    widget.clickDelay = Integer.parseInt(clickDelay);
                    widget.debounceDelay = Integer.parseInt(debounceDelay);
                    widget.noRepeat = widgetBinding.widgetNoRepeat.isChecked();
                    widget.clickOnly = widgetBinding.widgetClickOnly.isChecked();
                    dataDao.updateWidget(widget);
                    widgetBinding.widgetModify.setTextColor(0xff000000);
                    widgetBinding.widgetModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
                }
            };

            TextWatcher widgetTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    widgetSaveRun.run();
                }
            };

            widgetBinding.widgetRect.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetNodeId.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetViewId.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetDescribe.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetText.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetClickNumber.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetClickInterval.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetClickDelay.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetDebounceDelay.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetComment.addTextChangedListener(widgetTextWatcher);

            View.OnClickListener widgetClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == widgetBinding.widgetActionClick || v == widgetBinding.widgetActionBack) {
                        widget.action = Integer.parseInt((String) v.getTag());
                        widgetBinding.widgetActionClick.setChecked(widget.action == Widget.ACTION_CLICK);
                        widgetBinding.widgetActionBack.setChecked(widget.action == Widget.ACTION_BACK);
                        widgetBinding.widgetActionClick.setEnabled(widget.action != Widget.ACTION_CLICK);
                        widgetBinding.widgetActionBack.setEnabled(widget.action != Widget.ACTION_BACK);
                        widgetBinding.llClickProp.setVisibility(widget.action == Widget.ACTION_CLICK ? View.VISIBLE : View.GONE);
                    }
                    if (v == widgetBinding.widgetConditionOr || v == widgetBinding.widgetConditionAnd) {
                        widget.condition = Integer.parseInt((String) v.getTag());
                        widgetBinding.widgetConditionOr.setChecked(widget.condition == Widget.CONDITION_OR);
                        widgetBinding.widgetConditionAnd.setChecked(widget.condition == Widget.CONDITION_AND);
                        widgetBinding.widgetConditionOr.setEnabled(widget.condition != Widget.CONDITION_OR);
                        widgetBinding.widgetConditionAnd.setEnabled(widget.condition != Widget.CONDITION_AND);
                    }
                    widgetSaveRun.run();
                }
            };
            widgetBinding.widgetNoRepeat.setOnClickListener(widgetClickListener);
            widgetBinding.widgetClickOnly.setOnClickListener(widgetClickListener);
            widgetBinding.widgetActionClick.setOnClickListener(widgetClickListener);
            widgetBinding.widgetActionBack.setOnClickListener(widgetClickListener);
            widgetBinding.widgetConditionOr.setOnClickListener(widgetClickListener);
            widgetBinding.widgetConditionAnd.setOnClickListener(widgetClickListener);

            widgetBinding.widgetShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WidgetShare widgetShare = new WidgetShare();
                    widgetShare.widget = widget;
                    widgetShare.basicContent = new BasicContent();
                    widgetShare.basicContent.fingerPrint = Build.FINGERPRINT;
                    widgetShare.basicContent.displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(widgetShare.basicContent.displayMetrics);
                    widgetShare.basicContent.packageName = widget.appPackage;
                    try {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(widget.appPackage, PackageManager.GET_META_DATA);
                        widgetShare.basicContent.versionCode = packageInfo.versionCode;
                        widgetShare.basicContent.versionName = packageInfo.versionName;
                    } catch (PackageManager.NameNotFoundException ex) {
                        // ex.printStackTrace();
                    }
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String strRule = '"' + WidgetShare.class.getSimpleName() + '"' + ": " + gson.toJson(widgetShare);
                    showEditShareFileNameDialog(strRule);
                }
            });

            widgetBinding.widgetDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(EditDataActivity.this)
                            .setTitle("确定删除？")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dataDao.deleteWidget(widget);
                                    appDescribe.widgetList.remove(widget);
                                    editDataBinding.widgetLayout.removeView(widgetBinding.getRoot());
                                    if (appDescribe.widgetList.isEmpty()) {
                                        editDataBinding.widgetLayout.setVisibility(View.GONE);
                                        baseSettingBinding.widgetSwitch.setChecked(false);
                                        appDescribe.widgetOnOff = false;
                                        if (appDescribe.coordinateList.isEmpty()) {
                                            baseSettingBinding.coordinateSwitch.setChecked(false);
                                            appDescribe.coordinateOnOff = false;
                                        }
                                        dataDao.updateAppDescribe(appDescribe);
                                    }
                                }
                            }).create().show();
                }
            });
            editDataBinding.widgetLayout.addView(widgetBinding.getRoot());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyUtils.requestUpdateAppDescribe(appDescribe.appPackage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myAppConfig.autoHideOnTaskList) {
            MyUtils.setExcludeFromRecents(true);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra("packageName", packageName);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void showEditShareFileNameDialog(String strRegulation) {
        ViewEditFileNameBinding binding = ViewEditFileNameBinding.inflate(inflater);
        binding.fileName.setHint(DigestUtil.md5Hex(strRegulation));
        new AlertDialog.Builder(EditDataActivity.this)
                .setView(binding.getRoot())
                .setCancelable(false)
                .setTitle("编辑文件名称")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            FileUtils.cleanDirectory(getCacheDir());
                            String fileName = StrUtil.trimToEmpty(binding.fileName.getText());
                            File file = new File(getCacheDir(), (fileName.isEmpty() ? binding.fileName.getHint() : fileName) + ".txt");
                            FileUtils.writeStringToFile(file, strRegulation, StandardCharsets.UTF_8);
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                            intent.setDataAndType(uri, getContentResolver().getType(uri));
                            intent.putExtra(Intent.EXTRA_TEXT, strRegulation);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.setClipData(new ClipData(ClipData.newUri(getContentResolver(), "regulation", uri)));
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "分享"));
                        } catch (IOException ex) {
                            Toast.makeText(context, "生成分享文件时发生错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).create().show();
    }
}
