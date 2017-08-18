package com.coolweather.android;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.bumptech.glide.Glide;
import com.coolweather.android.gson.DailyForecast;
import com.coolweather.android.gson.HeWeather5;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherScroll;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView nowTmpText;

    private TextView nowTxtText;

    private LinearLayout dailyForecastLayout;

    private TextView dailyForecastDate;

    private TextView dailyForecastTxtD;

    private TextView dailyForecastMax;

    private TextView dailyForecastMin;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView suggestionComf;

    private TextView suggestionCw;

    private TextView suggestionDrsg;

    private ImageView bingPicImg;

    private LocationClient locationClient;

    public static final String WEATHER_URL = "https://free-api.heweather.com/v5/weather?key=cd83db70d0ba468a8486a906fee14faf&city=";

    private String weatherId;

    private String localtion;

    private boolean havePreferences = false;

    private static final String TAG = "WeatherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());

        setContentView(R.layout.activity_weather);
        initView();
        initPermission();
        loadBingPic();

        /**
         * 状态栏透明
         */
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void findPreferencesText() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherText = preferences.getString("weather",null);
        String bingPic = preferences.getString("bing_pic",null);
        String dateLast = preferences.getString("date_last",null);

        if (null != bingPic){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
        if (preferences.getBoolean("havePreferences",false)){
            HeWeather5 weather = Utility.handleWeatherResponse(weatherText);
            showWeatherInfo(weather);
            if (!dateLast.equals(weather.dailyForecast.get(0).date.toString())){
                Log.d(TAG, "findPreferencesText: dateLast != weather.dailyForecast.get(0).date" + dateLast + "," + weather.dailyForecast.get(0).date);
                loadBingPic();
            }else{
                Glide.with(this).load(bingPic).into(bingPicImg);
            }
        } else {
            requestLocation();
        }
    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        Log.d(TAG, "loadBingPic: ");
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseTxt = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",responseTxt);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(responseTxt).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 获取多个权限
     */
    private void initPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this, permissions, 1);

        } else {
            findPreferencesText();
        }
    }

//    百度地图位置监听
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {

            //.getLatitude();//经度
            //bdLocation.getLongitude();//纬度
            localtion = bdLocation.getLatitude() + "," + bdLocation.getLongitude();
            Log.d(TAG, "onReceiveLocation: localtion" + localtion);
            requestWeather();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    /**
     * 查询地址
     */
    private void requestLocation() {
        locationClient.start();
    }

    /**
     * 根据weatherId网络查询天气
     */
    private void requestWeather() {
        weatherId = getIntent().getStringExtra("weatherId");
        HttpUtil.sendOkHttpRequest(WEATHER_URL + localtion, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseTxt = response.body().string();
                final HeWeather5 weather = Utility.handleWeatherResponse(responseTxt);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if (weather != null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather",responseTxt);
                        editor.putBoolean("havePreferences",true);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                        if (null == preferences.getString("date_last",null)) {
                            Log.d(TAG, "run: first add date_last");
                            editor.putString("date_last", weather.dailyForecast.get(0).date);
                        }
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                    }
                    }
                });

            }
        });
    }

    /**
     * 初始化所有组件
     */
    private void initView(){
        weatherScroll = (ScrollView) findViewById(R.id.weather_scroll_view);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        nowTmpText = (TextView) findViewById(R.id.now_tmp_text);
        nowTxtText = (TextView) findViewById(R.id.now_txt_text);
        dailyForecastLayout = (LinearLayout) findViewById(R.id.daily_forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_aqi_text);
        pm25Text = (TextView) findViewById(R.id.aqi_pm25_text);
        suggestionComf = (TextView) findViewById(R.id.suggestion_comf_text);
        suggestionCw = (TextView) findViewById(R.id.suggestion_cw_text);
        suggestionDrsg = (TextView) findViewById(R.id.suggestion_drsg_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
    }

    /**
     * 更新所有组件内容
     * @param weather
     */
    private void showWeatherInfo(HeWeather5 weather){
        Log.d(TAG, "showWeatherInfo: ");
        titleCity.setText(weather.basic.city);
        titleUpdateTime.setText(weather.basic.update.loc);
        nowTmpText.setText(weather.now.tmp);
        nowTxtText.setText(weather.now.cond.txt);
        for (DailyForecast dailyForecast : weather.dailyForecast){
            View view = LayoutInflater.from(this).inflate(R.layout.daily_forecast_item,dailyForecastLayout,false);
            dailyForecastDate = view.findViewById(R.id.daily_forecast_date);
            dailyForecastMax = view.findViewById(R.id.daily_forecast_max);
            dailyForecastMin = view.findViewById(R.id.daily_forecast_min);
            dailyForecastTxtD = view.findViewById(R.id.daily_forecast_txt_d);
            dailyForecastDate.setText(dailyForecast.date);
            dailyForecastMax.setText(dailyForecast.tmp.max);
            dailyForecastMin.setText(dailyForecast.tmp.min);
            dailyForecastTxtD.setText(dailyForecast.cond.txt_d);
            dailyForecastLayout.addView(view);
        }
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        suggestionComf.setText("舒适度：" + weather.suggestion.comf.txt);
        suggestionCw.setText("洗车指数：" + weather.suggestion.cw.txt);
        suggestionDrsg.setText("感觉：" + weather.suggestion.drsg.txt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(WeatherActivity.this, "必须同意全部权限", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    findPreferencesText();
                } else {
                    Toast.makeText(WeatherActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}
