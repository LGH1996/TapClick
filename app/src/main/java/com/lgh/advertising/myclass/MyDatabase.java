package com.lgh.advertising.myclass;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(version = 1, entities = {AppDescribe.class, AutoFinder.class, Coordinate.class, Widget.class, MyAppConfig.class}, exportSchema = false)
@TypeConverters(MyTypeConverter.class)
public abstract class MyDatabase extends RoomDatabase {
    abstract DataDao dataDao();
}
