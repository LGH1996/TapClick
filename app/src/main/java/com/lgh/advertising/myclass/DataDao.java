package com.lgh.advertising.myclass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.Set;

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMyAppConfig(MyAppConfig myAppConfig);

    @Query("DELETE FROM AppDescribe WHERE appPackage NOT IN (:appPackages)")
    void deleteAppDescribeByNotIn(Set<String> appPackages);

    @Query("DELETE FROM AppDescribe WHERE appPackage = :appPackage")
    void deleteAppDescribeByPackage(String appPackage);

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
}
