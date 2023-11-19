package com.lgh.tapclick.myclass;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lgh.tapclick.mybean.MyAppConfig;
import com.lgh.tapclick.myfunction.MyUtils;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MyApplication extends Application {

    public static DataDao dataDao;
    public static MyAppConfig myAppConfig;
    public static MyHttpRequest myHttpRequest;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (dataDao == null) {
            Migration migration_1_2 = new Migration(1, 2) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE 'Widget' ADD COLUMN 'lastClickTime' INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE 'Widget' ADD COLUMN 'clickCount' INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE 'Coordinate' ADD COLUMN 'lastClickTime' INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE 'Coordinate' ADD COLUMN 'clickCount' INTEGER NOT NULL DEFAULT 0");
                }
            };
            Migration migration_2_3 = new Migration(2, 3) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE 'Widget' ADD COLUMN 'clickNumber' INTEGER NOT NULL DEFAULT 0");
                    database.execSQL("ALTER TABLE 'Widget' ADD COLUMN 'clickInterval' INTEGER NOT NULL DEFAULT 0");
                }
            };
            dataDao = Room.databaseBuilder(base, MyDatabase.class, "applicationData.db").addMigrations(migration_1_2, migration_2_3).allowMainThreadQueries().build().dataDao();
        }

        if (myAppConfig == null) {
            myAppConfig = dataDao.getMyAppConfig();
            if (myAppConfig == null) {
                myAppConfig = new MyAppConfig();
                dataDao.insertMyAppConfig(myAppConfig);
            }
        }

        if (myHttpRequest == null) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.github.com/").addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(RxJava3CallAdapterFactory.create()).build();
            myHttpRequest = retrofit.create(MyHttpRequest.class);
        }

        MyUtils.init(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyUncaughtExceptionHandler.getInstance(this).run();
    }
}
