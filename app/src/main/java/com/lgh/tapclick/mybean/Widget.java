package com.lgh.tapclick.mybean;

import android.graphics.Rect;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(indices = @Index(value = {"appPackage", "appActivity", "widgetRect"}, unique = true))
public class Widget {
    public static final int ACTION_CLICK = 0;
    public static final int ACTION_BACK = 1;

    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public long createTime;
    public String appPackage;
    public String appActivity;
    public int clickDelay;
    public int debounceDelay;
    public boolean noRepeat;
    public boolean clickOnly;
    public boolean widgetClickable;
    public Rect widgetRect;
    public String widgetId;
    public String widgetDescribe;
    public String widgetText;
    public String comment;
    public long lastTriggerTime;
    public int triggerCount;
    public int clickInterval;
    public int clickNumber;
    public int action;

    public Widget() {
        this.appPackage = "";
        this.appActivity = "";
        this.clickNumber = 1;
        this.clickInterval = 500;
        this.clickDelay = 0;
        this.debounceDelay = 0;
        this.noRepeat = false;
        this.clickOnly = false;
        this.widgetClickable = false;
        this.widgetRect = null;
        this.widgetId = "";
        this.widgetDescribe = "";
        this.widgetText = "";
        this.createTime = System.currentTimeMillis();
        this.comment = "";
        this.lastTriggerTime = 0;
        this.triggerCount = 0;
        this.action = 0;
    }

    public Widget(Widget widget) {
        this.createTime = widget.createTime;
        this.appPackage = widget.appPackage;
        this.appActivity = widget.appActivity;
        this.clickNumber = widget.clickNumber;
        this.clickInterval = widget.clickInterval;
        this.clickDelay = widget.clickDelay;
        this.debounceDelay = widget.debounceDelay;
        this.noRepeat = widget.noRepeat;
        this.clickOnly = widget.clickOnly;
        this.widgetClickable = widget.widgetClickable;
        this.widgetRect = widget.widgetRect;
        this.widgetId = widget.widgetId;
        this.widgetDescribe = widget.widgetDescribe;
        this.widgetText = widget.widgetText;
        this.comment = widget.comment;
        this.lastTriggerTime = widget.lastTriggerTime;
        this.triggerCount = widget.triggerCount;
        this.action = widget.action;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof Widget)) return false;
        Widget widget = (Widget) obj;
        return Objects.equals(this.appPackage, widget.appPackage)
                && Objects.equals(this.appActivity, widget.appActivity)
                && Objects.equals(this.widgetRect, widget.widgetRect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.appPackage, this.appActivity, this.widgetRect);
    }

    @Override
    public String toString() {
        return "Widget{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", appPackage='" + appPackage + '\'' +
                ", appActivity='" + appActivity + '\'' +
                ", clickDelay=" + clickDelay +
                ", debounceDelay=" + debounceDelay +
                ", noRepeat=" + noRepeat +
                ", clickOnly=" + clickOnly +
                ", widgetClickable=" + widgetClickable +
                ", widgetRect=" + widgetRect +
                ", widgetId='" + widgetId + '\'' +
                ", widgetDescribe='" + widgetDescribe + '\'' +
                ", widgetText='" + widgetText + '\'' +
                ", comment='" + comment + '\'' +
                ", lastActionTime=" + lastTriggerTime +
                ", actionCount=" + triggerCount +
                ", clickInterval=" + clickInterval +
                ", clickNumber=" + clickNumber +
                ", action=" + action +
                '}';
    }
}
