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
            Migration migration_2_3 = new Migration(2, 3) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE 'MyAppConfig' ADD 'isVip' INTEGER NOT NULL DEFAULT 0");
                }
            };
            dataDao = Room.databaseBuilder(context, MyDatabase.class, "applicationData.db").addMigrations(migration_1_2, migration_2_3).allowMainThreadQueries().build().dataDao();
        }
        return dataDao;
    }
}
