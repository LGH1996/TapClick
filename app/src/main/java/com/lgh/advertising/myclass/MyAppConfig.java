package com.lgh.advertising.myclass;

import android.content.Intent;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index("id"))
public class MyAppConfig {
    @PrimaryKey
    public Integer id;
    public boolean autoHideOnTaskList;
    public String  forUpdate;

    public MyAppConfig() {
        this.id = 0;
        this.autoHideOnTaskList = false;
        this.forUpdate = "";
    }
}
