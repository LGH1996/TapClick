package com.lgh.advertising.going;

import android.content.Intent;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lgh.advertising.myclass.LatestMessage;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String str = "1app-release-444412.apk11d33";
        Matcher pattern = Pattern.compile("\\d+").matcher(str);
        System.out.println(pattern.toString());
    }
}