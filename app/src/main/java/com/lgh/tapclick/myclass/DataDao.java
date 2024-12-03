package com.lgh.tapclick.myclass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.mybean.Coordinate;
import com.lgh.tapclick.mybean.MyAppConfig;
import com.lgh.tapclick.mybean.Widget;

import java.util.List;

@Dao
public interface DataDao {

    @Query("SELECT * FROM AppDescribe")
    List<AppDescribe> getAllAppDescribes();

    @Query("SELECT * FROM AppDescribe WHERE appPackage = :appPackage")
    AppDescribe getAppDescribeByPackage(String appPackage);

    @Query("SELECT * FROM Coordinate WHERE appPackage = :appPackage")
    List<Coordinate> getCoordinatesByPackage(String appPackage);

    @Query("SELECT * FROM Widget WHERE appPackage = :appPackage")
    List<Widget> getWidgetsByPackage(String appPackage);

    @Query("SELECT * FROM MyAppConfig WHERE id = 0")
    MyAppConfig getMyAppConfig();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertAppDescribe(AppDescribe appDescribe);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertCoordinate(Coordinate coordinate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertWidget(Widget widget);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertMyAppConfig(MyAppConfig myAppConfig);

    @Delete
    void deleteCoordinate(Coordinate coordinate);

    @Delete
    void deleteWidget(Widget widget);

    @Delete
    void deleteAppDescribes(List<AppDescribe> appDescribes);

    @Delete
    void deleteCoordinates(List<Coordinate> coordinates);

    @Delete
    void deleteWidgets(List<Widget> widgets);

    @Update
    void updateAppDescribe(AppDescribe appDescribe);

    @Update
    void updateCoordinate(Coordinate coordinate);

    @Update
    void updateWidget(Widget widget);

    @Update
    void updateMyAppConfig(MyAppConfig myAppConfig);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAppDescribes(List<AppDescribe> appDescribes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCoordinates(List<Coordinate> coordinates);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWidgets(List<Widget> widgets);
}
