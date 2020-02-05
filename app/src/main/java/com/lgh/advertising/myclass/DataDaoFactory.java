package com.lgh.advertising.myclass;

import android.content.Context;

import androidx.room.Room;

public class DataDaoFactory {
    private static DataDao dataDao;
    public static DataDao getInstance(Context context){
        if (dataDao == null){
           dataDao = Room.databaseBuilder(context,MyDatabase.class,"applicationData.db").allowMainThreadQueries().build().dataDao();
        }
        return dataDao;
    }
}
