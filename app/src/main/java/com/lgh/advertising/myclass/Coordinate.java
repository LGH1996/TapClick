package com.lgh.advertising.myclass;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index(value = {"appPackage","appActivity"},unique = true))
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

    public Coordinate() {
        this.appPackage = "";
        this.appActivity = "";
        this.xPosition = 0;
        this.yPosition = 0;
        this.clickDelay = 2000;
        this.clickInterval = 500;
        this.clickNumber = 1;
    }

    public Coordinate(Coordinate coordinate){
        this.appPackage = coordinate.appPackage;
        this.appActivity = coordinate.appActivity;
        this.xPosition = coordinate.xPosition;
        this.yPosition = coordinate.yPosition;
        this.clickDelay = coordinate.clickDelay;
        this.clickInterval = coordinate.clickInterval;
        this.clickNumber = coordinate.clickNumber;
    }
}
