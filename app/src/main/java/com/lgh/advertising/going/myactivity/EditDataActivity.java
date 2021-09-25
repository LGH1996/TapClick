package com.lgh.advertising.going.myactivity;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lgh.advertising.going.R;
import com.lgh.advertising.going.databinding.ActivityEditDataBinding;
import com.lgh.advertising.going.databinding.ViewAutoFinderBinding;
import com.lgh.advertising.going.databinding.ViewBaseSettingBinding;
import com.lgh.advertising.going.databinding.ViewCoordinateBinding;
import com.lgh.advertising.going.databinding.ViewQuestionBinding;
import com.lgh.advertising.going.databinding.ViewWidgetBinding;
import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.mybean.Coordinate;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;
import com.lgh.advertising.going.mybean.Widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EditDataActivity extends BaseActivity {

    private Context context;
    private AppDescribe appDescribe;
    private LayoutInflater inflater;
    private DataDao dataDao;
    private DisplayMetrics metrics;
    private SimpleDateFormat dateFormatModify;
    private SimpleDateFormat dateFormatCreate;
    private ActivityEditDataBinding editDataBinding;
    private ViewBaseSettingBinding baseSettingBinding;
    private ViewAutoFinderBinding autoFinderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editDataBinding = ActivityEditDataBinding.inflate(inflater = getLayoutInflater());
        setContentView(editDataBinding.getRoot());
        context = getApplicationContext();
        appDescribe = MyApplication.appDescribe;

        dataDao = MyApplication.dataDao;
        metrics = new DisplayMetrics();
        getDisplay().getRealMetrics(metrics);
        dateFormatModify = new SimpleDateFormat("HH:mm:ss a", Locale.ENGLISH);
        dateFormatCreate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH);

        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        editDataBinding.rootLayout.setLayoutTransition(transition);
        editDataBinding.baseSettingLayout.setLayoutTransition(transition);
        editDataBinding.autoFinderLayout.setLayoutTransition(transition);
        editDataBinding.coordinateLayout.setLayoutTransition(transition);
        editDataBinding.widgetLayout.setLayoutTransition(transition);

        class QuestionClickListener implements View.OnClickListener {
            private int strId;

            private QuestionClickListener(int strId) {
                this.strId = strId;
            }

            @Override
            public void onClick(View v) {
                ViewQuestionBinding questionBinding = ViewQuestionBinding.inflate(inflater);
                questionBinding.questionAnswer.setText(Html.fromHtml(getString(strId), Html.FROM_HTML_MODE_COMPACT));
                AlertDialog alertDialog = new AlertDialog.Builder(EditDataActivity.this).setView(questionBinding.getRoot()).setPositiveButton("确定", null).create();
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.add_data_background);
                }
                alertDialog.show();
            }
        }
        editDataBinding.baseSettingQuestion.setOnClickListener(new QuestionClickListener(R.string.baseSettingQuestion));
        editDataBinding.autoFinderQuestion.setOnClickListener(new QuestionClickListener(R.string.autoFinderQuestion));
        editDataBinding.coordinateQuestion.setOnClickListener(new QuestionClickListener(R.string.coordinateQuestion));
        editDataBinding.widgetQuestion.setOnClickListener(new QuestionClickListener(R.string.widgetQuestion));

        baseSettingBinding = ViewBaseSettingBinding.inflate(inflater);
        baseSettingBinding.baseSettingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "该选项不允许删除", Toast.LENGTH_SHORT).show();
            }
        });
        baseSettingBinding.baseSettingSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String autoFinderTime = baseSettingBinding.autoFinderSustainTime.getText().toString();
                String coordinateTime = baseSettingBinding.coordinateSustainTime.getText().toString();
                String widgetTime = baseSettingBinding.widgetSustainTime.getText().toString();
                baseSettingBinding.baseSettingModify.setTextColor(0xffff0000);
                if (autoFinderTime.isEmpty() || coordinateTime.isEmpty() || widgetTime.isEmpty()) {
                    baseSettingBinding.baseSettingModify.setText("内容不能为空");
                    return;
                }
                appDescribe.on_off = baseSettingBinding.onOffSwitch.isChecked();
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
        });
        editDataBinding.baseSettingLayout.addView(baseSettingBinding.getRoot());

        autoFinderBinding = ViewAutoFinderBinding.inflate(inflater);
        autoFinderBinding.autoFinderDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "该选项不允许删除", Toast.LENGTH_SHORT).show();
            }
        });
        autoFinderBinding.autoFinderSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> temKeyword;
                String keywordList = autoFinderBinding.retrieveKeyword.getText().toString().trim();
                String retrieveNumber = autoFinderBinding.retrieveNumber.getText().toString();
                String clickDelay = autoFinderBinding.clickDelay.getText().toString();
                autoFinderBinding.autoFinderModify.setTextColor(0xffff0000);
                try {
                    temKeyword = new Gson().fromJson(keywordList.isEmpty() || keywordList.equalsIgnoreCase("null") ? "[]" : keywordList, new TypeToken<List<String>>() {
                    }.getType());
                    autoFinderBinding.retrieveKeyword.setText(keywordList);
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
        });
        editDataBinding.autoFinderLayout.addView(autoFinderBinding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();

        baseSettingBinding.onOffName.setText(appDescribe.appName);
        baseSettingBinding.onOffSwitch.setChecked(appDescribe.on_off);

        baseSettingBinding.autoFinderSwitch.setChecked(appDescribe.autoFinderOnOFF);
        baseSettingBinding.autoFinderSustainTime.setText(String.valueOf(appDescribe.autoFinderRetrieveTime));
        baseSettingBinding.autoFinderRetrieveAllTime.setChecked(appDescribe.autoFinderRetrieveAllTime);

        baseSettingBinding.coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        baseSettingBinding.coordinateSustainTime.setText(String.valueOf(appDescribe.coordinateRetrieveTime));
        baseSettingBinding.coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        baseSettingBinding.widgetSwitch.setChecked(appDescribe.widgetOnOff);
        baseSettingBinding.widgetSustainTime.setText(String.valueOf(appDescribe.widgetRetrieveTime));
        baseSettingBinding.widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        autoFinderBinding.retrieveKeyword.setText(appDescribe.autoFinder.keywordList == null || appDescribe.autoFinder.keywordList.isEmpty() ? "" : new Gson().toJson(appDescribe.autoFinder.keywordList));
        autoFinderBinding.retrieveNumber.setText(String.valueOf(appDescribe.autoFinder.retrieveNumber));
        autoFinderBinding.clickDelay.setText(String.valueOf(appDescribe.autoFinder.clickDelay));
        autoFinderBinding.clickOnly.setChecked(appDescribe.autoFinder.clickOnly);

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
            coordinateBinding.coordinateCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));
            coordinateBinding.coordinateXPosition.setText(String.valueOf(e.xPosition));
            coordinateBinding.coordinateYPosition.setText(String.valueOf(e.yPosition));
            coordinateBinding.coordinateClickDelay.setText(String.valueOf(e.clickDelay));
            coordinateBinding.coordinateClickInterval.setText(String.valueOf(e.clickInterval));
            coordinateBinding.coordinateClickNumber.setText(String.valueOf(e.clickNumber));
            coordinateBinding.coordinateComment.setText(e.comment);
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
            coordinateBinding.coordinateSure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sX = coordinateBinding.coordinateXPosition.getText().toString();
                    String sY = coordinateBinding.coordinateYPosition.getText().toString();
                    String sDelay = coordinateBinding.coordinateClickDelay.getText().toString();
                    String sInterval = coordinateBinding.coordinateClickInterval.getText().toString();
                    String sNumber = coordinateBinding.coordinateClickNumber.getText().toString();
                    String sComment = coordinateBinding.coordinateComment.getText().toString().trim();
                    coordinateBinding.coordinateModify.setTextColor(0xffff0000);
                    if (sX.isEmpty() || sY.isEmpty() || sInterval.isEmpty() || sNumber.isEmpty()) {
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
                    coordinateBinding.coordinateComment.setText(e.comment);
                    coordinateBinding.coordinateModify.setTextColor(0xff000000);
                    coordinateBinding.coordinateModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
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
                widgetBinding.widgetCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));
                widgetBinding.widgetClickable.setText(String.valueOf(e.widgetClickable));
                widgetBinding.widgetRect.setText(e.widgetRect.toShortString());
                widgetBinding.widgetId.setText(e.widgetId);
                widgetBinding.widgetDescribe.setText(e.widgetDescribe);
                widgetBinding.widgetText.setText(e.widgetText);
                widgetBinding.widgetClickDelay.setText(String.valueOf(e.clickDelay));
                widgetBinding.widgetNoRepeat.setChecked(e.noRepeat);
                widgetBinding.widgetClickOnly.setChecked(e.clickOnly);
                widgetBinding.widgetComment.setText(e.comment);
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
                widgetBinding.widgetSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                        widgetBinding.widgetId.setText(e.widgetId);
                        widgetBinding.widgetDescribe.setText(e.widgetDescribe);
                        widgetBinding.widgetText.setText(e.widgetText);
                        widgetBinding.widgetComment.setText(e.comment);
                        widgetBinding.widgetModify.setTextColor(0xff000000);
                        widgetBinding.widgetModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
                    }
                });
                editDataBinding.widgetLayout.addView(widgetBinding.getRoot());
            }
        }
    }
}
