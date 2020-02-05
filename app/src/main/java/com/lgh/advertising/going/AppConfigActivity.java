package com.lgh.advertising.going;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.lgh.advertising.myclass.AppDescribe;
import com.lgh.advertising.myclass.AutoFinder;
import com.lgh.advertising.myclass.Coordinate;
import com.lgh.advertising.myclass.Widget;

import java.util.List;

public class AppConfigActivity extends AppCompatActivity {

    LayoutInflater inflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);
        AppDescribe appDescribe = AppSelectActivity.appDescribe;
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

        LinearLayout layoutCoordinate = findViewById(R.id.coordinate_layout);
        List<Coordinate> coordinateList = appDescribe.coordinateList;
        for (Coordinate e:coordinateList){
            View viewCoordinate = inflater.inflate(R.layout.view_coordinate, null);
            EditText coordinateActivity = viewCoordinate.findViewById(R.id.coordinate_activity);
            EditText coordinateXPosition = viewCoordinate.findViewById(R.id.coordinate_xPosition);
            EditText coordinateYPosition = viewCoordinate.findViewById(R.id.coordinate_yPosition);
            EditText coordinateDelay = viewCoordinate.findViewById(R.id.coordinate_clickDelay);
            EditText coordinateInterval = viewCoordinate.findViewById(R.id.coordinate_ClickInterval);
            EditText coordinateNumber  = viewCoordinate.findViewById(R.id.coordinate_clickNumber);
            TextView coordinateModify = viewCoordinate.findViewById(R.id.coordinate_modify);
            TextView coordinateDelete = viewCoordinate.findViewById(R.id.coordinate_delete);
            TextView coordinateSure = viewCoordinate.findViewById(R.id.coordinate_sure);
            coordinateActivity.setText(e.appActivity);
            coordinateXPosition.setText(String.valueOf(e.xPosition));
            coordinateYPosition.setText(String.valueOf(e.yPosition));
            coordinateDelay.setText(String.valueOf(e.clickDelay));
            coordinateInterval.setText(String.valueOf(e.clickInterval));
            coordinateNumber.setText(String.valueOf(e.clickNumber));
            layoutCoordinate.addView(viewCoordinate);
        }


        LinearLayout layoutWidget = findViewById(R.id.widget_layout);
        List<Widget> widgetList = appDescribe.widgetList;
        for (Widget e:widgetList) {
                View viewWidget = inflater.inflate(R.layout.view_widget, null);
                EditText widgetActivity = viewWidget.findViewById(R.id.widget_activity);
                EditText widgetClickable = viewWidget.findViewById(R.id.widget_clickable);
                EditText widgetRect = viewWidget.findViewById(R.id.widget_rect);
                EditText widgetId = viewWidget.findViewById(R.id.widget_id);
                EditText widgetDescribe = viewWidget.findViewById(R.id.widget_describe);
                EditText widgetText = viewWidget.findViewById(R.id.widget_text);
                CheckBox widgetClickOnly = viewWidget.findViewById(R.id.widget_clickOnly);
                TextView widgetModify = viewWidget.findViewById(R.id.widget_modify);
                TextView widgetDelete = viewWidget.findViewById(R.id.widget_delete);
                TextView widgetSure = viewWidget.findViewById(R.id.widget_sure);
                widgetActivity.setText(e.appActivity);
                widgetClickable.setText(String.valueOf(e.widgetClickable));
                widgetRect.setText(e.widgetRect.toShortString());
                widgetId.setText(e.widgetId);
                widgetDescribe.setText(e.widgetDescribe);
                widgetText.setText(e.widgetText);
                widgetClickOnly.setChecked(e.clickOnly);
                layoutWidget.addView(viewWidget);
        }
    }
}
