package com.lgh.advertising.going;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.AutoFinder;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.DataDao;
import com.lgh.advertising.myclass.DataDaoFactory;
import com.lgh.advertising.myclass.Widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AppConfigActivity extends AppCompatActivity {

    LayoutInflater inflater;
    DataDao dataDao;
    DisplayMetrics metrics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);
        dataDao = DataDaoFactory.getInstance(this);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        final AppDescribe appDescribe = AppSelectActivity.appDescribe;
        setContentView(R.layout.view_appconfig);
        setTitle(AppSelectActivity.appDescribe.appName);
        setContentView(R.layout.view_appconfig);

        Switch autoFinderSwitch = findViewById(R.id.auto_finder_switch);
        EditText autoFinderSustainTime = findViewById(R.id.auto_finder_sustainTime);
        CheckBox autoFinderRetrieveAllTime = findViewById(R.id.auto_finder_retrieveAllTime);
        autoFinderSwitch.setChecked(appDescribe.autoFinderOnOFF);
        autoFinderSustainTime.setText(String.valueOf(appDescribe.autoFinderRetrieveTime)) ;
        autoFinderRetrieveAllTime.setChecked(appDescribe.autoFinderRetrieveAllTime);


        Switch coordinateSwitch = findViewById(R.id.coordinate_switch);
        EditText coordinateSustainTime = findViewById(R.id.coordinate_sustainTime);
        CheckBox coordinateRetrieveAllTime = findViewById(R.id.coordinate_retrieveAllTime);
        coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        coordinateSustainTime.setText(String.valueOf(appDescribe.coordinateRetrieveTime));
        coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        Switch widgetSwitch = findViewById(R.id.widget_switch);
        EditText widgetSustainTime = findViewById(R.id.widget_sustainTime);
        CheckBox widgetRetrieveAllTime = findViewById(R.id.widget_retrieveAllTime);
        widgetSwitch.setChecked(appDescribe.widgetOnOff);
        widgetSustainTime.setText(String.valueOf(appDescribe.widgetRetrieveTime));
        widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        LinearLayout layoutAutoFinder = findViewById(R.id.auto_finder_layout);
        View viewAutoFinder = inflater.inflate(R.layout.view_auto_finder,null);
        AutoFinder autoFinder = appDescribe.autoFinder;
        EditText autoFinderKeyword = viewAutoFinder.findViewById(R.id.retrieveKeyword);
        EditText autoFinderRetrieveNumber = viewAutoFinder.findViewById(R.id.retrieveNumber);
        EditText autoFinderClickDelay = viewAutoFinder.findViewById(R.id.clickDelay);
        CheckBox autoFinderClickOnly = viewAutoFinder.findViewById(R.id.clickOnly);
        autoFinderKeyword.setText(autoFinder.keywordList.toString());
        autoFinderRetrieveNumber.setText(String.valueOf(autoFinder.retrieveNumber));
        autoFinderClickDelay.setText(String.valueOf(autoFinder.clickDelay));
        autoFinderClickOnly.setChecked(autoFinder.clickOnly);
        layoutAutoFinder.addView(viewAutoFinder);

        final LinearLayout layoutCoordinate = findViewById(R.id.coordinate_layout);
        final List<Coordinate> coordinateList = new ArrayList<>(appDescribe.coordinateMap.values());
        if (coordinateList.isEmpty()) layoutCoordinate.setVisibility(View.GONE);
        for (final Coordinate e:coordinateList){
            final View viewCoordinate = inflater.inflate(R.layout.view_coordinate, null);
            EditText coordinateActivity = viewCoordinate.findViewById(R.id.coordinate_activity);
            final EditText coordinateXPosition = viewCoordinate.findViewById(R.id.coordinate_xPosition);
            final EditText coordinateYPosition = viewCoordinate.findViewById(R.id.coordinate_yPosition);
            final EditText coordinateDelay = viewCoordinate.findViewById(R.id.coordinate_clickDelay);
            final EditText coordinateInterval = viewCoordinate.findViewById(R.id.coordinate_ClickInterval);
            final EditText coordinateNumber  = viewCoordinate.findViewById(R.id.coordinate_clickNumber);
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
                    dataDao.updateCoordiante(e);
                    coordinateModify.setTextColor(0xff000000);
                    coordinateModify.setText((new SimpleDateFormat("HH:mm:ss a", Locale.ENGLISH).format(new Date()) + "(修改成功)"));
                }
            });
            layoutCoordinate.addView(viewCoordinate);
        }


        final LinearLayout layoutWidget = findViewById(R.id.widget_layout);
        List<Set<Widget>> widgetSetList = new ArrayList<>(appDescribe.widgetSetMap.values());
        if (widgetSetList.isEmpty()) layoutWidget.setVisibility(View.GONE);
        for (final Set<Widget> widgetSet:widgetSetList) {
            for (final Widget e:widgetSet) {
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
                        if (widgetSet.isEmpty()){
                            appDescribe.widgetSetMap.remove(e.appActivity);
                        }

                    }
                });
                widgetSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String clickDelay = widgetClickDelay.getText().toString();
                        widgetModify.setTextColor(0xffff0000);
                        if (clickDelay.isEmpty()){
                            widgetModify.setText("延迟点击不能为空");
                            return;
                        }else if (Integer.valueOf(clickDelay)>4000){
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
                        widgetModify.setText(new SimpleDateFormat("HH:mm:ss a", Locale.ENGLISH).format(new Date()) + "(修改成功)");
                    }
                });
                layoutWidget.addView(viewWidget);
            }
        }
    }
}
