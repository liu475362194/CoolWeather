package com.coolweather.android.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
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


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.coolweather.android.R;
import com.coolweather.android.db.SaveCity;
import com.coolweather.android.gson.Weather5;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView weatherScroll;

    private SwipeRefreshLayout swipeRefresh;

    private TextView titleCity;

//    private TextView titleUpdateTime;

    private ImageView chooseCity;

    private TextView nowTmpText;

    private TextView nowTxtText;

    private LinearLayout dailyForecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

//    private TextView suggestionComf;
//
//    private TextView suggestionCw;
//
//    private TextView suggestionDrsg;

    private LinearLayout suggestionLayout;

    private ImageView bingPicImg;

    //声明AMapLocationClient类对象
    public AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    public static final String WEATHER_URL = "https://free-api.heweather.com/v5/weather?key=cd83db70d0ba468a8486a906fee14faf&city=";

    private String weatherId;

    private String poiName = "";

    private String location;

    private LocalBroadcastManager broadcastManager;

    private LocalReceiver localReceiver;

    private boolean isLocation = true;

    private static final String TAG = "WeatherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Log.d(TAG, "onCreate: ");

        initLocation();
        initView();
        initOnClickListener();
        initPermission();
        loadBingPic();
        updateWeather();

        /**
         * 状态栏透明
         */
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.coolweather.android.weather.id");
        localReceiver = new LocalReceiver();
        broadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
//        String weatherId = getIntent().getStringExtra("weatherId");
//        Log.d(TAG, "onStart: " + weatherId);
//        if (null != weatherId) {
//            location = weatherId;
//            requestWeather();
//
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 初始化组件的点击监听
     */
    private void initOnClickListener() {
        chooseCity.setOnClickListener(this);
        titleCity.setOnClickListener(this);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
//        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
//        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
//        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
//        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 高德定位监听器
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (null != aMapLocation) {
                location = aMapLocation.getLongitude() + "," + aMapLocation.getLatitude();
                poiName = aMapLocation.getPoiName();
                requestWeather();
            }
        }
    };

    /**
     * 寻找缓存的天气数据
     */
    private void findPreferencesText() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherText = preferences.getString("weather", null);
        String bingPic = preferences.getString("bing_pic", null);
        String dateLast = preferences.getString("date_last", null);
        poiName = preferences.getString("poiName", "");

        if (null != bingPic) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
        if (preferences.getBoolean("havePreferences", false)) {
//            HeWeather5 weather = Utility.handleWeatherResponse(weatherText);
            Weather5 weather = new Gson().fromJson(weatherText, Weather5.class);
            showWeatherInfo(weather);
            if (!dateLast.equals(weather.getHeWeather5().get(0).getDaily_forecast().get(0).getDate().toString())) {
                Log.d(TAG, "findPreferencesText: dateLast != weather.dailyForecast.get(0).date" +
                        dateLast + "," + weather.getHeWeather5().get(0).getDaily_forecast().get(0).getDate());
                loadBingPic();
            } else {
                Glide.with(this).load(bingPic).into(bingPicImg);
            }
        } else {
            requestLocation();
        }
    }

    /**
     * 加载Bing图片
     */
    private void loadBingPic() {
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
                editor.putString("bing_pic", responseTxt);
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
     * 下拉刷新
     */
    private void updateWeather() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isLocation = true;
                requestLocation();
            }
        });
    }

    /**
     * 开启定位
     */
    private void requestLocation() {
        locationClient.startLocation();
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


    /**
     * 根据weatherId网络查询天气
     */
    private void requestWeather() {
        weatherId = getIntent().getStringExtra("weatherId");
        HttpUtil.sendOkHttpRequest(WEATHER_URL + location, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                swipeRefresh.setRefreshing(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseTxt = response.body().string();
//                final HeWeather5 weather = Utility.handleWeatherResponse(responseTxt);
                Gson gson = new Gson();
                final Weather5 weather = gson.fromJson(responseTxt, Weather5.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.getHeWeather5().get(0).getStatus())) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseTxt);
                            editor.putBoolean("havePreferences", true);
                            editor.putString("poiName", poiName);
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            if (null == preferences.getString("date_last", null)) {
                                Log.d(TAG, "run: first add date_last");
                                editor.putString("date_last", weather.getHeWeather5().get(0).getDaily_forecast().get(0).getDate());
                            }
                            editor.apply();
                            showWeatherInfo(weather);
                            if (isLocation) {
                                locationClient.stopLocation();
                            }
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 初始化所有组件
     */
    private void initView() {
        weatherScroll = (ScrollView) findViewById(R.id.weather_scroll_view);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        titleCity = (TextView) findViewById(R.id.title_city);
        chooseCity = (ImageView) findViewById(R.id.choose_city);
//        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        nowTmpText = (TextView) findViewById(R.id.now_tmp_text);
        nowTxtText = (TextView) findViewById(R.id.now_txt_text);
        dailyForecastLayout = (LinearLayout) findViewById(R.id.daily_forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_aqi_text);
        pm25Text = (TextView) findViewById(R.id.aqi_pm25_text);
//        suggestionComf = (TextView) findViewById(R.id.suggestion_comf_text);
//        suggestionCw = (TextView) findViewById(R.id.suggestion_cw_text);
//        suggestionDrsg = (TextView) findViewById(R.id.suggestion_drsg_text);
        suggestionLayout = (LinearLayout) findViewById(R.id.suggestion_layout);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
    }


    /**
     * 更新所有组件内容
     *
     * @param weather
     */
    private void showWeatherInfo(Weather5 weather) {
        Log.d(TAG, "showWeatherInfo: ");
        if (isLocation) {
            titleCity.setText(poiName);
        } else {
            titleCity.setText(weather.getHeWeather5().get(0).getBasic().getCity());
        }

        swipeRefresh.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);
//        titleUpdateTime.setText(weather.basic.update.loc);
        nowTmpText.setText(weather.getHeWeather5().get(0).getNow().getTmp());
        nowTxtText.setText(weather.getHeWeather5().get(0).getNow().getCond().getTxt());
        if (dailyForecastLayout.getChildCount() > 0) {
            //解决出现重复日期的问题
            dailyForecastLayout.removeViews(0, 3);
        }
        for (Weather5.HeWeather5Bean.DailyForecastBean dailyForecast : weather.getHeWeather5().get(0).getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.daily_forecast_item, dailyForecastLayout, false);
            TextView dailyForecastDate = view.findViewById(R.id.daily_forecast_date);
            TextView dailyForecastMax = view.findViewById(R.id.daily_forecast_max);
            TextView dailyForecastMin = view.findViewById(R.id.daily_forecast_min);
            TextView dailyForecastTxtD = view.findViewById(R.id.daily_forecast_txt_d);
            dailyForecastDate.setText(dailyForecast.getDate());
            dailyForecastMax.setText(dailyForecast.getTmp().getMax());
            dailyForecastMin.setText(dailyForecast.getTmp().getMin());
            dailyForecastTxtD.setText(dailyForecast.getCond().getTxt_d());
            dailyForecastLayout.addView(view);
        }
        if (weather.getHeWeather5().get(0).getAqi() != null) {
            aqiText.setText(weather.getHeWeather5().get(0).getAqi().getCity().getAqi());
            pm25Text.setText(weather.getHeWeather5().get(0).getAqi().getCity().getPm25());
        }
        Weather5.HeWeather5Bean.SuggestionBean suggestion = weather.getHeWeather5().get(0).getSuggestion();
        String[] suggestions = {"舒适度：" + suggestion.getComf().getTxt(), "洗车指数：" + suggestion.getCw().getTxt(),
                "穿衣：" + suggestion.getDrsg().getTxt(), "感冒：" + suggestion.getFlu().getTxt(),
                "运动：" + suggestion.getSport().getTxt(), "旅游：" + suggestion.getTrav().getTxt(),
                "紫外线：" + suggestion.getUv().getTxt()};
        if (suggestionLayout.getChildCount() > 0) {
            //解决出现重复日期的问题
            suggestionLayout.removeViews(0, 7);
        }
        for (String str : suggestions) {
            View view = LayoutInflater.from(this).inflate(R.layout.suggestion_item, suggestionLayout, false);
            TextView suggestText = view.findViewById(R.id.suggestion_text);
            suggestText.setText(str);
            suggestionLayout.addView(view);
        }
        addSaveCity(weather);
//        suggestionComf.setText("舒适度：" + suggestion.getComf().getTxt());
//        suggestionCw.setText("洗车指数：" + suggestion.getCw().getTxt());
//        suggestionDrsg.setText("感觉：" + suggestion.getDrsg().getTxt());

    }

    private void addSaveCity(Weather5 weather) {
        List<SaveCity> saveCityList = DataSupport.where("city = ?", weather.getHeWeather5().get(0).getBasic().getCity()).find(SaveCity.class);
        Log.d(TAG, "addSaveCity: saveCityList.size(): " + saveCityList.size());
        if (saveCityList.size() <= 0) {
            SaveCity saveCity = new SaveCity();
            saveCity.setCity(weather.getHeWeather5().get(0).getBasic().getCity());
            saveCity.setWeather(Integer.valueOf(weather.getHeWeather5().get(0).getNow().getTmp()));
            saveCity.setWeatherTxt(weather.getHeWeather5().get(0).getNow().getCond().getTxt());
            saveCity.setTime(weather.getHeWeather5().get(0).getDaily_forecast().get(0).getDate());
            saveCity.save();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyLocation();
    }

    /**
     * 销毁定位
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_city:
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                startActivity(intent);
//                finish();
                break;
            case R.id.choose_city:
                Intent intent1 = new Intent(WeatherActivity.this, CityActivity
                        .class);
                startActivity(intent1);
        }
    }

    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String weatherId = intent.getStringExtra("weatherId");
            Toast.makeText(WeatherActivity.this, "接收到广播" + weatherId, Toast.LENGTH_SHORT).show();
            location = weatherId;
            isLocation = false;
            requestWeather();
        }
    }
}
