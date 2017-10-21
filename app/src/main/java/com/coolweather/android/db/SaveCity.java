package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by pzbz025 on 2017/9/12.
 */

public class SaveCity extends DataSupport {
    private int id;
    private String city;
    private String response;
    private int weather;
    private String weatherTxt;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public String getWeatherTxt() {
        return weatherTxt;
    }

    public void setWeatherTxt(String weatherTxt) {
        this.weatherTxt = weatherTxt;
    }
}
