package com.lgh.advertising.going;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        Gson gson = new Gson();
        List<String> stringList = new ArrayList<>();
        stringList.add("a");
        stringList.add("跳过");
        System.out.println(gson.toJson(stringList));
        System.out.println(stringList.toString());
        System.out.println(gson.fromJson(stringList.toString(),new TypeToken<List<String>>(){}.getType()).toString());
        System.out.println(gson.fromJson("   [a,  b     ,解决       ]    ",new TypeToken<List<String>>(){}.getType()).toString());
        System.out.println("[ g，好]".matches("^\\[(\\S+,)*(\\S.+)\\]$"));

    }
}