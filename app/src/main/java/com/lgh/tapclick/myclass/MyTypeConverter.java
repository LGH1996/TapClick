package com.lgh.tapclick.myclass;

import android.graphics.Rect;

import androidx.room.TypeConverter;

import com.google.gson.Gson;

import java.util.UUID;

public class MyTypeConverter {
    @TypeConverter
    public static Rect JsonToRect(String json) {
        if (!json.matches("\\{.*\\}")) {
            return null;
        }
        return new Gson().fromJson(json, Rect.class);
    }

    @TypeConverter
    public static String RectToJson(Rect rect) {
        if (rect == null) {
            return UUID.randomUUID().toString();
        }
        return new Gson().toJson(rect);
    }
}
