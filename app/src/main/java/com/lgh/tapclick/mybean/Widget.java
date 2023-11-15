package com.lgh.tapclick.mybean;

import android.graphics.Rect;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(indices = @Index(value = {"appPackage", "appActivity", "widgetRect"}, unique = true))
public class Widget {
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
    public long lastClickTime;
    public int clickCount;

    public Widget() {
        this.appPackage = "";
        this.appActivity = "";
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
        this.lastClickTime = 0;
        this.clickCount = 0;
    }

    public Widget(Widget widget) {
        this.createTime = widget.createTime;
        this.appPackage = widget.appPackage;
        this.appActivity = widget.appActivity;
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
        this.lastClickTime = widget.lastClickTime;
        this.clickCount = widget.clickCount;
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
                ", lastClickTime=" + lastClickTime +
                ", clickCount=" + clickCount +
                '}';
    }
}
