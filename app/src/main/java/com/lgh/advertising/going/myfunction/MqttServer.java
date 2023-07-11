package com.lgh.advertising.going.myfunction;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * CreateDate   2018/11/08
 * Desc         ${MQTT服务}
 */

public class MqttServer extends Service {

    public final String TAG = MqttServer.class.getSimpleName();
    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    public String HOST = "ssl://e038ef11.ala.cn-hangzhou.emqxsl.cn:8883";//服务器地址（协议+地址+端口号）
    public String USERNAME = "qwer";//用户名
    public String PASSWORD = "123456";//密码
    public static String PUBLISH_TOPIC = "daKaRe";//发布主题
    public static String RESPONSE_TOPIC = "daKa";//响应主题

    public ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static String CLIENTID = Build.FINGERPRINT;

    private final IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            try {
                Toast.makeText(MqttServer.this, "连接成功", Toast.LENGTH_SHORT).show();
//                mqttAndroidClient.subscribe(PUBLISH_TOPIC, 2, new IMqttMessageListener() {
//                    @Override
//                    public void messageArrived(String topic, MqttMessage message) {
//                        Toast.makeText(MqttServer.this, topic + ":" + new String(message.getPayload()), Toast.LENGTH_SHORT).show();
//                    }
//                });
                mqttAndroidClient.subscribe(RESPONSE_TOPIC + "/#", 2,this, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i("LinGH", "subscribe onSuccess");
//                        Toast.makeText(MqttServer.this, "subscribe onSuccess", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.i("LinGH", "subscribe onFailure");
//                        Toast.makeText(MqttServer.this, "subscribe onFailure", Toast.LENGTH_SHORT).show();
                    }
                }, new IMqttMessageListener() {
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.i("LinGH", "subscribe messageArrived " + topic + ", " + new String(message.getPayload()));
//                        Toast.makeText(MqttServer.this, "subscribe messageArrived", Toast.LENGTH_SHORT).show();
                    }
                });
                mqttAndroidClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        cause.printStackTrace();
                        Log.i("LinGH", "connectionLost");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.i("LinGH", topic + ", " + new String(message.getPayload()));
//                        Toast.makeText(MqttServer.this, topic + ", " + new String(message.getPayload()), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
//                        Toast.makeText(MqttServer.this, "deliveryComplete", Toast.LENGTH_SHORT).show();
                        Log.i("LinGH", "deliveryComplete");
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            exception.printStackTrace();
            Log.i("LinGH", "onFailure "+ exception.getMessage());
//            Toast.makeText(MqttServer.this, "onFailure "+ exception.getMessage(), Toast.LENGTH_SHORT).show();
//            executorService.schedule(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if (!isConnectIsNormal()) {
//                            executorService.schedule(this, 10, TimeUnit.SECONDS);
//                            return;
//                        }
//                        asyncActionToken.getClient().connect(mMqttConnectOptions, iMqttActionListener);
//                    } catch (MqttException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 10, TimeUnit.SECONDS);

        }
    };

    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = getApplicationContext().getSystemService(ConnectivityManager.class);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mMqttConnectOptions = new MqttConnectOptions();
            mMqttConnectOptions.setUserName(USERNAME);
            mMqttConnectOptions.setPassword(PASSWORD.toCharArray());
            mMqttConnectOptions.setConnectionTimeout(30);
            mMqttConnectOptions.setCleanSession(false);
            mMqttConnectOptions.setKeepAliveInterval(60);
            mMqttConnectOptions.setAutomaticReconnect(true);
            mqttAndroidClient = new MqttAndroidClient(this, HOST, CLIENTID);
//            executorService.scheduleAtFixedRate(new Runnable() {
//                @Override
//                public void run() {
//                    if (mqttAndroidClient.isConnected()) {
//                        MqttMessage message = new MqttMessage();
//                        message.setPayload(UUID.randomUUID().toString().getBytes());
//                        message.setQos(2);
//                        message.setRetained(true);
//                        try {
//                            mqttAndroidClient.publish(RESPONSE_TOPIC, message);
//                        } catch (MqttException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//            }, 0, 5, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, this, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            mqttAndroidClient.disconnect(); //断开连接
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}