package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * GSON 实体类，对应和风天气 API 接口中返回的 basic 字段
 * 其中：
 * city 字段表示：城市名
 * id 字段表示：城市对应的天气 id
 * update 中的 loc 表示：天气的更新时间
 */
public class Basic {

    /**
     * 由于JSON中的一些字段可能不太适合直接作为Java字段来命名，因此这里使用了@SerializedName注解的方式
     * 来让JSON字段和Java字段之间建立映射关系
     */
    @SerializedName( "city" )
    public String cityName;

    @SerializedName( "id" )
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName( "loc" )
        public String updateTime;
    }

}
