package com.coolweather.android.db;
import org.litepal.crud.LitePalSupport;
/**
 * County 县---对应数据库中的县 表
 */
public class County extends LitePalSupport {

    private int id;
    private String countyName;  // 记录县的名字
    private String weatherId;   // 记录县所对应的天气 id
    private int cityId; // 记录当前县所属的城市 id 值

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCountyName() {
        return countyName;
    }
    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }
    public String getWeatherId() {
        return weatherId;
    }
    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }
    public int getCityId() {
        return cityId;
    }
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
