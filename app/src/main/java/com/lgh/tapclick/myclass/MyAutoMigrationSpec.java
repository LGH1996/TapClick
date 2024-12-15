package com.lgh.tapclick.myclass;

import androidx.room.DeleteColumn;
import androidx.room.RenameColumn;
import androidx.room.migration.AutoMigrationSpec;

public class MyAutoMigrationSpec {
    @DeleteColumn(tableName = "AppDescribe", columnName = "onOff")
    public static class From7To8 implements AutoMigrationSpec {
    }

    @RenameColumn(tableName = "Widget", fromColumnName = "widgetId", toColumnName = "widgetViewId")
    public static class From8To9 implements AutoMigrationSpec {
    }

    @DeleteColumn(tableName = "Widget", columnName = "toast")
    @DeleteColumn(tableName = "Coordinate", columnName = "toast")
    public static class From11To12 implements AutoMigrationSpec {
    }
}
