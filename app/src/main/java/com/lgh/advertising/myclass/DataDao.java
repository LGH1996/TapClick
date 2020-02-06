package com.lgh.advertising.myclass;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

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

}
