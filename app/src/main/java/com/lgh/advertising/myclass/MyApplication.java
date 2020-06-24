package com.lgh.advertising.myclass;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MyApplication extends Application {

    public static DataDao dataDao;
    public static MyAppConfig myAppConfig;
    public static AppDescribe appDescribe;

    @Override
    public void onCreate() {
        super.onCreate();

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
            Migration migration_3_4 = new Migration(3, 4) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE 'Widget' ADD `noRepeat` INTEGER NOT NULL DEFAULT 0");
                }
            };
            dataDao = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, "applicationData.db").addMigrations(migration_1_2, migration_2_3, migration_3_4).allowMainThreadQueries().build().dataDao();
        }

        if (myAppConfig == null) {
            myAppConfig = dataDao.getMyAppConfig();
        }
    }
    
}
