package com.lgh.advertising.going.myclass;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.mybean.AutoFinder;
import com.lgh.advertising.going.mybean.Coordinate;
import com.lgh.advertising.going.mybean.MyAppConfig;
import com.lgh.advertising.going.mybean.Widget;

@Database(version = 5, entities = {AppDescribe.class, AutoFinder.class, Coordinate.class, Widget.class, MyAppConfig.class}, exportSchema = false)
@TypeConverters(MyTypeConverter.class)
public abstract class MyDatabase extends RoomDatabase {
	abstract DataDao dataDao();
}
