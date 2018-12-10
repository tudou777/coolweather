package com.tudou.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null)!=null){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }

//        HeConfig.switchToFreeServerNode();//切换和风天气到免费模式
//        HeConfig.init("HE1812071751341905","cce5445a8ddc44a4867a4962c0a1f05b");//注册和风天气时分配的id和key
        /**
         * 调用和风天气接口的demo
         * @author tudou
        HeWeather.getWeatherNow(this, "CN101090501", Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherNowBeanListener() {
                    @Override
                    public void onError(Throwable e) {
                        Log.i("tudou", "onError: ", e);
                    }

                    @Override
                    public void onSuccess(List dataObject) {
                        Log.i("tudou", "onSuccess: " + new Gson().toJson(dataObject));
                    }
                });
        */
    }
}
