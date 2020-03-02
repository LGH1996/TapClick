package com.lgh.advertising.going;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect()throws Throwable {
//        URL url = new URL("https://api.github.com/repos/lgh1996/UPDATEADGO/contents/a.pdf?access_token=83bf0df4f89ad8b3620a27ae90327ee5003a4601");
//        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
//        httpsURLConnection.setUseCaches(false);
//        httpsURLConnection.setRequestMethod("PUT");
//        httpsURLConnection.setDoOutput(true);
//        httpsURLConnection.setDoInput(true);
//        httpsURLConnection.connect();
//        OutputStreamWriter writer = new OutputStreamWriter(httpsURLConnection.getOutputStream());
//        PostMessage postMessage = new PostMessage();
//        postMessage.message = "TEST";
//
//        postMessage.content = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("/home/lgh/桌面/简历.pdf")));
//        writer.write(new Gson().toJson(postMessage));
//        writer.close();
//        Scanner scanner = new Scanner(httpsURLConnection.getInputStream());
//        while (scanner.hasNextLine()){
//            System.out.println(scanner.nextLine());
//        }
        String activityName = "android.widget.LinearLayout";
        boolean isActivity = !activityName.startsWith("android.widget.");
        System.out.println(isActivity);
    }

    class PostMessage{
        String message;
        String content;
    }
}