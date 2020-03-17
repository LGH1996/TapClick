package com.lgh.advertising.myclass;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DataDaoFactory {
    private static DataDao dataDao;

    public static DataDao getInstance(Context context) {
        if (dataDao == null) {
            Migration migration_1_2 = new Migration(1, 2) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE 'Coordinate' ADD 'comment' TEXT");
                    database.execSQL("ALTER TABLE 'Widget' ADD 'comment' TEXT");
                }
            };
            dataDao = Room.databaseBuilder(context, MyDatabase.class, "applicationData.db").addMigrations(migration_1_2).allowMainThreadQueries().build().dataDao();
        }
        return dataDao;
    }
}
