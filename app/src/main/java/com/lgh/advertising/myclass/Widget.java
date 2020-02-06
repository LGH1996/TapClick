package com.lgh.advertising.myclass;

import android.graphics.Rect;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(indices = @Index(value = {"appActivity","widgetRect"},unique = true))
public class Widget {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public String appPackage;
    public String appActivity;
    public int clickDelay;
    public boolean clickOnly;
    public boolean widgetClickable;
    public Rect widgetRect;
    public String widgetClass;
    public String widgetId;
    public String widgetDescribe;
    public String widgetText;

    public Widget(){
        this.appPackage = "";
        this.appActivity = "";
        this.clickDelay = 0;
        this.clickOnly = false;
        this.widgetClickable = false;
        this.widgetRect = null;
        this.widgetClass = "";
        this.widgetId = "";
        this.widgetDescribe = "";
        this.widgetText = "";
    }
    public Widget(Widget widget) {
        this.appPackage = widget.appPackage;
        this.appActivity = widget.appActivity;
        this.clickDelay = widget.clickDelay;
        this.clickOnly = widget.clickOnly;
        this.widgetClickable = widget.widgetClickable;
        this.widgetRect = widget.widgetRect;
        this.widgetClass = widget.widgetClass;
        this.widgetId = widget.widgetId;
        this.widgetDescribe = widget.widgetDescribe;
        this.widgetText = widget.widgetText;
    }
}
