package com.lgh.advertising.myactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
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
import com.lgh.advertising.myclass.AutoFinder;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.Widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AppConfigActivity extends Activity {

    LayoutInflater inflater;
    DataDao dataDao;
    DisplayMetrics metrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
        inflater = LayoutInflater.from(this);
        dataDao = DataDaoFactory.getInstance(this);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        final AppDescribe appDescribe = AppSelectActivity.appDescribe;
        final SimpleDateFormat simpleDateFormat= new SimpleDateFormat("HH:mm:ss a", Locale.ENGLISH);

        ImageButton questionBaseSetting = findViewById(R.id.base_setting_question);
        ImageButton questionAutoFinder = findViewById(R.id.auto_finder_question);
        ImageButton questionCoordinate = findViewById(R.id.coordinate_question);
        ImageButton questionWidget = findViewById(R.id.widget_question);
        class QuestionClickListener implements View.OnClickListener {
            private int strId;
            private QuestionClickListener(int strId){
                this.strId = strId;
            }
            @Override
            public void onClick(View v) {
                View view = inflater.inflate(R.layout.view_question,null);
                TextView textView = view.findViewById(R.id.question_answer);
                textView.setText(Html.fromHtml(getString(strId)));
                AlertDialog alertDialog = new AlertDialog.Builder(AppConfigActivity.this).setView(view).setPositiveButton("确定",null).create();
                alertDialog.show();
            }
        }
        questionBaseSetting.setOnClickListener(new QuestionClickListener(R.string.baseSettingQuestion));
        questionAutoFinder.setOnClickListener(new QuestionClickListener(R.string.autoFinderQuestion));
        questionCoordinate.setOnClickListener(new QuestionClickListener(R.string.coordinateQuestion));
        questionWidget.setOnClickListener(new QuestionClickListener(R.string.widgetQuestion));

        final Switch onOffSwitch = findViewById(R.id.on_off_switch);
        EditText onOffName = findViewById(R.id.on_off_name);
        onOffName.setText(appDescribe.appName);
        onOffSwitch.setChecked(appDescribe.on_off);

        final Switch autoFinderSwitch = findViewById(R.id.auto_finder_switch);
        final EditText autoFinderSustainTime = findViewById(R.id.auto_finder_sustainTime);
        final CheckBox autoFinderRetrieveAllTime = findViewById(R.id.auto_finder_retrieveAllTime);
        autoFinderSwitch.setChecked(appDescribe.autoFinderOnOFF);
        autoFinderSustainTime.setText(String.valueOf(appDescribe.autoFinderRetrieveTime));
        autoFinderRetrieveAllTime.setChecked(appDescribe.autoFinderRetrieveAllTime);


        final Switch coordinateSwitch = findViewById(R.id.coordinate_switch);
        final EditText coordinateSustainTime = findViewById(R.id.coordinate_sustainTime);
        final CheckBox coordinateRetrieveAllTime = findViewById(R.id.coordinate_retrieveAllTime);
        coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        coordinateSustainTime.setText(String.valueOf(appDescribe.coordinateRetrieveTime));
        coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        final Switch widgetSwitch = findViewById(R.id.widget_switch);
        final EditText widgetSustainTime = findViewById(R.id.widget_sustainTime);
        final CheckBox widgetRetrieveAllTime = findViewById(R.id.widget_retrieveAllTime);
        widgetSwitch.setChecked(appDescribe.widgetOnOff);
        widgetSustainTime.setText(String.valueOf(appDescribe.widgetRetrieveTime));
        widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        final TextView baseSettingModify = findViewById(R.id.base_setting_modify);
        TextView baseSettingDelete = findViewById(R.id.base_setting_delete);
        TextView baseSettingSure = findViewById(R.id.base_setting_sure);
        baseSettingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AppConfigActivity.this,"该选项不允许删除",Toast.LENGTH_SHORT).show();
            }
        });
        baseSettingSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String autoFinderTime= autoFinderSustainTime.getText().toString();
                String coordinateTime = coordinateSustainTime.getText().toString();
                String widgetTime = widgetSustainTime.getText().toString();
                baseSettingModify.setTextColor(0xffff0000);
                if (autoFinderTime.isEmpty()||coordinateTime.isEmpty()||widgetTime.isEmpty()){}{
                    baseSettingModify.setText("内容不能为空");
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
                baseSettingModify.setText(simpleDateFormat.format(new Date()) + "(修改成功)");
            }
        });

        LinearLayout layoutAutoFinder = findViewById(R.id.auto_finder_layout);
        View viewAutoFinder = inflater.inflate(R.layout.view_auto_finder, null);
        final AutoFinder autoFinder = appDescribe.autoFinder;
        final EditText autoFinderKeyword = viewAutoFinder.findViewById(R.id.retrieveKeyword);
        final EditText autoFinderRetrieveNumber = viewAutoFinder.findViewById(R.id.retrieveNumber);
        final EditText autoFinderClickDelay = viewAutoFinder.findViewById(R.id.clickDelay);
        final CheckBox autoFinderClickOnly = viewAutoFinder.findViewById(R.id.clickOnly);
        final TextView autoFinderModify = viewAutoFinder.findViewById(R.id.auto_finder_modify);
        TextView autoFinderDelete = viewAutoFinder.findViewById(R.id.auto_finder_delete);
        TextView autoFinderSure = viewAutoFinder.findViewById(R.id.auto_finder_sure);
        autoFinderKeyword.setText(new Gson().toJson(autoFinder.keywordList));
        autoFinderRetrieveNumber.setText(String.valueOf(autoFinder.retrieveNumber));
        autoFinderClickDelay.setText(String.valueOf(autoFinder.clickDelay));
        autoFinderClickOnly.setChecked(autoFinder.clickOnly);
        autoFinderDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AppConfigActivity.this,"该选项不允许删除",Toast.LENGTH_SHORT).show();
            }
        });
        autoFinderSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keywordList = autoFinderKeyword.getText().toString().trim();
                String retrieveNumber = autoFinderRetrieveNumber.getText().toString();
                String clickDelay = autoFinderClickDelay.getText().toString();
                List<String> temKeyword;
                autoFinderModify.setTextColor(0xffff0000);
                try {
                    temKeyword = new Gson().fromJson(keywordList, new TypeToken<List<String>>() {
                    }.getType());
                    autoFinderKeyword.setText(keywordList);
                } catch (JsonSyntaxException jse) {
                    autoFinderModify.setText("关键词格式填写错误");
                    return;
                }
                if (retrieveNumber.isEmpty() || clickDelay.isEmpty()) {
                    autoFinderModify.setText("内容不能为空");
                    return;
                }else if (Integer.valueOf(retrieveNumber) < 1 || Integer.valueOf(retrieveNumber) > 100) {
                    autoFinderModify.setText("检索次数应为１~100次之间");
                    return;
                } else if (Integer.valueOf(clickDelay) > 4000) {
                    autoFinderModify.setText("点击延迟应为0~4000(ms)之间");
                    return;
                } else {
                    autoFinder.keywordList = temKeyword;
                    autoFinder.retrieveNumber = Integer.valueOf(retrieveNumber);
                    autoFinder.clickDelay = Integer.valueOf(clickDelay);
                    autoFinder.clickOnly = autoFinderClickOnly.isChecked();
                }
                dataDao.updateAutoFinder(autoFinder);
                autoFinderModify.setTextColor(0xff000000);
                autoFinderModify.setText(simpleDateFormat.format(new Date()) + "(修改成功)");
            }
        });
        layoutAutoFinder.addView(viewAutoFinder);

        final LinearLayout layoutCoordinate = findViewById(R.id.coordinate_layout);
        final List<Coordinate> coordinateList = new ArrayList<>(appDescribe.coordinateMap.values());
        if (coordinateList.isEmpty()) layoutCoordinate.setVisibility(View.GONE);
        for (final Coordinate e : coordinateList) {
            final View viewCoordinate = inflater.inflate(R.layout.view_coordinate, null);
            EditText coordinateActivity = viewCoordinate.findViewById(R.id.coordinate_activity);
            final EditText coordinateXPosition = viewCoordinate.findViewById(R.id.coordinate_xPosition);
            final EditText coordinateYPosition = viewCoordinate.findViewById(R.id.coordinate_yPosition);
            final EditText coordinateDelay = viewCoordinate.findViewById(R.id.coordinate_clickDelay);
            final EditText coordinateInterval = viewCoordinate.findViewById(R.id.coordinate_ClickInterval);
            final EditText coordinateNumber = viewCoordinate.findViewById(R.id.coordinate_clickNumber);
            final TextView coordinateModify = viewCoordinate.findViewById(R.id.coordinate_modify);
            TextView coordinateDelete = viewCoordinate.findViewById(R.id.coordinate_delete);
            TextView coordinateSure = viewCoordinate.findViewById(R.id.coordinate_sure);
            coordinateActivity.setText(e.appActivity);
            coordinateXPosition.setText(String.valueOf(e.xPosition));
            coordinateYPosition.setText(String.valueOf(e.yPosition));
            coordinateDelay.setText(String.valueOf(e.clickDelay));
            coordinateInterval.setText(String.valueOf(e.clickInterval));
            coordinateNumber.setText(String.valueOf(e.clickNumber));
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
                    }
                    dataDao.updateCoordinate(e);
                    coordinateModify.setTextColor(0xff000000);
                    coordinateModify.setText(simpleDateFormat.format(new Date()) + "(修改成功)");
                }
            });
            layoutCoordinate.addView(viewCoordinate);
        }


        final LinearLayout layoutWidget = findViewById(R.id.widget_layout);
        List<Set<Widget>> widgetSetList = new ArrayList<>(appDescribe.widgetSetMap.values());
        if (widgetSetList.isEmpty()) layoutWidget.setVisibility(View.GONE);
        for (final Set<Widget> widgetSet : widgetSetList) {
            for (final Widget e : widgetSet) {
                final View viewWidget = inflater.inflate(R.layout.view_widget, null);
                EditText widgetActivity = viewWidget.findViewById(R.id.widget_activity);
                EditText widgetClickable = viewWidget.findViewById(R.id.widget_clickable);
                EditText widgetRect = viewWidget.findViewById(R.id.widget_rect);
                final EditText widgetId = viewWidget.findViewById(R.id.widget_id);
                final EditText widgetDescribe = viewWidget.findViewById(R.id.widget_describe);
                final EditText widgetText = viewWidget.findViewById(R.id.widget_text);
                final EditText widgetClickDelay = viewWidget.findViewById(R.id.widget_clickDelay);
                final CheckBox widgetClickOnly = viewWidget.findViewById(R.id.widget_clickOnly);
                final TextView widgetModify = viewWidget.findViewById(R.id.widget_modify);
                TextView widgetDelete = viewWidget.findViewById(R.id.widget_delete);
                TextView widgetSure = viewWidget.findViewById(R.id.widget_sure);
                widgetActivity.setText(e.appActivity);
                widgetClickable.setText(String.valueOf(e.widgetClickable));
                widgetRect.setText(e.widgetRect.toShortString());
                widgetId.setText(e.widgetId);
                widgetDescribe.setText(e.widgetDescribe);
                widgetText.setText(e.widgetText);
                widgetClickDelay.setText(String.valueOf(e.clickDelay));
                widgetClickOnly.setChecked(e.clickOnly);
                widgetDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataDao.deleteWidget(e);
                        widgetSet.remove(e);
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
                        widgetId.setText(e.widgetId);
                        widgetDescribe.setText(e.widgetDescribe);
                        widgetText.setText(e.widgetText);
                        dataDao.updateWidget(e);
                        widgetModify.setTextColor(0xff000000);
                        widgetModify.setText(simpleDateFormat.format(new Date()) + "(修改成功)");
                    }
                });
                layoutWidget.addView(viewWidget);
            }
        }
    }
}
