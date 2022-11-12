package com.lgh.advertising.going.mybean;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(indices = @Index(value = "appPackage", unique = true))
public class AutoFinder {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public String appPackage;
    public boolean clickOnly;
    public int clickDelay;
    public int retrieveNumber;
    public List<String> keywordList;

    public AutoFinder() {
        this.appPackage = "";
        this.clickOnly = false;
        this.clickDelay = 0;
        this.retrieveNumber = 1;
        this.keywordList = new ArrayList<>();
    }
}
