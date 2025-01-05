package com.lgh.tapclick.mybean;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index(value = "id", unique = true))
public class MyAppConfig {
    @PrimaryKey
    public Integer id;
    public boolean autoHideOnTaskList;
    public String forUpdate;
    public boolean isVip;

    public MyAppConfig() {
        this.id = 0;
        this.autoHideOnTaskList = true;
        this.forUpdate = "";
        this.isVip = false;
    }
}
