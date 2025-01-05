package com.lgh.tapclick.mybean;

import android.graphics.Rect;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(indices = @Index(value = {"id"}, unique = true))
public class Widget {
    public static final int ACTION_CLICK = 0;
    public static final int ACTION_BACK = 1;
    public static final int CONDITION_OR = 0;
    public static final int CONDITION_AND = 1;

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public String appPackage;
    public String appActivity;
    public long createTime;
    public int clickDelay;
    public int debounceDelay;
    public boolean noRepeat;
    public boolean clickOnly;
    public boolean widgetClickable;
    public Rect widgetRect;
    public Long widgetNodeId;
    public String widgetViewId;
    public String widgetDescribe;
    public String widgetText;
    public String comment;
    public String triggerReason;
    public long lastTriggerTime;
    public int triggerCount;
    public int clickInterval;
    public int clickNumber;
    public int action;
    public int condition;

    public Widget() {
        this.appPackage = "";
        this.appActivity = "";
        this.clickNumber = 1;
        this.clickInterval = 0;
        this.clickDelay = 0;
        this.debounceDelay = 0;
        this.noRepeat = false;
        this.clickOnly = false;
        this.widgetClickable = false;
        this.widgetRect = null;
        this.widgetNodeId = null;
        this.widgetViewId = "";
        this.widgetDescribe = "";
        this.widgetText = "";
        this.comment = "";
        this.triggerReason = "";
        this.lastTriggerTime = 0;
        this.triggerCount = 0;
        this.action = ACTION_CLICK;
        this.condition = CONDITION_OR;
        this.createTime = System.currentTimeMillis();
    }

    public Widget(Widget widget) {
        this.appPackage = widget.appPackage;
        this.appActivity = widget.appActivity;
        this.createTime = widget.createTime;
        this.clickNumber = widget.clickNumber;
        this.clickInterval = widget.clickInterval;
        this.clickDelay = widget.clickDelay;
        this.debounceDelay = widget.debounceDelay;
        this.noRepeat = widget.noRepeat;
        this.clickOnly = widget.clickOnly;
        this.widgetClickable = widget.widgetClickable;
        this.widgetRect = widget.widgetRect;
        this.widgetNodeId = widget.widgetNodeId;
        this.widgetViewId = widget.widgetViewId;
        this.widgetDescribe = widget.widgetDescribe;
        this.widgetText = widget.widgetText;
        this.comment = widget.comment;
        this.lastTriggerTime = widget.lastTriggerTime;
        this.triggerCount = widget.triggerCount;
        this.action = widget.action;
        this.condition = widget.condition;
        this.triggerReason = widget.triggerReason;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof Widget)) return false;
        Widget widget = (Widget) obj;
        return Objects.equals(this.appPackage, widget.appPackage)
                && Objects.equals(this.appActivity, widget.appActivity)
                && Objects.equals(this.widgetRect, widget.widgetRect)
                && Objects.equals(this.widgetNodeId, widget.widgetNodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.appPackage, this.appActivity, this.widgetRect, this.widgetNodeId);
    }

    @Override
    public String toString() {
        return "Widget{" +
                "id=" + id +
                ", appPackage='" + appPackage + '\'' +
                ", appActivity='" + appActivity + '\'' +
                ", createTime=" + createTime +
                ", clickDelay=" + clickDelay +
                ", debounceDelay=" + debounceDelay +
                ", noRepeat=" + noRepeat +
                ", clickOnly=" + clickOnly +
                ", widgetClickable=" + widgetClickable +
                ", widgetRect=" + widgetRect +
                ", widgetNodeId=" + widgetNodeId +
                ", widgetViewId='" + widgetViewId + '\'' +
                ", widgetDescribe='" + widgetDescribe + '\'' +
                ", widgetText='" + widgetText + '\'' +
                ", comment='" + comment + '\'' +
                ", triggerReason='" + triggerReason + '\'' +
                ", lastTriggerTime=" + lastTriggerTime +
                ", triggerCount=" + triggerCount +
                ", clickInterval=" + clickInterval +
                ", clickNumber=" + clickNumber +
                ", action=" + action +
                ", condition=" + condition +
                '}';
    }
}
