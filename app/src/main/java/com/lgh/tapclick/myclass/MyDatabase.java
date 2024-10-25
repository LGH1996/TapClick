package com.lgh.tapclick.myclass;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.mybean.Coordinate;
import com.lgh.tapclick.mybean.MyAppConfig;
import com.lgh.tapclick.mybean.Widget;

@Database(version = 9, entities = {AppDescribe.class, Coordinate.class, Widget.class, MyAppConfig.class}, autoMigrations = {
        @AutoMigration(from = 7, to = 8, spec = MyAutoMigrationSpec.From7To8.class),
        @AutoMigration(from = 8, to = 9, spec = MyAutoMigrationSpec.From8To9.class),
})
@TypeConverters(MyTypeConverter.class)
public abstract class MyDatabase extends RoomDatabase {
    abstract DataDao dataDao();
}
