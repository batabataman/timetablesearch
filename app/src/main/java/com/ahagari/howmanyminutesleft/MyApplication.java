package com.ahagari.howmanyminutesleft;

import android.app.Application;

import java.util.Map;

public class MyApplication extends Application {

    public Map<String, RailsInfoData> getStationMap() {
        return stationMap;
    }

    public void setStationMap(Map<String, RailsInfoData> stationMap) {
        this.stationMap = stationMap;
    }

    Map<String, RailsInfoData> stationMap;
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
