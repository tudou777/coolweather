package com.tudou.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tudou.coolweather.db.City;
import com.tudou.coolweather.db.County;
import com.tudou.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;


//import com.google.gson.JsonArray;

public class Utility {
    /**
     * 从json中解析省份信息（不使用gson），使用LitePal插入数据库
     * @param response json数据
     * @return 是否成功
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response))//非空验证
        {
            try {
                JSONArray jsonArray=new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++)
                {
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return  false;
    }

    /**
     * 从json中解析城市信息（使用gson），使用LitePal插入数据库
     * @param response json数据
     * @return 是否成功
     */
    public static boolean handleCityResponse(String response,int provinceId){
        try{
            if(!TextUtils.isEmpty(response))
            {
                try {
                    JSONArray jsonArray=new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        City city=new City();
                        city.setCityCode(jsonObject.getInt("id"));
                        city.setProvinceId(provinceId);
                        city.setCityName(jsonObject.getString("name"));
                        city.save();
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            Gson gson=new Gson();
//            Type type=new TypeToken<List<City>>(){}.getType();//映射对象类型
//            List<City> cityList=gson.fromJson(response,type);
//            for (City city:cityList)
//            {
//                city.setProvinceId(provinceId);
//                city.save();
//            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 从json中解析县级信息（使用gson），使用LitePal插入数据库
     * @param response json数据
     * @return 是否成功
     */
    public static boolean handleCountyResponse(String response,int cityId){
        try{
            if(!TextUtils.isEmpty(response))
            {
                try {
                    JSONArray jsonArray=new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        County county=new County();
                        county.setCityId(cityId);
                        county.setCountyName(jsonObject.getString("name"));
                        county.setWeatherId(jsonObject.getString("weather_id"));
                        county.save();
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            Gson gson=new Gson();
//            Type type=new TypeToken<List<County>>(){}.getType();//映射对象类型
//            List<County> countyList=gson.fromJson(response,type);
//            for (County county:countyList)
//            {
//                county.setCityId(cityId);
//                county.save();
//            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
