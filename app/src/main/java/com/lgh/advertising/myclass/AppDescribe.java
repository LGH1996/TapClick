package com.lgh.advertising.myclass;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity(indices = @Index(value = "appPackage",unique = true))
public class AppDescribe {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public String appName;
    public String appPackage;
    public boolean on_off;
    public int autoFinderRetrieveTime;
    public boolean autoFinderRetrieveAllTime;
    public int coordinateRetrieveTime;
    public boolean coordinateRetrieveAllTime;
    public int widgetRetrieveTime;
    public boolean widgetRetrieveAllTime;
    public boolean autoFinderOnOFF;
    public boolean coordinateOnOff;
    public boolean widgetOnOff;
    @Ignore
    public Drawable appDrawable;
    @Ignore
    public AutoFinder autoFinder;
    @Ignore
    public Map<String,Coordinate> coordinateMap;
    @Ignore
    public Map<String,Set<Widget>> widgetSetMap;

    public AppDescribe() {
        this.appName = "";
        this.appPackage = "";
        this.on_off = true;
        this.autoFinderRetrieveTime = 8000;
        this.autoFinderRetrieveAllTime = false;
        this.coordinateRetrieveTime = 8000;
        this.coordinateRetrieveAllTime = false;
        this.widgetRetrieveTime = 15000;
        this.widgetRetrieveAllTime = false;
        this.autoFinderOnOFF = true;
        this.coordinateOnOff = true;
        this.widgetOnOff = true;
        this.appDrawable = null;
        this.autoFinder = null;
        this.coordinateMap = null;
        this.widgetSetMap = null;
    }

    public void getOtherField(DataDao dataDao){
        this.autoFinder = dataDao.getAutoFinder(this.appPackage);
        this.coordinateMap = new HashMap<>( Maps.uniqueIndex(dataDao.getCoordinates(this.appPackage), new Function<Coordinate, String>() {
            @Override
            public String apply(Coordinate input) {
                return input.appActivity;
            }
        }));
        List<Widget> widgetList = dataDao.getWidgets(this.appPackage);
        this.widgetSetMap = new HashMap<>();
        for (Widget w : widgetList) {
            Set<Widget> widgetSet = this.widgetSetMap.get(w.appActivity);
            if (widgetSet == null) {
                widgetSet = new HashSet<>();
                widgetSet.add(w);
                this.widgetSetMap.put(w.appActivity, widgetSet);
            } else {
                widgetSet.add(w);
            }
        }
    }
}
