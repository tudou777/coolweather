package com.tudou.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tudou.coolweather.gson.Air;
import com.tudou.coolweather.gson.Weather;
import com.tudou.coolweather.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.LifestyleBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class WeatherActivity extends AppCompatActivity {

   private ScrollView weatherLayout;
   private TextView titleCity;
   private TextView titleUpdateTime;
   private TextView degreeText;
   private TextView weatherInfoText;
   private LinearLayout forecastLayout;
   private TextView aqiText;
   private TextView pm25Text;
   private TextView comfortText;
   private TextView carWashText;
   private TextView sportText;
   private Button chooseArea;
   private ImageView bingPicImg;
   public SwipeRefreshLayout swipeRefreshLayout;
   public DrawerLayout drawerLayout;
   private Button navButton;
    public String weatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置内容与状态栏融合
        if(Build.VERSION.SDK_INT>=21){//android5.0及以上
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化控件
        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        chooseArea=findViewById(R.id.choose_area);
        bingPicImg=findViewById(R.id.bing_pic_img);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        //切换城市
//        chooseArea.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
//                editor.putString("weather",null);
//                editor.apply();
//                Intent intent=new Intent(WeatherActivity.this,MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
        drawerLayout=findViewById(R.id.drawer_layout);
        navButton=findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //有的地区没法检测pm25，需要初始值
        aqiText.setText("NaN");
        pm25Text.setText("NaN");

        //判断缓存中是否有背景图片和地区信息
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null)
        {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }
        else
        {
            loadBingPic();
        }
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            weatherId=weatherString;
            requestWeather(weatherId);
        }
        else
        {
            weatherId= getIntent().getStringExtra("weather_Id");

            requestWeather(weatherId);
        }
        //下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

    }
    public void requestWeather(String weatherId){
        //缓存
        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("weather",weatherId);
        editor.apply();
        //获取天气
        //weatherLayout.setVisibility(View.INVISIBLE);
        HeConfig.switchToFreeServerNode();//切换和风天气到免费模式
        HeConfig.init("HE1812071751341905","cce5445a8ddc44a4867a4962c0a1f05b");//注册和风天气时分配的id和key
        HeWeather.getWeatherNow(getContext(), weatherId, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherNowBeanListener() {
                    @Override
                    public void onError(Throwable e) {
                        Log.i("tudou", "onError: ", e);
                    }

                    @Override
                    public void onSuccess(List dataObject) {
                        String result=new Gson().toJson(dataObject);
                        Type type=new TypeToken<List<Weather>>(){}.getType();//映射对象类型
                        ArrayList<Weather> list=new Gson().fromJson(result,type);
                        Weather weather=list.get(0);
                        // Log.d("tudou",weather.basic.cnty);
                        showWeatherInfo(weather);
                    }
                });
        HeWeather.getAirNow(getContext(), weatherId, new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("tudou", "onError: ",throwable);
            }

            @Override
            public void onSuccess(List<AirNow> list) {
                showAirInfo(list);
            }
        });
        HeWeather.getWeatherForecast(getContext(), weatherId, new HeWeather.OnResultWeatherForecastBeanListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("tudou", "onError: ",throwable);
            }

            @Override
            public void onSuccess(List<Forecast> list) {
                show3Days(list);
            }
        });
        HeWeather.getWeatherLifeStyle(getContext(), weatherId, new HeWeather.OnResultWeatherLifeStyleBeanListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(List<Lifestyle> list) {
                showLife(list);
            }
        });
        swipeRefreshLayout.setRefreshing(false);

        //启动后台服务
        Intent intent =new Intent(WeatherActivity.this,AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 绑定天气基本信息
     * @param weather
     */
    protected  void showWeatherInfo(Weather weather)
    {
        String cityName=weather.basic.location;
        String updateTime=weather.update.loc.split(" ")[1];
        String degree=weather.now.tmp+"℃";
        String weatherInfo=weather.now.cond_txt;//"天气:"+weather.now.cond_txt+"/风力:"+weather.now.wind_sc+"/风向:"+weather.now.wind_dir+"/湿度:"+weather.now.hum;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

    }

    /**
     * 绑定空气质量
     * @param list
     */
    protected void showAirInfo(List<AirNow> list)
    {
        AirNow now=list.get(0);
        aqiText.setText(now.getAir_now_city().getAqi());
        pm25Text.setText(now.getAir_now_city().getPm25());

    }

    /**
     * 绑定最近3天
     * @param list
     */
    protected void show3Days(List<Forecast> list)
    {
        List<ForecastBase> baseList=list.get(0).getDaily_forecast();
        forecastLayout.removeAllViews();
       for(ForecastBase forecase:baseList){
           View view=LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
           TextView dateText=view.findViewById(R.id.date_text);
           TextView infoText=view.findViewById(R.id.info_text);
           TextView maxText=view.findViewById(R.id.max_text);
           TextView minText=view.findViewById(R.id.min_text);
           dateText.setText(forecase.getDate());
           infoText.setText(forecase.getCond_txt_d()+"/"+forecase.getCond_txt_n());
           maxText.setText(forecase.getTmp_max());
           minText.setText(forecase.getTmp_min());
           forecastLayout.addView(view);
       }
    }

    /**
     * 绑定生活建议
     * @param list
     */
   protected void showLife(List<Lifestyle> list)
   {
       comfortText.setText(list.get(0).getLifestyle().get(0).getTxt());
       //weatherLayout.setVisibility(View.VISIBLE);
   }

    /**
     * 绑定背景图片
     */
   private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
       HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               e.printStackTrace();
           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
           }
       });
   }
}
