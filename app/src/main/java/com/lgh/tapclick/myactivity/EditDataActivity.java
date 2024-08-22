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
import com.lgh.advertising.tapclick.BuildConfig;
import com.lgh.advertising.tapclick.databinding.ActivityEditDataBinding;
import com.lgh.advertising.tapclick.databinding.ViewBaseSettingBinding;
import com.lgh.advertising.tapclick.databinding.ViewCoordinateBinding;
import com.lgh.advertising.tapclick.databinding.ViewEditFileNameBinding;
import com.lgh.advertising.tapclick.databinding.ViewOnOffWarningBinding;
import com.lgh.advertising.tapclick.databinding.ViewWidgetBinding;
import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.mybean.BasicContent;
import com.lgh.tapclick.mybean.Coordinate;
import com.lgh.tapclick.mybean.CoordinateShare;
import com.lgh.tapclick.mybean.Widget;
import com.lgh.tapclick.mybean.WidgetShare;
import com.lgh.tapclick.myclass.DataDao;
import com.lgh.tapclick.myclass.MyApplication;
import com.lgh.tapclick.myfunction.MyUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class EditDataActivity extends BaseActivity {
    private AppDescribe appDescribe;
    private Context context;
    private LayoutInflater inflater;
    private DataDao dataDao;
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

        dataDao = MyApplication.dataDao;
        context = getApplicationContext();
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

        if (MyApplication.myAppConfig.autoHideOnTaskList) {
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
        baseSettingBinding.onOffName.setText(appDescribe.appName);
        baseSettingBinding.onOffSwitch.setChecked(appDescribe.onOff);

        baseSettingBinding.coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        baseSettingBinding.coordinateSustainTime.setText(appDescribe.coordinateRetrieveAllTime ? "∞" : String.valueOf(appDescribe.coordinateRetrieveTime));
        baseSettingBinding.coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        baseSettingBinding.widgetSwitch.setChecked(appDescribe.widgetOnOff);
        baseSettingBinding.widgetSustainTime.setText(appDescribe.widgetRetrieveAllTime ? "∞" : String.valueOf(appDescribe.widgetRetrieveTime));
        baseSettingBinding.widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        Runnable baseSettingSaveRun = new Runnable() {
            @Override
            public void run() {
                String coordinateTime = baseSettingBinding.coordinateSustainTime.getText().toString();
                String widgetTime = baseSettingBinding.widgetSustainTime.getText().toString();
                editDataBinding.baseSettingModify.setTextColor(0xffff0000);
                if (coordinateTime.isEmpty()) {
                    editDataBinding.baseSettingModify.setText("坐标检索持续时间不能为空");
                    return;
                }
                if (widgetTime.isEmpty()) {
                    editDataBinding.baseSettingModify.setText("控件检索持续时间不能为空");
                    return;
                }
                appDescribe.onOff = baseSettingBinding.onOffSwitch.isChecked();
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
        baseSettingBinding.onOffSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = baseSettingBinding.onOffSwitch.isChecked();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        baseSettingBinding.onOffSwitch.setChecked(isChecked);
                        baseSettingBinding.widgetSwitch.setChecked(isChecked);
                        baseSettingBinding.coordinateSwitch.setChecked(isChecked);
                        baseSettingSaveRun.run();
                    }
                };
                if (isChecked && pkgSuggestNotOnList.contains(appDescribe.appPackage)) {
                    baseSettingBinding.onOffSwitch.setChecked(false);
                    View view = ViewOnOffWarningBinding.inflate(getLayoutInflater()).getRoot();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditDataActivity.this);
                    alertDialogBuilder.setView(view);
                    alertDialogBuilder.setNegativeButton("取消", null);
                    alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            runnable.run();
                        }
                    });
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.show();
                } else {
                    runnable.run();
                }

            }
        });
        View.OnClickListener onOffClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseSettingSaveRun.run();
                if (!appDescribe.onOff && ((SwitchCompat) v).isChecked()) {
                    appDescribe.onOff = true;
                    baseSettingBinding.onOffSwitch.setChecked(true);
                    dataDao.updateAppDescribe(appDescribe);
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

        if (appDescribe.coordinateList.isEmpty()) {
            editDataBinding.coordinateLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.coordinateLayout.setVisibility(View.VISIBLE);
        }
        appDescribe.coordinateList.sort(new Comparator<Coordinate>() {
            @Override
            public int compare(Coordinate o1, Coordinate o2) {
                return Long.compare(o2.createTime, o1.createTime);
            }
        });
        if (editDataBinding.coordinateLayout.getChildCount() > 2) {
            editDataBinding.coordinateLayout.removeViews(2, editDataBinding.coordinateLayout.getChildCount() - 2);
        }
        for (final Coordinate e : appDescribe.coordinateList) {
            final ViewCoordinateBinding coordinateBinding = ViewCoordinateBinding.inflate(inflater);
            coordinateBinding.coordinateActivity.setText(e.appActivity);
            coordinateBinding.coordinateXPosition.setText(String.valueOf(e.xPosition));
            coordinateBinding.coordinateYPosition.setText(String.valueOf(e.yPosition));
            coordinateBinding.coordinateClickDelay.setText(String.valueOf(e.clickDelay));
            coordinateBinding.coordinateClickInterval.setText(String.valueOf(e.clickInterval));
            coordinateBinding.coordinateClickNumber.setText(String.valueOf(e.clickNumber));
            coordinateBinding.coordinateClickCount.setText(String.valueOf(e.triggerCount));
            coordinateBinding.coordinateToast.setText(e.toast);
            coordinateBinding.coordinateComment.setText(e.comment);
            long day1 = (System.currentTimeMillis() - e.createTime) / (24 * 60 * 60 * 1000);
            long day2 = (System.currentTimeMillis() - e.lastTriggerTime) / (24 * 60 * 60 * 1000);
            coordinateBinding.coordinateCreateTime.setText(String.format("%s (%s天前)", dateFormat.format(new Date(e.createTime)), day1));
            coordinateBinding.coordinateLastClickTime.setTextColor(day1 >= 60 && day2 >= 60 ? Color.RED : coordinateBinding.coordinateLastClickTime.getCurrentTextColor());
            if (e.lastTriggerTime <= 0) {
                coordinateBinding.coordinateLastClickTime.setText("无触发记录");
            } else {
                coordinateBinding.coordinateLastClickTime.setText(String.format("%s (%s天前)", dateFormat.format(e.lastTriggerTime), day2));
            }

            Runnable coordinateSaveRun = new Runnable() {
                @Override
                public void run() {
                    String sX = coordinateBinding.coordinateXPosition.getText().toString();
                    String sY = coordinateBinding.coordinateYPosition.getText().toString();
                    String sDelay = coordinateBinding.coordinateClickDelay.getText().toString();
                    String sInterval = coordinateBinding.coordinateClickInterval.getText().toString();
                    String sNumber = coordinateBinding.coordinateClickNumber.getText().toString();
                    coordinateBinding.coordinateModify.setTextColor(0xffff0000);
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
                    e.xPosition = Integer.parseInt(sX);
                    e.yPosition = Integer.parseInt(sY);
                    e.clickDelay = Integer.parseInt(sDelay);
                    e.clickInterval = Integer.parseInt(sInterval);
                    e.clickNumber = Integer.parseInt(sNumber);
                    e.toast = coordinateBinding.coordinateToast.getText().toString().trim();
                    e.comment = coordinateBinding.coordinateComment.getText().toString().trim();
                    dataDao.updateCoordinate(e);
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
            coordinateBinding.coordinateToast.addTextChangedListener(coordinateTextWatcher);

            coordinateBinding.coordinateShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CoordinateShare coordinateShare = new CoordinateShare();
                    coordinateShare.coordinate = e;
                    coordinateShare.basicContent = new BasicContent();
                    coordinateShare.basicContent.fingerPrint = Build.FINGERPRINT;
                    coordinateShare.basicContent.displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(coordinateShare.basicContent.displayMetrics);
                    coordinateShare.basicContent.packageName = e.appPackage;
                    try {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(e.appPackage, PackageManager.GET_META_DATA);
                        coordinateShare.basicContent.versionCode = packageInfo.versionCode;
                        coordinateShare.basicContent.versionName = packageInfo.versionName;
                    } catch (PackageManager.NameNotFoundException ex) {
                        ex.printStackTrace();
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
                                    dataDao.deleteCoordinate(e);
                                    appDescribe.coordinateList.remove(e);
                                    editDataBinding.coordinateLayout.removeView(coordinateBinding.getRoot());
                                    if (appDescribe.coordinateList.isEmpty()) {
                                        editDataBinding.coordinateLayout.setVisibility(View.GONE);
                                        appDescribe.coordinateOnOff = false;
                                        baseSettingBinding.coordinateSwitch.setChecked(false);
                                        if (appDescribe.widgetList.isEmpty()) {
                                            appDescribe.onOff = false;
                                            appDescribe.widgetOnOff = false;
                                            baseSettingBinding.onOffSwitch.setChecked(false);
                                            baseSettingBinding.widgetSwitch.setChecked(false);
                                        }
                                        dataDao.updateAppDescribe(appDescribe);
                                    }
                                }
                            }).create().show();
                }
            });
            editDataBinding.coordinateLayout.addView(coordinateBinding.getRoot());
        }

        if (appDescribe.widgetList.isEmpty()) {
            editDataBinding.widgetLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.widgetLayout.setVisibility(View.VISIBLE);
        }
        if (editDataBinding.widgetLayout.getChildCount() > 2) {
            editDataBinding.widgetLayout.removeViews(2, editDataBinding.widgetLayout.getChildCount() - 2);
        }
        appDescribe.widgetList.sort(new Comparator<Widget>() {
            @Override
            public int compare(Widget o1, Widget o2) {
                return Long.compare(o2.createTime, o1.createTime);
            }
        });
        for (final Widget e : appDescribe.widgetList) {
            final ViewWidgetBinding widgetBinding = ViewWidgetBinding.inflate(inflater);
            widgetBinding.widgetActivity.setText(e.appActivity);
            widgetBinding.widgetClickable.setText(String.valueOf(e.widgetClickable));
            widgetBinding.widgetRect.setText(e.widgetRect != null ? gson.toJson(e.widgetRect) : null);
            widgetBinding.widgetId.setText(e.widgetId);
            widgetBinding.widgetDescribe.setText(e.widgetDescribe);
            widgetBinding.widgetText.setText(e.widgetText);
            widgetBinding.widgetClickDelay.setText(String.valueOf(e.clickDelay));
            widgetBinding.widgetDebounceDelay.setText(String.valueOf(e.debounceDelay));
            widgetBinding.widgetNoRepeat.setChecked(e.noRepeat);
            widgetBinding.widgetClickOnly.setChecked(e.clickOnly);
            widgetBinding.widgetToast.setText(e.toast);
            widgetBinding.widgetComment.setText(e.comment);
            widgetBinding.widgetClickNumber.setText(String.valueOf(e.clickNumber));
            widgetBinding.widgetClickInterval.setText(String.valueOf(e.clickInterval));
            widgetBinding.widgetClickCount.setText(String.valueOf(e.triggerCount));
            widgetBinding.widgetActionClick.setChecked(e.action == Widget.ACTION_CLICK);
            widgetBinding.widgetActionBack.setChecked(e.action == Widget.ACTION_BACK);
            widgetBinding.widgetActionClick.setEnabled(e.action != Widget.ACTION_CLICK);
            widgetBinding.widgetActionBack.setEnabled(e.action != Widget.ACTION_BACK);
            widgetBinding.llClickProp.setVisibility(e.action == Widget.ACTION_CLICK ? View.VISIBLE : View.GONE);
            widgetBinding.widgetConditionOr.setChecked(e.condition == Widget.CONDITION_OR);
            widgetBinding.widgetConditionAnd.setChecked(e.condition == Widget.CONDITION_AND);
            widgetBinding.widgetConditionOr.setEnabled(e.condition != Widget.CONDITION_OR);
            widgetBinding.widgetConditionAnd.setEnabled(e.condition != Widget.CONDITION_AND);
            long day1 = (System.currentTimeMillis() - e.createTime) / (24 * 60 * 60 * 1000);
            long day2 = (System.currentTimeMillis() - e.lastTriggerTime) / (24 * 60 * 60 * 1000);
            widgetBinding.widgetCreateTime.setText(String.format("%s (%s天前)", dateFormat.format(new Date(e.createTime)), day1));
            widgetBinding.widgetLastClickTime.setTextColor(day1 >= 60 && day2 >= 60 ? Color.RED : widgetBinding.widgetLastClickTime.getCurrentTextColor());
            if (e.lastTriggerTime <= 0) {
                widgetBinding.widgetLastClickTime.setText("无触发记录");
            } else {
                widgetBinding.widgetLastClickTime.setText(String.format("%s (%s天前)", dateFormat.format(e.lastTriggerTime), day2));
            }
            Runnable widgetSaveRun = new Runnable() {
                @Override
                public void run() {
                    String clickNumber = widgetBinding.widgetClickNumber.getText().toString();
                    String clickInterval = widgetBinding.widgetClickInterval.getText().toString();
                    String clickDelay = widgetBinding.widgetClickDelay.getText().toString();
                    String debounceDelay = widgetBinding.widgetDebounceDelay.getText().toString();
                    widgetBinding.widgetModify.setTextColor(0xffff0000);
                    if (clickNumber.isEmpty()) {
                        widgetBinding.widgetModify.setText("点击次数不能为空");
                        return;
                    }
                    if (clickInterval.isEmpty()) {
                        widgetBinding.widgetModify.setText("点击间隔不能为空");
                        return;
                    }
                    if (clickDelay.isEmpty()) {
                        widgetBinding.widgetModify.setText("延迟点击不能为空");
                        return;
                    }
                    if (debounceDelay.isEmpty()) {
                        widgetBinding.widgetModify.setText("防抖延迟不能为空");
                        return;
                    }
                    try {
                        e.widgetRect = gson.fromJson(widgetBinding.widgetRect.getText().toString().trim(), Rect.class);
                    } catch (JsonSyntaxException jsonSyntaxException) {
                        widgetBinding.widgetModify.setText("Bonus格式错误");
                        return;
                    }
                    e.widgetId = widgetBinding.widgetId.getText().toString().trim();
                    e.widgetDescribe = widgetBinding.widgetDescribe.getText().toString().trim();
                    e.widgetText = widgetBinding.widgetText.getText().toString().trim();
                    e.toast = widgetBinding.widgetToast.getText().toString().trim();
                    e.comment = widgetBinding.widgetComment.getText().toString().trim();
                    e.clickNumber = Integer.parseInt(clickNumber);
                    e.clickInterval = Integer.parseInt(clickInterval);
                    e.clickDelay = Integer.parseInt(clickDelay);
                    e.debounceDelay = Integer.parseInt(debounceDelay);
                    e.noRepeat = widgetBinding.widgetNoRepeat.isChecked();
                    e.clickOnly = widgetBinding.widgetClickOnly.isChecked();
                    dataDao.updateWidget(e);
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
            widgetBinding.widgetId.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetDescribe.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetText.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetClickNumber.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetClickInterval.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetClickDelay.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetDebounceDelay.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetToast.addTextChangedListener(widgetTextWatcher);
            widgetBinding.widgetComment.addTextChangedListener(widgetTextWatcher);

            View.OnClickListener widgetClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == widgetBinding.widgetActionClick || v == widgetBinding.widgetActionBack) {
                        e.action = Integer.parseInt((String) v.getTag());
                        widgetBinding.widgetActionClick.setChecked(e.action == Widget.ACTION_CLICK);
                        widgetBinding.widgetActionBack.setChecked(e.action == Widget.ACTION_BACK);
                        widgetBinding.widgetActionClick.setEnabled(e.action != Widget.ACTION_CLICK);
                        widgetBinding.widgetActionBack.setEnabled(e.action != Widget.ACTION_BACK);
                        widgetBinding.llClickProp.setVisibility(e.action == Widget.ACTION_CLICK ? View.VISIBLE : View.GONE);
                    }
                    if (v == widgetBinding.widgetConditionOr || v == widgetBinding.widgetConditionAnd) {
                        e.condition = Integer.parseInt((String) v.getTag());
                        widgetBinding.widgetConditionOr.setChecked(e.condition == Widget.CONDITION_OR);
                        widgetBinding.widgetConditionAnd.setChecked(e.condition == Widget.CONDITION_AND);
                        widgetBinding.widgetConditionOr.setEnabled(e.condition != Widget.CONDITION_OR);
                        widgetBinding.widgetConditionAnd.setEnabled(e.condition != Widget.CONDITION_AND);
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
                    widgetShare.widget = e;
                    widgetShare.basicContent = new BasicContent();
                    widgetShare.basicContent.fingerPrint = Build.FINGERPRINT;
                    widgetShare.basicContent.displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(widgetShare.basicContent.displayMetrics);
                    widgetShare.basicContent.packageName = e.appPackage;
                    try {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(e.appPackage, PackageManager.GET_META_DATA);
                        widgetShare.basicContent.versionCode = packageInfo.versionCode;
                        widgetShare.basicContent.versionName = packageInfo.versionName;
                    } catch (PackageManager.NameNotFoundException ex) {
                        ex.printStackTrace();
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
                                    dataDao.deleteWidget(e);
                                    appDescribe.widgetList.remove(e);
                                    editDataBinding.widgetLayout.removeView(widgetBinding.getRoot());
                                    if (appDescribe.widgetList.isEmpty()) {
                                        editDataBinding.widgetLayout.setVisibility(View.GONE);
                                        appDescribe.widgetOnOff = false;
                                        baseSettingBinding.widgetSwitch.setChecked(false);
                                        if (appDescribe.coordinateList.isEmpty()) {
                                            appDescribe.onOff = false;
                                            appDescribe.coordinateOnOff = false;
                                            baseSettingBinding.onOffSwitch.setChecked(false);
                                            baseSettingBinding.coordinateSwitch.setChecked(false);
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
        MyUtils.requestUpdateCoordinate(appDescribe.appPackage);
        MyUtils.requestUpdateWidget(appDescribe.appPackage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyApplication.myAppConfig.autoHideOnTaskList) {
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
        binding.fileName.setHint(DigestUtils.md5Hex(strRegulation));
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
                            String fileName = binding.fileName.getText().toString();
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
