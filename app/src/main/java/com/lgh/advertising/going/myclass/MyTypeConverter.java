package com.lgh.advertising.going.myclass;

import android.graphics.Rect;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class MyTypeConverter {
    @TypeConverter
    public static List<String> JsonToStringList(String json) {
        return new Gson().fromJson(json, new TypeToken<List<String>>() {
        }.getType());
    }

    @TypeConverter
    public static String StringListToJson(List<String> stringList) {
        return new Gson().toJson(stringList);
    }

    @TypeConverter
    public static Rect JsonToRect(String json) {
        return new Gson().fromJson(json, Rect.class);
    }

    @TypeConverter
    public static String RectToJson(Rect rect) {
        return new Gson().toJson(rect);
    }
}
