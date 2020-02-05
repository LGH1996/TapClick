package com.lgh.advertising.myclass;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;
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
    public List<Coordinate> coordinateList;
    @Ignore
    public List<Widget> widgetList;

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
        this.coordinateList = null;
        this.widgetList = null;
    }
}
