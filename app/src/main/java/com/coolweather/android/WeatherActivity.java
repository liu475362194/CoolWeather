package com.coolweather.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.coolweather.android.gson.HeWeather5;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public static final String WEATHER_URL = "https://free-api.heweather.com/v5/weather?key=cd83db70d0ba468a8486a906fee14faf&city=";

    private String weatherId;

    private static final String TAG = "WeatherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);



        weatherId = getIntent().getStringExtra("weatherId");
        HttpUtil.sendOkHttpRequest(WEATHER_URL + weatherId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseTxt = response.body().string();
                HeWeather5 weather = Utility.handleWeatherResponse(responseTxt);
                Log.d(TAG, "onResponse: weather" + weather.hourlyForecast.get(0).cond.txt);
            }
        });
    }
}
