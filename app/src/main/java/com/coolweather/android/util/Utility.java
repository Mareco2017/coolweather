package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析和处理 JSON 格式的数据 的工具类
 */
public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     * 需要条件：请求回来的省份数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty( response )){
            try {
                JSONArray allProvinces = new JSONArray( response );
                for (int i = 0;i < allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject( i );
                    Province province = new Province();
                    province.setProvinceName( provinceObject.getString( "name" ) );
                    province.setProvinceCode( provinceObject.getInt( "id" ) );
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }



    /**
     * 解析和处理服务器返回的市级数据
     * 需要条件：请求回来的城市的天气数据  城市对应的省份id
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty( response )){
            try {
                JSONArray allCities = new JSONArray( response );
                for (int i = 0;i < allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject( i );
                    City city = new City();
                    city.setCityName( cityObject.getString( "name" ) );
                    city.setCityCode( cityObject.getInt( "id" ) );
                    city.setProvinceId( provinceId );
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 解析和处理服务器返回的县级数据
     * 需要条件：请求回来的县级数据   县所对应的城市 id
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty( response )){
            try {
                JSONArray allCounties = new JSONArray( response );
                for (int i = 0;i < allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject( i );
                    County county = new County();
                    county.setCountyName( countyObject.getString( "name" ) );
                    county.setWeatherId( countyObject.getString( "weather_id" ) );
                    county.setCityId( cityId );
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 将返回的 JSON 数据解析成 Weather 实体类
     */
    public static Weather handleWeatherResponse(String response){
        try {
            // 先是通过 JSONObject 和 JSONArray 将天气数据中的主体内容解析出来
            JSONObject jsonObject = new JSONObject( response );
            JSONArray jsonArray = jsonObject.getJSONArray( "HeWeather" );
            String weatherContent = jsonArray.getJSONObject( 0 ).toString();
            
            // 然后由于我们之前已经按照上面的数据格式定义过相应的 GSON 实体类，因此只需要通过调用 fromJson()
            // 方法，就能直接将 JSON 数据转换成 Weather 对象了
            return new Gson().fromJson( weatherContent,Weather.class );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



}
