package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.coolweather.android.WeatherActivity;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 创建一个长期在后台运行的定时任务  服务
 */
public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * 会在每次服务启动的时候调用
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService( ALARM_SERVICE );
        int anHour = 8 * 60 * 60 * 1000;    // 这是 8 小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent( this,AutoUpdateService.class );
        PendingIntent pi = PendingIntent.getService( this,0,i,0 );
        manager.cancel( pi );
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi );
        return super.onStartCommand( intent, flags, startId );
    }


    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        String weatherString = prefs.getString( "weather",null );
        if (weatherString != null){
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse( weatherString );
            final String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=8636011177354ceaa13cae65a65aa3b7";
            HttpUtil.sendOkHttpRequest( weatherUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse( responseText );
                    if (weather != null && "ok".equals( weather.status )){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( AutoUpdateService.this ).edit();
                        editor.putString( "weather",responseText );
                        editor.apply();
                    }
                }
            } );
        }
    }


    /**
     * 更新必应每日一图
     */
    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        // 发送网络请求
        HttpUtil.sendOkHttpRequest( requestBingPic, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bingPic = response.body().string();
                // 缓存
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( AutoUpdateService.this ).edit();
                editor.putString( "bing_pic",bingPic );
                editor.apply();
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        } );
    }




}
