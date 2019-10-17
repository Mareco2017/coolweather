package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

public class WeatherActivity extends AppCompatActivity {

    // 主界面部分
    private ScrollView weatherLayout;   // 滚动布局

    // 标题栏部分
    private TextView titleCity;         // 标题
    private TextView titleUpdateTime;   // 更新时间

    // Now部分
    private TextView degreeText;        // 温度
    private TextView weatherInfoText;   // 天气信息

    // Forecast预报信息部分
    private LinearLayout forecastLayout;  // 布局

    // AQI部分
    private TextView aqiText;   // 空气质量文本
    private TextView pm25Text;  // PM2.5文本

    // 生活建议部分
    private TextView comfortText;   // 舒适程度
    private TextView carWashText;   // 洗车指数
    private TextView sportText;     // 运动指数

    private ImageView bingPicImg;   // 必应图片


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        // 实现背景图和状态栏融合
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView( R.layout.activity_weather );

        // 初始化各控件
        weatherLayout = findViewById( R.id.weather_layout );

        titleCity = findViewById( R.id.title_city );
        titleUpdateTime = findViewById( R.id.title_update_time );

        degreeText = findViewById( R.id.degree_text );
        weatherInfoText = findViewById( R.id.weather_info_text );

        forecastLayout = findViewById( R.id.forecast_layout );

        aqiText = findViewById( R.id.aqi_text );
        pm25Text = findViewById( R.id.pm25_text );

        comfortText = findViewById( R.id.comfort_text );
        carWashText = findViewById( R.id.car_wash_text );
        sportText = findViewById( R.id.sport_text );

        bingPicImg = findViewById( R.id.bing_pic_img );

        // 尝试从本地缓存中读取缓存数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        String weatherString = prefs.getString( "weather",null );
        if (weatherString != null){
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse( weatherString );
            showWeatherInfo(weather);
        } else {
            // 无缓存时区服务器查询天气 （第一次肯定是没有缓存的）
            // 从 Intent 中取出天气id
            String weatherId = getIntent().getStringExtra( "weather_id" );
            // 先将 ScrollView 进行隐藏（不然空数据的界面很奇怪）
            weatherLayout.setVisibility( View.INVISIBLE );
            // 从服务器上请求天气数据
            requestWeather(weatherId);
        }

        // 尝试从本地缓存中读取图片
        String bingPic = prefs.getString( "bing_pic",null );
        if (bingPic != null){
            Glide.with( this ).load( bingPic ).into( bingPicImg );
        } else {
            loadBingPic();  // 第一次肯定是没有的
        }

    }


    /**
     * 根据天气 id 请求城市天气信息
     */
    public void requestWeather(final String weatherId){
        // 使用之前申请好的 API Key 拼装出一个接口地址
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=8636011177354ceaa13cae65a65aa3b7";

        // 调用网络请求的方法向该地址发起请求，请求成功服务器会将相应城市的天气信息以 JSON 格式返回
        HttpUtil.sendOkHttpRequest( weatherUrl, new Callback() {
            // 请求成功的回调
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 获取到服务器返回的数据
                final String responseText = response.body().string();
                // 调用 Utility 的 handleWeatherResponse 方法，将返回的 JSON 数据转换成 Weather 对象
                final Weather weather = Utility.handleWeatherResponse( responseText );
                // 将线程切换为主线程
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        // 判断服务器返回的 status 状态是ok，说明请求天气成功了
                        if (weather != null && "ok".equals( weather.status )){
                            // 此时，将返回的数据缓存到 SharedPreference 当中
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( WeatherActivity.this ).edit();
                            editor.putString( "weather",responseText );
                            editor.apply();
                            // 调用 showWeatherInfo() 方法来进行内容显示
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText( WeatherActivity.this, "获取天气信息失败-onResponse", Toast.LENGTH_SHORT ).show();
                        }
                    }
                } );
            }

            // 请求失败的回调
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText( WeatherActivity.this, "获取天气信息失败-onFailure", Toast.LENGTH_SHORT ).show();
                    }
                } );
            }
        } );

        loadBingPic(); // 加载图片
    }


    /**
     * 处理并展示 Weather 实体类中的数据
     * 比较简单：
     * 就是从 Weather 总的实例对象中获取数据，然后显示到相应的控件上。
     */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split( " " )[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;

        titleCity.setText( cityName );
        titleUpdateTime.setText( updateTime );
        degreeText.setText( degree );
        weatherInfoText.setText( weatherInfo );

        // 未来几天天气预报部分，使用一个for循环来处理每天的天气信息，在循环中动态加载 forecast_item 布局
        // 并设置相应的数据，然后添加到父布局当中
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from( this ).inflate( R.layout.forecast_item,forecastLayout,false );

            TextView dateText = view.findViewById( R.id.date_text );
            TextView infoText = view.findViewById( R.id.info_text );
            TextView maxText = view.findViewById( R.id.max_text );
            TextView minText = view.findViewById( R.id.min_text );

            dateText.setText( forecast.date );
            infoText.setText( forecast.more.info );
            maxText.setText( forecast.temperature.max +"℃");
            minText.setText( forecast.temperature.min +"℃");

            forecastLayout.addView( view );
        }

        if (weather.aqi != null){
            aqiText.setText( weather.aqi.city.aqi );
            pm25Text.setText( weather.aqi.city.pm25 );
        }

        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动建议："+weather.suggestion.sport.info;

        comfortText.setText( comfort );
        carWashText.setText( carWash );
        sportText.setText( sport );

        // 设置完了所有的数据，将 ScrollView 重写设置为可见
        weatherLayout.setVisibility( View.VISIBLE );
    }


    /**
     * 加载必应每日一图
     */
    private void loadBingPic(){
        // 添加一个必应图片接口地址
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        // 发送网络请求
        HttpUtil.sendOkHttpRequest( requestBingPic, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                // 缓存
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( WeatherActivity.this ).edit();
                editor.putString( "bing_pic",bingPic );
                editor.apply();
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        // 加载
                        Glide.with( WeatherActivity.this ).load( bingPic ).into( bingPicImg );
                    }
                } );
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        } );
    }


}
