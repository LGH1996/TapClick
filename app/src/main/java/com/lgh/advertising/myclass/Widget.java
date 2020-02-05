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

    public Widget(String appPackage, String appActivity,boolean widgetClickable, int clickDelay, boolean clickOnly, Rect widgetRect, String widgetClass, String widgetId, String widgetDescribe, String widgetText) {
        this.appPackage = appPackage;
        this.appActivity = appActivity;
        this.clickDelay = clickDelay;
        this.clickOnly = clickOnly;
        this.widgetClickable = widgetClickable;
        this.widgetRect = widgetRect;
        this.widgetClass = widgetClass;
        this.widgetId = widgetId;
        this.widgetDescribe = widgetDescribe;
        this.widgetText = widgetText;
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
