package com.lgh.advertising.myactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lgh.advertising.going.R;
import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.DataBridge;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.Widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EditDataActivity extends Activity {

    Context context;
    AppDescribe appDescribe;
    LayoutInflater inflater;
    DataDao dataDao;
    DisplayMetrics metrics;
    SimpleDateFormat dateFormatModify;
    SimpleDateFormat dateFormatCreate;

    LinearLayout layoutBaseSetting;
    LinearLayout layoutAutoFinder;
    LinearLayout layoutCoordinate;
    LinearLayout layoutWidget;

    ImageButton questionBaseSetting;
    ImageButton questionAutoFinder;
    ImageButton questionCoordinate;
    ImageButton questionWidget;

    View viewBaseSetting;
    Switch onOffSwitch;
    EditText onOffName;

    Switch autoFinderSwitch;
    EditText autoFinderSustainTime;
    CheckBox autoFinderRetrieveAllTime;

    Switch coordinateSwitch;
    EditText coordinateSustainTime;
    CheckBox coordinateRetrieveAllTime;

    Switch widgetSwitch;
    EditText widgetSustainTime;
    CheckBox widgetRetrieveAllTime;

    TextView baseSettingModify;
    TextView baseSettingDelete;
    TextView baseSettingSure;

    View viewAutoFinder;
    EditText autoFinderKeyword;
    EditText autoFinderRetrieveNumber;
    EditText autoFinderClickDelay;
    CheckBox autoFinderClickOnly;
    TextView autoFinderModify;
    TextView autoFinderDelete;
    TextView autoFinderSure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);
        context = getApplicationContext();
        appDescribe = DataBridge.appDescribe;
        inflater = LayoutInflater.from(context);
        dataDao = DataDaoFactory.getInstance(context);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        dateFormatModify = new SimpleDateFormat("HH:mm:ss a", Locale.ENGLISH);
        dateFormatCreate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH);
        layoutBaseSetting = findViewById(R.id.base_setting_layout);
        layoutAutoFinder = findViewById(R.id.auto_finder_layout);
        layoutCoordinate = findViewById(R.id.coordinate_layout);
        layoutWidget = findViewById(R.id.widget_layout);

        questionBaseSetting = findViewById(R.id.base_setting_question);
        questionAutoFinder = findViewById(R.id.auto_finder_question);
        questionCoordinate = findViewById(R.id.coordinate_question);
        questionWidget = findViewById(R.id.widget_question);
        class QuestionClickListener implements View.OnClickListener {
            private int strId;

            private QuestionClickListener(int strId) {
                this.strId = strId;
            }

            @Override
            public void onClick(View v) {
                View view = inflater.inflate(R.layout.view_question, null);
                TextView textView = view.findViewById(R.id.question_answer);
                textView.setText(Html.fromHtml(getString(strId)));
                AlertDialog alertDialog = new AlertDialog.Builder(EditDataActivity.this).setView(view).setPositiveButton("确定", null).create();
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.add_data_background);
                }
                alertDialog.show();
            }
        }
        questionBaseSetting.setOnClickListener(new QuestionClickListener(R.string.baseSettingQuestion));
        questionAutoFinder.setOnClickListener(new QuestionClickListener(R.string.autoFinderQuestion));
        questionCoordinate.setOnClickListener(new QuestionClickListener(R.string.coordinateQuestion));
        questionWidget.setOnClickListener(new QuestionClickListener(R.string.widgetQuestion));

        viewBaseSetting = inflater.inflate(R.layout.view_base_setting, null);
        onOffSwitch = viewBaseSetting.findViewById(R.id.on_off_switch);
        onOffName = viewBaseSetting.findViewById(R.id.on_off_name);

        autoFinderSwitch = viewBaseSetting.findViewById(R.id.auto_finder_switch);
        autoFinderSustainTime = viewBaseSetting.findViewById(R.id.auto_finder_sustainTime);
        autoFinderRetrieveAllTime = viewBaseSetting.findViewById(R.id.auto_finder_retrieveAllTime);

        coordinateSwitch = viewBaseSetting.findViewById(R.id.coordinate_switch);
        coordinateSustainTime = viewBaseSetting.findViewById(R.id.coordinate_sustainTime);
        coordinateRetrieveAllTime = viewBaseSetting.findViewById(R.id.coordinate_retrieveAllTime);

        widgetSwitch = viewBaseSetting.findViewById(R.id.widget_switch);
        widgetSustainTime = viewBaseSetting.findViewById(R.id.widget_sustainTime);
        widgetRetrieveAllTime = viewBaseSetting.findViewById(R.id.widget_retrieveAllTime);

        baseSettingModify = viewBaseSetting.findViewById(R.id.base_setting_modify);
        baseSettingDelete = viewBaseSetting.findViewById(R.id.base_setting_delete);
        baseSettingSure = viewBaseSetting.findViewById(R.id.base_setting_sure);
        baseSettingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "该选项不允许删除", Toast.LENGTH_SHORT).show();
            }
        });
        baseSettingSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String autoFinderTime = autoFinderSustainTime.getText().toString();
                String coordinateTime = coordinateSustainTime.getText().toString();
                String widgetTime = widgetSustainTime.getText().toString();
                baseSettingModify.setTextColor(0xffff0000);
                if (autoFinderTime.isEmpty() || coordinateTime.isEmpty() || widgetTime.isEmpty()) {
                    baseSettingModify.setText("内容不能为空");
                    return;
                }
                appDescribe.on_off = onOffSwitch.isChecked();
                appDescribe.autoFinderOnOFF = autoFinderSwitch.isChecked();
                appDescribe.autoFinderRetrieveTime = Integer.valueOf(autoFinderTime);
                appDescribe.autoFinderRetrieveAllTime = autoFinderRetrieveAllTime.isChecked();
                appDescribe.coordinateOnOff = coordinateSwitch.isChecked();
                appDescribe.coordinateRetrieveTime = Integer.valueOf(coordinateTime);
                appDescribe.coordinateRetrieveAllTime = coordinateRetrieveAllTime.isChecked();
                appDescribe.widgetOnOff = widgetSwitch.isChecked();
                appDescribe.widgetRetrieveTime = Integer.valueOf(widgetTime);
                appDescribe.widgetRetrieveAllTime = widgetRetrieveAllTime.isChecked();
                dataDao.updateAppDescribe(appDescribe);
                baseSettingModify.setTextColor(0xff000000);
                baseSettingModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
            }
        });
        layoutBaseSetting.addView(viewBaseSetting);


        viewAutoFinder = inflater.inflate(R.layout.view_auto_finder, null);
        autoFinderKeyword = viewAutoFinder.findViewById(R.id.retrieveKeyword);
        autoFinderRetrieveNumber = viewAutoFinder.findViewById(R.id.retrieveNumber);
        autoFinderClickDelay = viewAutoFinder.findViewById(R.id.clickDelay);
        autoFinderClickOnly = viewAutoFinder.findViewById(R.id.clickOnly);
        autoFinderModify = viewAutoFinder.findViewById(R.id.auto_finder_modify);
        autoFinderDelete = viewAutoFinder.findViewById(R.id.auto_finder_delete);
        autoFinderSure = viewAutoFinder.findViewById(R.id.auto_finder_sure);
        autoFinderDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "该选项不允许删除", Toast.LENGTH_SHORT).show();
            }
        });
        autoFinderSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> temKeyword;
                String keywordList = autoFinderKeyword.getText().toString().trim();
                String retrieveNumber = autoFinderRetrieveNumber.getText().toString();
                String clickDelay = autoFinderClickDelay.getText().toString();
                autoFinderModify.setTextColor(0xffff0000);
                try {
                    temKeyword = new Gson().fromJson(keywordList.isEmpty() || keywordList.equalsIgnoreCase("null") ? "[]" : keywordList, new TypeToken<List<String>>() {
                    }.getType());
                    autoFinderKeyword.setText(keywordList);
                } catch (JsonSyntaxException jse) {
                    autoFinderModify.setText("关键词格式填写错误");
                    return;
                }
                if (retrieveNumber.isEmpty() || clickDelay.isEmpty()) {
                    autoFinderModify.setText("内容不能为空");
                    return;
                } else if (Integer.valueOf(retrieveNumber) < 1 || Integer.valueOf(retrieveNumber) > 100) {
                    autoFinderModify.setText("检索次数应为１~100次之间");
                    return;
                } else if (Integer.valueOf(clickDelay) > 4000) {
                    autoFinderModify.setText("点击延迟应为0~4000(ms)之间");
                    return;
                } else {
                    appDescribe.autoFinder.keywordList = temKeyword;
                    appDescribe.autoFinder.retrieveNumber = Integer.valueOf(retrieveNumber);
                    appDescribe.autoFinder.clickDelay = Integer.valueOf(clickDelay);
                    appDescribe.autoFinder.clickOnly = autoFinderClickOnly.isChecked();
                }
                dataDao.updateAutoFinder(appDescribe.autoFinder);
                autoFinderModify.setTextColor(0xff000000);
                autoFinderModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
            }
        });
        layoutAutoFinder.addView(viewAutoFinder);
    }

    @Override
    protected void onStart() {
        super.onStart();

        onOffName.setText(appDescribe.appName);
        onOffSwitch.setChecked(appDescribe.on_off);

        autoFinderSwitch.setChecked(appDescribe.autoFinderOnOFF);
        autoFinderSustainTime.setText(String.valueOf(appDescribe.autoFinderRetrieveTime));
        autoFinderRetrieveAllTime.setChecked(appDescribe.autoFinderRetrieveAllTime);

        coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        coordinateSustainTime.setText(String.valueOf(appDescribe.coordinateRetrieveTime));
        coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        widgetSwitch.setChecked(appDescribe.widgetOnOff);
        widgetSustainTime.setText(String.valueOf(appDescribe.widgetRetrieveTime));
        widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        autoFinderKeyword.setText(appDescribe.autoFinder.keywordList == null || appDescribe.autoFinder.keywordList.isEmpty() ? "" : new Gson().toJson(appDescribe.autoFinder.keywordList));
        autoFinderRetrieveNumber.setText(String.valueOf(appDescribe.autoFinder.retrieveNumber));
        autoFinderClickDelay.setText(String.valueOf(appDescribe.autoFinder.clickDelay));
        autoFinderClickOnly.setChecked(appDescribe.autoFinder.clickOnly);

        final List<Coordinate> coordinateList = new ArrayList<>(appDescribe.coordinateMap.values());
        if (coordinateList.isEmpty()) {
            layoutCoordinate.setVisibility(View.GONE);
        } else {
            layoutCoordinate.setVisibility(View.VISIBLE);
        }
        Collections.sort(coordinateList, new Comparator<Coordinate>() {
            @Override
            public int compare(Coordinate o1, Coordinate o2) {
                return (int) (o2.createTime - o1.createTime);
            }
        });
        if (layoutCoordinate.getChildCount() > 2) {
            layoutCoordinate.removeViews(2, layoutCoordinate.getChildCount() - 2);
        }
        for (final Coordinate e : coordinateList) {
            final View viewCoordinate = inflater.inflate(R.layout.view_coordinate, null);
            EditText coordinateActivity = viewCoordinate.findViewById(R.id.coordinate_activity);
            EditText coordinateCreateTime = viewCoordinate.findViewById(R.id.coordinate_createTime);
            final EditText coordinateXPosition = viewCoordinate.findViewById(R.id.coordinate_xPosition);
            final EditText coordinateYPosition = viewCoordinate.findViewById(R.id.coordinate_yPosition);
            final EditText coordinateDelay = viewCoordinate.findViewById(R.id.coordinate_clickDelay);
            final EditText coordinateInterval = viewCoordinate.findViewById(R.id.coordinate_clickInterval);
            final EditText coordinateNumber = viewCoordinate.findViewById(R.id.coordinate_clickNumber);
            final EditText coordinateComment = viewCoordinate.findViewById(R.id.coordinate_comment);
            final TextView coordinateModify = viewCoordinate.findViewById(R.id.coordinate_modify);
            TextView coordinateDelete = viewCoordinate.findViewById(R.id.coordinate_delete);
            TextView coordinateSure = viewCoordinate.findViewById(R.id.coordinate_sure);
            coordinateActivity.setText(e.appActivity);
            coordinateCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));
            coordinateXPosition.setText(String.valueOf(e.xPosition));
            coordinateYPosition.setText(String.valueOf(e.yPosition));
            coordinateDelay.setText(String.valueOf(e.clickDelay));
            coordinateInterval.setText(String.valueOf(e.clickInterval));
            coordinateNumber.setText(String.valueOf(e.clickNumber));
            coordinateComment.setText(e.comment);
            coordinateDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataDao.deleteCoordinate(e);
                    appDescribe.coordinateMap.remove(e.appActivity);
                    layoutCoordinate.removeView(viewCoordinate);
                }
            });
            coordinateSure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sX = coordinateXPosition.getText().toString();
                    String sY = coordinateYPosition.getText().toString();
                    String sDelay = coordinateDelay.getText().toString();
                    String sInterval = coordinateInterval.getText().toString();
                    String sNumber = coordinateNumber.getText().toString();
                    String sComment = coordinateComment.getText().toString().trim();
                    coordinateModify.setTextColor(0xffff0000);
                    if (sX.isEmpty() || sY.isEmpty() || sInterval.isEmpty() || sNumber.isEmpty()) {
                        coordinateModify.setText("内容不能为空");
                        return;
                    } else if (Integer.valueOf(sX) > metrics.widthPixels) {
                        coordinateModify.setText("X坐标超出屏幕寸");
                        return;
                    } else if (Integer.valueOf(sY) > metrics.heightPixels) {
                        coordinateModify.setText("Y坐标超出屏幕寸");
                        return;
                    } else if (Integer.valueOf(sDelay) > 4000) {
                        coordinateModify.setText("点击延迟应为0~4000(ms)之间");
                        return;
                    } else if (Integer.valueOf(sInterval) < 100 || Integer.valueOf(sInterval) > 2000) {
                        coordinateModify.setText("点击间隔应为100~2000(ms)之间");
                        return;
                    } else if (Integer.valueOf(sNumber) < 1 || Integer.valueOf(sNumber) > 20) {
                        coordinateModify.setText("点击次数应为1~20次之间");
                        return;
                    } else {
                        e.xPosition = Integer.valueOf(sX);
                        e.yPosition = Integer.valueOf(sY);
                        e.clickDelay = Integer.valueOf(sDelay);
                        e.clickInterval = Integer.valueOf(sInterval);
                        e.clickNumber = Integer.valueOf(sNumber);
                        e.comment = sComment;
                    }
                    dataDao.updateCoordinate(e);
                    coordinateComment.setText(e.comment);
                    coordinateModify.setTextColor(0xff000000);
                    coordinateModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
                }
            });
            layoutCoordinate.addView(viewCoordinate);
        }


        List<Set<Widget>> widgetSetList = new ArrayList<>(appDescribe.widgetSetMap.values());
        if (widgetSetList.isEmpty()) {
            layoutWidget.setVisibility(View.GONE);
        } else {
            layoutWidget.setVisibility(View.VISIBLE);
        }
        if (layoutWidget.getChildCount() > 2) {
            layoutWidget.removeViews(2, layoutWidget.getChildCount() - 2);
        }
        for (final Set<Widget> widgetSet : widgetSetList) {
            final List<Widget> widgetList = new ArrayList<>(widgetSet);
            Collections.sort(widgetList, new Comparator<Widget>() {
                @Override
                public int compare(Widget o1, Widget o2) {
                    return (int) (o2.createTime - o1.createTime);
                }
            });
            for (final Widget e : widgetList) {
                final View viewWidget = inflater.inflate(R.layout.view_widget, null);
                EditText widgetActivity = viewWidget.findViewById(R.id.widget_activity);
                EditText widgetCreateTime = viewWidget.findViewById(R.id.widget_createTime);
                EditText widgetClickable = viewWidget.findViewById(R.id.widget_clickable);
                EditText widgetRect = viewWidget.findViewById(R.id.widget_rect);
                final EditText widgetId = viewWidget.findViewById(R.id.widget_id);
                final EditText widgetDescribe = viewWidget.findViewById(R.id.widget_describe);
                final EditText widgetText = viewWidget.findViewById(R.id.widget_text);
                final EditText widgetClickDelay = viewWidget.findViewById(R.id.widget_clickDelay);
                final CheckBox widgetClickOnly = viewWidget.findViewById(R.id.widget_clickOnly);
                final EditText widgetComment = viewWidget.findViewById(R.id.widget_comment);
                final TextView widgetModify = viewWidget.findViewById(R.id.widget_modify);
                TextView widgetDelete = viewWidget.findViewById(R.id.widget_delete);
                TextView widgetSure = viewWidget.findViewById(R.id.widget_sure);
                widgetActivity.setText(e.appActivity);
                widgetCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));
                widgetClickable.setText(String.valueOf(e.widgetClickable));
                widgetRect.setText(e.widgetRect.toShortString());
                widgetId.setText(e.widgetId);
                widgetDescribe.setText(e.widgetDescribe);
                widgetText.setText(e.widgetText);
                widgetClickDelay.setText(String.valueOf(e.clickDelay));
                widgetClickOnly.setChecked(e.clickOnly);
                widgetComment.setText(e.comment);
                widgetDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataDao.deleteWidget(e);
                        widgetSet.remove(e);
                        widgetList.remove(e);
                        layoutWidget.removeView(viewWidget);
                        if (widgetSet.isEmpty()) {
                            appDescribe.widgetSetMap.remove(e.appActivity);
                        }

                    }
                });
                widgetSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String clickDelay = widgetClickDelay.getText().toString();
                        widgetModify.setTextColor(0xffff0000);
                        if (clickDelay.isEmpty()) {
                            widgetModify.setText("延迟点击不能为空");
                            return;
                        } else if (Integer.valueOf(clickDelay) > 4000) {
                            widgetModify.setText("点击延迟应为0~4000(ms)之间");
                            return;
                        }
                        e.widgetId = widgetId.getText().toString().trim();
                        e.widgetDescribe = widgetDescribe.getText().toString().trim();
                        e.widgetText = widgetText.getText().toString().trim();
                        e.clickDelay = Integer.valueOf(clickDelay);
                        e.clickOnly = widgetClickOnly.isChecked();
                        e.comment = widgetComment.getText().toString().trim();
                        dataDao.updateWidget(e);
                        widgetId.setText(e.widgetId);
                        widgetDescribe.setText(e.widgetDescribe);
                        widgetText.setText(e.widgetText);
                        widgetComment.setText(e.comment);
                        widgetModify.setTextColor(0xff000000);
                        widgetModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
                    }
                });
                layoutWidget.addView(viewWidget);
            }
        }
    }
}
