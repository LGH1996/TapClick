package com.lgh.tapclick.myclass;

import androidx.room.DeleteColumn;
import androidx.room.migration.AutoMigrationSpec;

public class MyAutoMigrationSpec {
    @DeleteColumn(tableName = "AppDescribe", columnName = "onOff")
    public static class From7To8 implements AutoMigrationSpec {
    }
}
