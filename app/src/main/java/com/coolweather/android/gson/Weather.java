package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 总的实体类，引用刚刚创建的各个实体类
 */
public class Weather {
    public String status;   // 还包含一项 status 数据
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName( "daily_forecast" )
    public List<Forecast> forecastList;
    /**
     * 由于 daily_forecast 中包含的是一个数组，因此这里使用了 List 集合来引用 Forecast 类
     */
}
