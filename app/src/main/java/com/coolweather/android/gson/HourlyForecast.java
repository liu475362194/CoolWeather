package com.coolweather.android.gson;

/**
 * Created by pzbz025 on 2017/8/14.
 */

public class HourlyForecast {

    public Cond cond;

    public class Cond{
        public String code;
        public String txt;
    }

    public String date;

    public String hum;

    public String pop;

    public String pres;

    public String tmp;

    public Wind wind;

    public class Wind{
        public String deg;

        public String dir;

        public String sc;

        public String spd;
    }
}
