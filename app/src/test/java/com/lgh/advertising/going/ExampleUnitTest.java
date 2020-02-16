package com.lgh.advertising.going;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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