package com.lgh.advertising.myclass;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index(value = "appActivity",unique = true))
public class Coordinate {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public String appPackage;
    public String appActivity;
    public int xPosition;
    public int yPosition;
    public int clickDelay;
    public int clickInterval;
    public int clickNumber;

    public Coordinate(String appPackage, String appActivity, int xPosition, int yPosition, int clickDelay, int clickInterval, int clickNumber) {
        this.appPackage = appPackage;
        this.appActivity = appActivity;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.clickDelay = clickDelay;
        this.clickInterval = clickInterval;
        this.clickNumber = clickNumber;
    }
}
