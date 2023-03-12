package com.lgh.advertising.going.myactivity;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lgh.advertising.going.BuildConfig;
import com.lgh.advertising.going.R;
import com.lgh.advertising.going.databinding.ActivityEditDataBinding;
import com.lgh.advertising.going.databinding.ViewAutoFinderBinding;
import com.lgh.advertising.going.databinding.ViewBaseSettingBinding;
import com.lgh.advertising.going.databinding.ViewCoordinateBinding;
import com.lgh.advertising.going.databinding.ViewEditFileNameBinding;
import com.lgh.advertising.going.databinding.ViewOnOffWarningBinding;
import com.lgh.advertising.going.databinding.ViewQuestionBinding;
import com.lgh.advertising.going.databinding.ViewWidgetBinding;
import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.mybean.BasicContent;
import com.lgh.advertising.going.mybean.Coordinate;
import com.lgh.advertising.going.mybean.CoordinateShare;
import com.lgh.advertising.going.mybean.Widget;
import com.lgh.advertising.going.mybean.WidgetShare;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;
import com.lgh.advertising.going.myfunction.MyUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class EditDataActivity extends BaseActivity {

    public static AppDescribe appDescribe;
    private Context context;
    private LayoutInflater inflater;
    private DataDao dataDao;
    private DisplayMetrics metrics;
    private SimpleDateFormat dateFormatModify;
    private SimpleDateFormat dateFormatCreate;
    private ActivityEditDataBinding editDataBinding;
    private ViewBaseSettingBinding baseSettingBinding;
    private ViewAutoFinderBinding autoFinderBinding;
    private Set<String> pkgSuggestNotOnList;
    private MyUtils myUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = getLayoutInflater();
        editDataBinding = ActivityEditDataBinding.inflate(inflater);
        setContentView(editDataBinding.getRoot());

        dataDao = MyApplication.dataDao;
        myUtils = MyApplication.myUtils;
        context = getApplicationContext();
        dateFormatModify = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault());
        dateFormatCreate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault());
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
        editDataBinding.autoFinderLayout.setLayoutTransition(transition);
        editDataBinding.coordinateLayout.setLayoutTransition(transition);
        editDataBinding.widgetLayout.setLayoutTransition(transition);

        class QuestionClickListener implements View.OnClickListener {
            private final int strId;

            private QuestionClickListener(int strId) {
                this.strId = strId;
            }

            @Override
            public void onClick(View v) {
                ViewQuestionBinding questionBinding = ViewQuestionBinding.inflate(inflater);
                questionBinding.questionAnswer.setText(Html.fromHtml(getString(strId), Html.FROM_HTML_MODE_COMPACT));
                AlertDialog alertDialog = new AlertDialog.Builder(EditDataActivity.this).setView(questionBinding.getRoot()).setPositiveButton("确定", null).create();
                Window window = alertDialog.getWindow();
                window.setBackgroundDrawableResource(R.drawable.add_data_background);
                alertDialog.show();
            }
        }
        editDataBinding.baseSettingQuestion.setOnClickListener(new QuestionClickListener(R.string.baseSettingQuestion));
        editDataBinding.autoFinderQuestion.setOnClickListener(new QuestionClickListener(R.string.autoFinderQuestion));
        editDataBinding.coordinateQuestion.setOnClickListener(new QuestionClickListener(R.string.coordinateQuestion));
        editDataBinding.widgetQuestion.setOnClickListener(new QuestionClickListener(R.string.widgetQuestion));
    }

    @Override
    protected void onStart() {
        super.onStart();
        String extraStr = getIntent().getStringExtra("packageName");
        if (!TextUtils.isEmpty(extraStr)) {
            AppDescribe appDescribeTemp = dataDao.getAppDescribeByPackage(extraStr);
            if (appDescribeTemp != null) {
                appDescribeTemp.getOtherFieldsFromDatabase(dataDao);
                appDescribe = appDescribeTemp;
            }
        }

        if (baseSettingBinding != null) {
            editDataBinding.baseSettingLayout.removeView(baseSettingBinding.getRoot());
        }
        baseSettingBinding = ViewBaseSettingBinding.inflate(inflater);
        baseSettingBinding.onOffName.setText(appDescribe.appName);
        baseSettingBinding.onOffSwitch.setChecked(appDescribe.onOff);

        baseSettingBinding.autoFinderSwitch.setChecked(appDescribe.autoFinderOnOFF);
        baseSettingBinding.autoFinderSustainTime.setText(String.valueOf(appDescribe.autoFinderRetrieveTime));
        baseSettingBinding.autoFinderRetrieveAllTime.setChecked(appDescribe.autoFinderRetrieveAllTime);

        baseSettingBinding.coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        baseSettingBinding.coordinateSustainTime.setText(String.valueOf(appDescribe.coordinateRetrieveTime));
        baseSettingBinding.coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        baseSettingBinding.widgetSwitch.setChecked(appDescribe.widgetOnOff);
        baseSettingBinding.widgetSustainTime.setText(String.valueOf(appDescribe.widgetRetrieveTime));
        baseSettingBinding.widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        Runnable baseSettingSaveRun = new Runnable() {
            @Override
            public void run() {
                String autoFinderTime = baseSettingBinding.autoFinderSustainTime.getText().toString();
                String coordinateTime = baseSettingBinding.coordinateSustainTime.getText().toString();
                String widgetTime = baseSettingBinding.widgetSustainTime.getText().toString();
                baseSettingBinding.baseSettingModify.setTextColor(0xffff0000);
                if (autoFinderTime.isEmpty() || coordinateTime.isEmpty() || widgetTime.isEmpty()) {
                    baseSettingBinding.baseSettingModify.setText("内容不能为空");
                    return;
                }
                appDescribe.onOff = baseSettingBinding.onOffSwitch.isChecked();
                appDescribe.autoFinderOnOFF = baseSettingBinding.autoFinderSwitch.isChecked();
                appDescribe.autoFinderRetrieveTime = Integer.parseInt(autoFinderTime);
                appDescribe.autoFinderRetrieveAllTime = baseSettingBinding.autoFinderRetrieveAllTime.isChecked();
                appDescribe.coordinateOnOff = baseSettingBinding.coordinateSwitch.isChecked();
                appDescribe.coordinateRetrieveTime = Integer.parseInt(coordinateTime);
                appDescribe.coordinateRetrieveAllTime = baseSettingBinding.coordinateRetrieveAllTime.isChecked();
                appDescribe.widgetOnOff = baseSettingBinding.widgetSwitch.isChecked();
                appDescribe.widgetRetrieveTime = Integer.parseInt(widgetTime);
                appDescribe.widgetRetrieveAllTime = baseSettingBinding.widgetRetrieveAllTime.isChecked();
                dataDao.updateAppDescribe(appDescribe);
                baseSettingBinding.baseSettingModify.setTextColor(0xff000000);
                baseSettingBinding.baseSettingModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
            }
        };
        baseSettingBinding.onOffSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((Switch) v).isChecked();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        baseSettingBinding.onOffSwitch.setChecked(isChecked);
                        baseSettingBinding.autoFinderSwitch.setChecked(isChecked);
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
            }
        };
        baseSettingBinding.autoFinderSwitch.setOnClickListener(onOffClickListener);
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
        baseSettingBinding.autoFinderSustainTime.addTextChangedListener(sustainTimeTextWatcher);
        baseSettingBinding.widgetSustainTime.addTextChangedListener(sustainTimeTextWatcher);
        baseSettingBinding.coordinateSustainTime.addTextChangedListener(sustainTimeTextWatcher);

        View.OnClickListener allTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseSettingSaveRun.run();
            }
        };
        baseSettingBinding.autoFinderRetrieveAllTime.setOnClickListener(allTimeClickListener);
        baseSettingBinding.widgetRetrieveAllTime.setOnClickListener(allTimeClickListener);
        baseSettingBinding.coordinateRetrieveAllTime.setOnClickListener(allTimeClickListener);
        editDataBinding.baseSettingLayout.addView(baseSettingBinding.getRoot());

        if (autoFinderBinding != null) {
            editDataBinding.autoFinderLayout.removeView(autoFinderBinding.getRoot());
        }
        autoFinderBinding = ViewAutoFinderBinding.inflate(inflater);
        autoFinderBinding.retrieveKeyword.setText(appDescribe.autoFinder.keywordList.isEmpty() ? "" : new Gson().toJson(appDescribe.autoFinder.keywordList));
        autoFinderBinding.retrieveNumber.setText(String.valueOf(appDescribe.autoFinder.retrieveNumber));
        autoFinderBinding.clickDelay.setText(String.valueOf(appDescribe.autoFinder.clickDelay));
        autoFinderBinding.clickOnly.setChecked(appDescribe.autoFinder.clickOnly);

        Runnable autoFinderSaveRun = new Runnable() {
            @Override
            public void run() {
                List<String> temKeyword;
                String keywordList = autoFinderBinding.retrieveKeyword.getText().toString().trim();
                String retrieveNumber = autoFinderBinding.retrieveNumber.getText().toString();
                String clickDelay = autoFinderBinding.clickDelay.getText().toString();
                autoFinderBinding.autoFinderModify.setTextColor(0xffff0000);
                try {
                    temKeyword = new Gson().fromJson(keywordList.isEmpty() || keywordList.equalsIgnoreCase("null") ? "[]" : keywordList, new TypeToken<List<String>>() {
                    }.getType());
                } catch (JsonSyntaxException jse) {
                    autoFinderBinding.autoFinderModify.setText("关键词格式填写错误");
                    return;
                }
                if (retrieveNumber.isEmpty() || clickDelay.isEmpty()) {
                    autoFinderBinding.autoFinderModify.setText("内容不能为空");
                    return;
                } else if (Integer.parseInt(retrieveNumber) < 1 || Integer.parseInt(retrieveNumber) > 100) {
                    autoFinderBinding.autoFinderModify.setText("检索次数应为１~100次之间");
                    return;
                } else if (Integer.parseInt(clickDelay) > 8000) {
                    autoFinderBinding.autoFinderModify.setText("点击延迟应为0~8000(ms)之间");
                    return;
                } else {
                    appDescribe.autoFinder.keywordList = temKeyword;
                    appDescribe.autoFinder.retrieveNumber = Integer.parseInt(retrieveNumber);
                    appDescribe.autoFinder.clickDelay = Integer.parseInt(clickDelay);
                    appDescribe.autoFinder.clickOnly = autoFinderBinding.clickOnly.isChecked();
                }
                dataDao.updateAutoFinder(appDescribe.autoFinder);
                autoFinderBinding.autoFinderModify.setTextColor(0xff000000);
                autoFinderBinding.autoFinderModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
            }
        };

        TextWatcher autoFinderTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                autoFinderSaveRun.run();
            }
        };
        autoFinderBinding.retrieveKeyword.addTextChangedListener(autoFinderTextWatcher);
        autoFinderBinding.retrieveNumber.addTextChangedListener(autoFinderTextWatcher);
        autoFinderBinding.clickDelay.addTextChangedListener(autoFinderTextWatcher);

        autoFinderBinding.clickOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFinderSaveRun.run();
            }
        });
        editDataBinding.autoFinderLayout.addView(autoFinderBinding.getRoot());

        final List<Coordinate> coordinateList = new ArrayList<>(appDescribe.coordinateMap.values());
        if (coordinateList.isEmpty()) {
            editDataBinding.coordinateLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.coordinateLayout.setVisibility(View.VISIBLE);
        }
        coordinateList.sort(new Comparator<Coordinate>() {
            @Override
            public int compare(Coordinate o1, Coordinate o2) {
                return (int) (o2.createTime - o1.createTime);
            }
        });
        if (editDataBinding.coordinateLayout.getChildCount() > 2) {
            editDataBinding.coordinateLayout.removeViews(2, editDataBinding.coordinateLayout.getChildCount() - 2);
        }
        for (final Coordinate e : coordinateList) {
            final ViewCoordinateBinding coordinateBinding = ViewCoordinateBinding.inflate(inflater);
            coordinateBinding.coordinateActivity.setText(e.appActivity);
            coordinateBinding.coordinateXPosition.setText(String.valueOf(e.xPosition));
            coordinateBinding.coordinateYPosition.setText(String.valueOf(e.yPosition));
            coordinateBinding.coordinateClickDelay.setText(String.valueOf(e.clickDelay));
            coordinateBinding.coordinateClickInterval.setText(String.valueOf(e.clickInterval));
            coordinateBinding.coordinateClickNumber.setText(String.valueOf(e.clickNumber));
            coordinateBinding.coordinateComment.setText(e.comment);
            coordinateBinding.coordinateCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));

            Runnable coordinateSaveRun = new Runnable() {
                @Override
                public void run() {
                    String sX = coordinateBinding.coordinateXPosition.getText().toString();
                    String sY = coordinateBinding.coordinateYPosition.getText().toString();
                    String sDelay = coordinateBinding.coordinateClickDelay.getText().toString();
                    String sInterval = coordinateBinding.coordinateClickInterval.getText().toString();
                    String sNumber = coordinateBinding.coordinateClickNumber.getText().toString();
                    String sComment = coordinateBinding.coordinateComment.getText().toString().trim();
                    coordinateBinding.coordinateModify.setTextColor(0xffff0000);
                    if (sX.isEmpty() || sY.isEmpty() || sDelay.isEmpty() || sInterval.isEmpty() || sNumber.isEmpty()) {
                        coordinateBinding.coordinateModify.setText("内容不能为空");
                        return;
                    } else if (Integer.parseInt(sX) > metrics.widthPixels) {
                        coordinateBinding.coordinateModify.setText("X坐标超出屏幕寸");
                        return;
                    } else if (Integer.parseInt(sY) > metrics.heightPixels) {
                        coordinateBinding.coordinateModify.setText("Y坐标超出屏幕寸");
                        return;
                    } else if (Integer.parseInt(sDelay) > 8000) {
                        coordinateBinding.coordinateModify.setText("点击延迟应为0~8000(ms)之间");
                        return;
                    } else if (Integer.parseInt(sInterval) < 100 || Integer.parseInt(sInterval) > 2000) {
                        coordinateBinding.coordinateModify.setText("点击间隔应为100~2000(ms)之间");
                        return;
                    } else if (Integer.parseInt(sNumber) < 1 || Integer.parseInt(sNumber) > 20) {
                        coordinateBinding.coordinateModify.setText("点击次数应为1~20次之间");
                        return;
                    } else {
                        e.xPosition = Integer.parseInt(sX);
                        e.yPosition = Integer.parseInt(sY);
                        e.clickDelay = Integer.parseInt(sDelay);
                        e.clickInterval = Integer.parseInt(sInterval);
                        e.clickNumber = Integer.parseInt(sNumber);
                        e.comment = sComment;
                    }
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
                    dataDao.deleteCoordinate(e);
                    appDescribe.coordinateMap.remove(e.appActivity);
                    editDataBinding.coordinateLayout.removeView(coordinateBinding.getRoot());
                    if (appDescribe.coordinateMap.isEmpty()) {
                        editDataBinding.coordinateLayout.setVisibility(View.GONE);
                    }
                }
            });
            editDataBinding.coordinateLayout.addView(coordinateBinding.getRoot());
        }

        List<Set<Widget>> widgetSetList = new ArrayList<>(appDescribe.widgetSetMap.values());
        if (widgetSetList.isEmpty()) {
            editDataBinding.widgetLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.widgetLayout.setVisibility(View.VISIBLE);
        }
        if (editDataBinding.widgetLayout.getChildCount() > 2) {
            editDataBinding.widgetLayout.removeViews(2, editDataBinding.widgetLayout.getChildCount() - 2);
        }
        for (final Set<Widget> widgetSet : widgetSetList) {
            final List<Widget> widgetList = new ArrayList<>(widgetSet);
            widgetList.sort(new Comparator<Widget>() {
                @Override
                public int compare(Widget o1, Widget o2) {
                    return (int) (o2.createTime - o1.createTime);
                }
            });
            for (final Widget e : widgetList) {
                final ViewWidgetBinding widgetBinding = ViewWidgetBinding.inflate(inflater);
                widgetBinding.widgetActivity.setText(e.appActivity);
                widgetBinding.widgetClickable.setText(String.valueOf(e.widgetClickable));
                widgetBinding.widgetRect.setText(e.widgetRect.toShortString());
                widgetBinding.widgetId.setText(e.widgetId);
                widgetBinding.widgetDescribe.setText(e.widgetDescribe);
                widgetBinding.widgetText.setText(e.widgetText);
                widgetBinding.widgetClickDelay.setText(String.valueOf(e.clickDelay));
                widgetBinding.widgetNoRepeat.setChecked(e.noRepeat);
                widgetBinding.widgetClickOnly.setChecked(e.clickOnly);
                widgetBinding.widgetComment.setText(e.comment);
                widgetBinding.widgetCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));

                Runnable widgetSaveRun = new Runnable() {
                    @Override
                    public void run() {
                        String clickDelay = widgetBinding.widgetClickDelay.getText().toString();
                        widgetBinding.widgetModify.setTextColor(0xffff0000);
                        if (clickDelay.isEmpty()) {
                            widgetBinding.widgetModify.setText("延迟点击不能为空");
                            return;
                        } else if (Integer.parseInt(clickDelay) > 8000) {
                            widgetBinding.widgetModify.setText("点击延迟应为0~8000(ms)之间");
                            return;
                        }
                        e.widgetId = widgetBinding.widgetId.getText().toString().trim();
                        e.widgetDescribe = widgetBinding.widgetDescribe.getText().toString().trim();
                        e.widgetText = widgetBinding.widgetText.getText().toString().trim();
                        e.clickDelay = Integer.parseInt(clickDelay);
                        e.noRepeat = widgetBinding.widgetNoRepeat.isChecked();
                        e.clickOnly = widgetBinding.widgetClickOnly.isChecked();
                        e.comment = widgetBinding.widgetComment.getText().toString().trim();
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

                widgetBinding.widgetId.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetDescribe.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetText.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetClickDelay.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetComment.addTextChangedListener(widgetTextWatcher);

                View.OnClickListener widgetClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        widgetSaveRun.run();
                    }
                };
                widgetBinding.widgetNoRepeat.setOnClickListener(widgetClickListener);
                widgetBinding.widgetClickOnly.setOnClickListener(widgetClickListener);

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
                        dataDao.deleteWidget(e);
                        widgetSet.remove(e);
                        widgetList.remove(e);
                        editDataBinding.widgetLayout.removeView(widgetBinding.getRoot());
                        if (widgetSet.isEmpty()) {
                            appDescribe.widgetSetMap.remove(e.appActivity);
                        }
                        if (appDescribe.widgetSetMap.isEmpty()) {
                            editDataBinding.widgetLayout.setVisibility(View.GONE);
                        }
                    }
                });
                editDataBinding.widgetLayout.addView(widgetBinding.getRoot());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myUtils.requestUpdateAppDescribe(appDescribe.appPackage);
        myUtils.requestUpdateAutoFinder(appDescribe.appPackage);
        myUtils.requestUpdateCoordinate(appDescribe.appPackage);
        myUtils.requestUpdateWidget(appDescribe.appPackage);
    }

    private void showEditShareFileNameDialog(String strRule) {
        ViewEditFileNameBinding binding = ViewEditFileNameBinding.inflate(inflater);
        binding.fileName.setHint(String.valueOf(Math.abs(strRule.hashCode())));
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
                            File file = new File(getCacheDir(), (fileName.isEmpty() ? "" : fileName + "-") + binding.fileName.getHint() + ".txt");
                            FileUtils.writeStringToFile(file, strRule, StandardCharsets.UTF_8);
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                            intent.setDataAndType(uri, getContentResolver().getType(uri));
                            intent.putExtra(Intent.EXTRA_TEXT, strRule);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.setClipData(new ClipData(ClipData.newUri(getContentResolver(), "rule", uri)));
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "分享"));
                        } catch (IOException ex) {
                            Toast.makeText(context, "生成分享文件时发生错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).create().show();
    }
}
