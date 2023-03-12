package com.lgh.advertising.going.myclass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.mybean.AutoFinder;
import com.lgh.advertising.going.mybean.Coordinate;
import com.lgh.advertising.going.mybean.MyAppConfig;
import com.lgh.advertising.going.mybean.Widget;

import java.util.List;

@Dao
public interface DataDao {

    @Query("SELECT * FROM AppDescribe")
    List<AppDescribe> getAllAppDescribes();

    @Query("SELECT * FROM AppDescribe WHERE appPackage = :appPackage")
    AppDescribe getAppDescribeByPackage(String appPackage);

    @Query("SELECT * FROM AutoFinder WHERE appPackage = :appPackage")
    AutoFinder getAutoFinderByPackage(String appPackage);

    @Query("SELECT * FROM Coordinate WHERE appPackage = :appPackage")
    List<Coordinate> getCoordinatesByPackage(String appPackage);

    @Query("SELECT * FROM Widget WHERE appPackage = :appPackage")
    List<Widget> getWidgetsByPackage(String appPackage);

    @Query("SELECT * FROM MyAppConfig WHERE id = 0")
    MyAppConfig getMyAppConfig();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAppDescribe(AppDescribe... appDescribes);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAppDescribe(List<AppDescribe> appDescribes);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAutoFinder(AutoFinder... autoFinders);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAutoFinder(List<AutoFinder> autoFinders);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCoordinate(Coordinate... coordinates);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWidget(Widget... widgets);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMyAppConfig(MyAppConfig myAppConfig);

    @Delete
    void deleteCoordinate(Coordinate... coordinates);

    @Delete
    void deleteWidget(Widget... widgets);

    @Update
    void updateCoordinate(Coordinate... coordinates);

    @Update
    void updateWidget(Widget... widgets);

    @Update
    void updateAutoFinder(AutoFinder... autoFinders);

    @Update
    void updateAppDescribe(AppDescribe... appDescribe);

    @Update
    void updateMyAppConfig(MyAppConfig myAppConfig);
}
