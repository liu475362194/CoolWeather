package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 和服务器交互
 * Created by pzbz025 on 2017/8/11.
 */

public class HttpUtil {
    /**
     * 和服务器交互，并注册一个回调来处理服务器响应
     * @param address
     * @param callback
     */
    public static void sendOkHttpRequest(final String address, final okhttp3.Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(address).build();
                client.newCall(request).enqueue(callback);
            }
        }).start();
    }
}
