package com.lgh.tapclick.myclass;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.mybean.Coordinate;
import com.lgh.tapclick.mybean.MyAppConfig;
import com.lgh.tapclick.mybean.Widget;

@Database(version = 4, entities = {AppDescribe.class, Coordinate.class, Widget.class, MyAppConfig.class}, exportSchema = false)
@TypeConverters(MyTypeConverter.class)
public abstract class MyDatabase extends RoomDatabase {
    abstract DataDao dataDao();
}
