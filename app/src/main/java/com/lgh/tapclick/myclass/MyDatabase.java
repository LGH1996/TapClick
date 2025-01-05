package com.lgh.tapclick.myclass;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.mybean.Coordinate;
import com.lgh.tapclick.mybean.MyAppConfig;
import com.lgh.tapclick.mybean.Widget;

@Database(version = 13, entities = {AppDescribe.class, Coordinate.class, Widget.class, MyAppConfig.class}, autoMigrations = {
        @AutoMigration(from = 7, to = 8, spec = MyAutoMigrationSpec.From7To8.class),
        @AutoMigration(from = 8, to = 9, spec = MyAutoMigrationSpec.From8To9.class),
        @AutoMigration(from = 9, to = 10),
        @AutoMigration(from = 10, to = 11),
        @AutoMigration(from = 11, to = 12, spec = MyAutoMigrationSpec.From11To12.class),
        @AutoMigration(from = 12, to = 13),
})
@TypeConverters(MyTypeConverter.class)
public abstract class MyDatabase extends RoomDatabase {
    abstract DataDao dataDao();
}
