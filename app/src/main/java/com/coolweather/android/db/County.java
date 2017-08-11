package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * 存放 县 信息
 * Created by pzbz025 on 2017/8/11.
 */

public class County extends DataSupport {
    private int id;
    //县名
    private String countyName;
    //县代号
    private String countyCode;
    //县所属市名
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }


}
