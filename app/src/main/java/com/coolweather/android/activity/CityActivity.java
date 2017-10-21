package com.coolweather.android.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.coolweather.android.R;
import com.coolweather.android.adapter.CityAdapter;
import com.coolweather.android.db.SaveCity;

import org.litepal.crud.DataSupport;

import java.util.List;

public class CityActivity extends AppCompatActivity {

    private RecyclerView cityRecycler;

    private CityAdapter cityAdapter;

    private List<SaveCity> saveCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        initView();
        initList();
        initRecycler();
    }

    private void initView(){
        cityRecycler = findViewById(R.id.city_recycler);
    }

    private void initRecycler(){
        LinearLayoutManager manager = new LinearLayoutManager(this);
        cityRecycler.setLayoutManager(manager);
        cityRecycler.setAdapter(cityAdapter);
    }

    private void initList(){
        saveCities = DataSupport.findAll(SaveCity.class);
        cityAdapter = new CityAdapter(saveCities);
    }
}
