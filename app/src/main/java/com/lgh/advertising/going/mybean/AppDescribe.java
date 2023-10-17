package com.lgh.advertising.going.mybean;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lgh.advertising.going.myclass.DataDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity(indices = @Index(value = "appPackage", unique = true))
public class AppDescribe {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public String appName;
    public String appPackage;
    public boolean onOff;
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
    public transient AutoFinder autoFinder;
    @Ignore
    public transient Map<String, Coordinate> coordinateMap;
    @Ignore
    public transient Map<String, Set<Widget>> widgetSetMap;
    @Ignore
    public transient List<Coordinate> coordinateList;
    @Ignore
    public transient List<Widget> widgetList;

    public AppDescribe() {
        this.appName = "";
        this.appPackage = "";
        this.onOff = true;
        this.autoFinderRetrieveTime = 10000;
        this.autoFinderRetrieveAllTime = false;
        this.coordinateRetrieveTime = 20000;
        this.coordinateRetrieveAllTime = false;
        this.widgetRetrieveTime = 20000;
        this.widgetRetrieveAllTime = false;
        this.autoFinderOnOFF = true;
        this.coordinateOnOff = true;
        this.widgetOnOff = true;
        this.autoFinder = new AutoFinder();
        this.coordinateMap = new HashMap<>();
        this.widgetSetMap = new HashMap<>();
        this.coordinateList = new ArrayList<>();
        this.widgetList = new ArrayList<>();
    }

    public void copy(AppDescribe appDescribe) {
        this.appName = appDescribe.appName;
        this.appPackage = appDescribe.appPackage;
        this.onOff = appDescribe.onOff;
        this.autoFinderRetrieveTime = appDescribe.autoFinderRetrieveTime;
        this.autoFinderRetrieveAllTime = appDescribe.autoFinderRetrieveAllTime;
        this.coordinateRetrieveTime = appDescribe.coordinateRetrieveTime;
        this.coordinateRetrieveAllTime = appDescribe.coordinateRetrieveAllTime;
        this.widgetRetrieveTime = appDescribe.widgetRetrieveTime;
        this.widgetRetrieveAllTime = appDescribe.widgetRetrieveAllTime;
        this.autoFinderOnOFF = appDescribe.autoFinderOnOFF;
        this.coordinateOnOff = appDescribe.coordinateOnOff;
        this.widgetOnOff = appDescribe.widgetOnOff;
        this.autoFinder = appDescribe.autoFinder;
        this.coordinateMap = appDescribe.coordinateMap;
        this.widgetSetMap = appDescribe.widgetSetMap;
        this.coordinateList = appDescribe.coordinateList;
        this.widgetList = appDescribe.widgetList;
    }

    public void getOtherFieldsFromDatabase(DataDao dataDao) {
        getAutoFinderFromDatabase(dataDao);
        getCoordinateFromDatabase(dataDao);
        getWidgetFromDatabase(dataDao);
    }

    public void getAutoFinderFromDatabase(DataDao dataDao) {
        AutoFinder autoFinderGet = dataDao.getAutoFinderByPackage(this.appPackage);
        autoFinder = autoFinderGet != null ? autoFinderGet : autoFinder;
    }

    public void getCoordinateFromDatabase(DataDao dataDao) {
        coordinateMap.clear();
        coordinateList.clear();
        coordinateList.addAll(dataDao.getCoordinatesByPackage(this.appPackage));
        for (Coordinate e : coordinateList) {
            coordinateMap.put(e.appActivity, e);
        }
    }

    public void getWidgetFromDatabase(DataDao dataDao) {
        widgetSetMap.clear();
        widgetList.clear();
        widgetList.addAll(dataDao.getWidgetsByPackage(this.appPackage));
        for (Widget w : widgetList) {
            Set<Widget> widgetSet = this.widgetSetMap.get(w.appActivity);
            if (widgetSet == null) {
                widgetSet = new HashSet<>();
                widgetSet.add(w);
                widgetSetMap.put(w.appActivity, widgetSet);
            } else {
                widgetSet.add(w);
            }
        }
    }

    @Override
    public String toString() {
        return "AppDescribe{" +
                "id=" + id +
                ", appName='" + appName + '\'' +
                ", appPackage='" + appPackage + '\'' +
                ", onOff=" + onOff +
                ", autoFinderRetrieveTime=" + autoFinderRetrieveTime +
                ", autoFinderRetrieveAllTime=" + autoFinderRetrieveAllTime +
                ", coordinateRetrieveTime=" + coordinateRetrieveTime +
                ", coordinateRetrieveAllTime=" + coordinateRetrieveAllTime +
                ", widgetRetrieveTime=" + widgetRetrieveTime +
                ", widgetRetrieveAllTime=" + widgetRetrieveAllTime +
                ", autoFinderOnOFF=" + autoFinderOnOFF +
                ", coordinateOnOff=" + coordinateOnOff +
                ", widgetOnOff=" + widgetOnOff +
                ", autoFinder=" + autoFinder +
                ", coordinateMap=" + coordinateMap +
                ", widgetSetMap=" + widgetSetMap +
                ", coordinateList=" + coordinateList +
                ", widgetList=" + widgetList +
                '}';
    }
}
