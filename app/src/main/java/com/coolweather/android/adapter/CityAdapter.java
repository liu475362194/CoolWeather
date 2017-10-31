package com.coolweather.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coolweather.android.R;
import com.coolweather.android.db.SaveCity;

import java.util.List;

/**
 * Created by pzbz025 on 2017/9/12.
 */

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder>{

    private List<SaveCity> saveCities;

    public CityAdapter(List<SaveCity> saveCities) {
        this.saveCities = saveCities;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SaveCity saveCity = saveCities.get(position);
        holder.cityName.setText(saveCity.getCity());
        holder.cityQiHou.setText(saveCity.getWeatherTxt());
        holder.cityWenDu.setText(String.valueOf(saveCity.getWeather()));
        holder.time.setText(saveCity.getTime());
    }

    @Override
    public int getItemCount() {
        return saveCities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView cityName;
        private TextView cityWenDu;
        private TextView cityQiHou;
        private TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.city_name);
            cityWenDu = itemView.findViewById(R.id.city_wendu);
            cityQiHou = itemView.findViewById(R.id.city_qihou);
            time = itemView.findViewById(R.id.time);
        }
    }
}
