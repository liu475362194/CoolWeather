package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * 数据库存放 省 信息
 * Created by pzbz025 on 2017/8/11.
 */

public class Province extends DataSupport{

    private int id;
    //省名
    private String provinceName;
    //省代号
    private String provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }
}
