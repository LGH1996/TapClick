package com.lgh.tapclick.mybean;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lgh.tapclick.myclass.DataDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity(indices = @Index(value = {"appPackage"}, unique = true))
public class AppDescribe {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    public String appName;
    public String appPackage;
    public int coordinateRetrieveTime;
    public boolean coordinateRetrieveAllTime;
    public int widgetRetrieveTime;
    public boolean widgetRetrieveAllTime;
    public boolean coordinateOnOff;
    public boolean widgetOnOff;
    @Ignore
    public transient Map<String, Set<Coordinate>> coordinateSetMap;
    @Ignore
    public transient Map<String, Set<Widget>> widgetSetMap;
    @Ignore
    public transient List<Coordinate> coordinateList;
    @Ignore
    public transient List<Widget> widgetList;

    public AppDescribe() {
        this.appName = "";
        this.appPackage = "";
        this.coordinateRetrieveTime = 20000;
        this.coordinateRetrieveAllTime = true;
        this.widgetRetrieveTime = 20000;
        this.widgetRetrieveAllTime = true;
        this.coordinateOnOff = false;
        this.widgetOnOff = false;
        this.coordinateSetMap = new HashMap<>();
        this.widgetSetMap = new HashMap<>();
        this.coordinateList = new ArrayList<>();
        this.widgetList = new ArrayList<>();
    }

    public void copy(AppDescribe appDescribe) {
        this.appName = appDescribe.appName;
        this.appPackage = appDescribe.appPackage;
        this.coordinateRetrieveTime = appDescribe.coordinateRetrieveTime;
        this.coordinateRetrieveAllTime = appDescribe.coordinateRetrieveAllTime;
        this.widgetRetrieveTime = appDescribe.widgetRetrieveTime;
        this.widgetRetrieveAllTime = appDescribe.widgetRetrieveAllTime;
        this.coordinateOnOff = appDescribe.coordinateOnOff;
        this.widgetOnOff = appDescribe.widgetOnOff;
        this.coordinateSetMap = appDescribe.coordinateSetMap;
        this.widgetSetMap = appDescribe.widgetSetMap;
        this.coordinateList = appDescribe.coordinateList;
        this.widgetList = appDescribe.widgetList;
    }

    public void getOtherFieldsFromDatabase(DataDao dataDao) {
        getCoordinateFromDatabase(dataDao);
        getWidgetFromDatabase(dataDao);
    }

    public void getCoordinateFromDatabase(DataDao dataDao) {
        coordinateSetMap.clear();
        coordinateList.clear();
        coordinateList.addAll(dataDao.getCoordinatesByPackage(this.appPackage));
        for (Coordinate e : coordinateList) {
            Set<Coordinate> coordinateSet = this.coordinateSetMap.get(e.appActivity);
            if (coordinateSet == null) {
                coordinateSet = new HashSet<>();
                coordinateSetMap.put(e.appActivity, coordinateSet);
            }
            coordinateSet.add(e);
        }
    }

    public void getWidgetFromDatabase(DataDao dataDao) {
        widgetSetMap.clear();
        widgetList.clear();
        widgetList.addAll(dataDao.getWidgetsByPackage(this.appPackage));
        for (Widget e : widgetList) {
            Set<Widget> widgetSet = this.widgetSetMap.get(e.appActivity);
            if (widgetSet == null) {
                widgetSet = new HashSet<>();
                widgetSetMap.put(e.appActivity, widgetSet);
            }
            widgetSet.add(e);
        }
    }

    @Override
    public String toString() {
        return "AppDescribe{" +
                "id=" + id +
                ", appName='" + appName + '\'' +
                ", appPackage='" + appPackage + '\'' +
                ", coordinateRetrieveTime=" + coordinateRetrieveTime +
                ", coordinateRetrieveAllTime=" + coordinateRetrieveAllTime +
                ", widgetRetrieveTime=" + widgetRetrieveTime +
                ", widgetRetrieveAllTime=" + widgetRetrieveAllTime +
                ", coordinateOnOff=" + coordinateOnOff +
                ", widgetOnOff=" + widgetOnOff +
                ", coordinateMap=" + coordinateSetMap +
                ", widgetSetMap=" + widgetSetMap +
                ", coordinateList=" + coordinateList +
                ", widgetList=" + widgetList +
                '}';
    }
}
