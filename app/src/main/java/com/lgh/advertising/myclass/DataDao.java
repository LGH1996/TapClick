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
    List<AppDescribe> getAppDescribes();
    @Query("SELECT * FROM AutoFinder WHERE appPackage = :appPackage")
    AutoFinder getAutoFinder(String appPackage);
    @Query("SELECT * FROM Coordinate WHERE appPackage = :appPackage")
    List<Coordinate> getCoordinates(String appPackage);
    @Query("SELECT * FROM Widget WHERE appPackage = :appPackage")
    List<Widget> getWidgets(String appPackage);
    @Query("DELETE FROM AppDescribe WHERE appPackage NOT IN (:packageNames)")
    void deleteAppDescribeByNotIn(Set<String> packageNames);
    @Query("DELETE FROM AppDescribe WHERE appPackage IN (:packageNames)")
    void deleteAppDescribeByPackageNames(String... packageNames);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAppDescribe(AppDescribe... appDescribes);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAppDescribe(List<AppDescribe> appDescribes);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAutoFinder(AutoFinder... autoFinders);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAutoFinder(List<AutoFinder> autoFinders);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCoordinate(Coordinate... coordinates);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWidget(Widget... widgets);
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
