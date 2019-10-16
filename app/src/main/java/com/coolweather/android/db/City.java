package com.coolweather.android.db;
import org.litepal.crud.LitePalSupport;

/**
 * City城市---对应数据库中的城市表
 */
public class City extends LitePalSupport {

    private int id;
    private String cityName;    // 城市名字
    private int cityCode;   // 城市代号
    private int provinceId; //所属省份的 id 值

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCityName() {
        return cityName;
    }
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    public int getCityCode() {
        return cityCode;
    }
    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }
    public int getProvinceId() {
        return provinceId;
    }
    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
