package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by pzbz025 on 2017/8/14.
 */

public class HeWeather5 {
    public String status;

    public Basic basic;

    public Alarms alarms;

    public Aqi aqi;

    @SerializedName("daily_forecast")
    public List<DailyForecast> dailyForecast;

    @SerializedName("hourly_forecast")
    public List<HourlyForecast> hourlyForecast;

    public Now now;

    public Suggestion suggestion;
}
