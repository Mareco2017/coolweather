package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    /**
     * 发起一条网络请求的方法  静态方法  需要条件:
     * @param address 请求的 URL
     * @param callback 注册一个回调来处理服务器响应
     */
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        // 创建一个 OkHttpClient 的实例
        OkHttpClient client = new OkHttpClient();
        // 创建一个Request对象
        Request request = new Request.Builder().url(address).build();
        // 发送请求并获取服务器返回的数据
        client.newCall(request).enqueue(callback);
    }

}
