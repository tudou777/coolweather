package com.tudou.coolweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tudou.coolweather.db.City;
import com.tudou.coolweather.db.County;
import com.tudou.coolweather.db.Province;
import com.tudou.coolweather.gson.Basic;
import com.tudou.coolweather.gson.Weather;
import com.tudou.coolweather.util.HttpUtil;
import com.tudou.coolweather.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        LitePal.deleteAll("Province","");
//        LitePal.deleteAll("City","");
//        LitePal.deleteAll("County","");
        View view=inflater.inflate(R.layout.choose_area,container,false);//获取fragment的view
        //初始化控件
        titleText=view.findViewById(R.id.title_text);
        backButton=view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);//用来填充数据的adapter
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//listview中每一项的点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){//当前为省信息，加载城市
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }
                else if(currentLevel==LEVEL_CITY)//当前为城市信息，加载县区
                {
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
                else if(currentLevel==LEVEL_COUNTY){
                    selectedCounty=countyList.get(position);
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_Id", selectedCounty.getWeatherId());
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity)
                    {
                        WeatherActivity activity=(WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(selectedCounty.getWeatherId());
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener(){//回退按钮事件
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){//从县区回退到城市
                    queryCities();
                }
                else if(currentLevel==LEVEL_CITY)//从城市回退到省，省无法回退
                {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 加载省信息，先从数据库读，找不到再去网上下载、下载后保存到数据库再读。
     */
    private void queryProvinces(){
        titleText.setText("中国");//设置标题
        backButton.setVisibility(View.GONE);//隐藏回退按钮
        provinceList= LitePal.findAll(Province.class);//从数据库获取省份数据
        if(provinceList.size()>0)
        {
            dataList.clear();
            for(Province province:provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//通知数据有改变，刷新
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;//设置当前级别为省份

        }
        else//数据库中没有省份数据
        {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 加载城市信息，先从数据库读，找不到再去网上下载、下载后保存到数据库再读。
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());//设置标题
        backButton.setVisibility(View.VISIBLE);//显示回退按钮
        cityList= LitePal.where("provinceId = ?",String.valueOf(selectedProvince.getId())).find(City.class);//从数据库获取城市数据
        if(cityList.size()>0)
        {
            dataList.clear();
            for(City city:cityList)
            {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();//通知数据有改变，刷新
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;//设置当前级别为城市

        }
        else//数据库中没有城市数据
        {
            String address="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromServer(address,"city");
        }
    }

    /**
     * 加载城镇信息，先从数据库读，找不到再去网上下载、下载后保存到数据库再读。
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());//设置标题
        backButton.setVisibility(View.VISIBLE);//显示回退按钮
        countyList= LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);//从数据库获取城镇数据
        if(countyList.size()>0)
        {
            dataList.clear();
            for(County county:countyList)
            {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();//通知数据有改变，刷新
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;//设置当前级别为城镇

        }
        else//数据库中没有城镇数据
        {
            String address="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryFromServer(address,"county");
        }
    }
    private void queryFromServer(String address,final String type){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                switch (type)
                {
                    case "province":
                        result=Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                        break;
                    case "county":
                        result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                        break;
                        default:
                            break;
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type)
                            {
                                case "province":
                                    queryProvinces();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }
        });


    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog()
    {
        if(progressDialog==null)
        {
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog()
    {
        if(progressDialog!=null)
        {
            progressDialog.dismiss();
        }
    }

}
