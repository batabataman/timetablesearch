package com.ahagari.howmanyminutesleft;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class AsyncHttpRailsRequest extends AsyncTask<String, Void, Object> {
    private MainActivity mActivity;


    // 路線名がキー
    HashMap<String, RailsInfoData> httpResponseMap = new HashMap<String, RailsInfoData>();;
    ArrayList<RailsInfoData> railsInfoDataArrayList;

    String strUrl = "https://transit.yahoo.co.jp";
    String strParam = "&done=time&q=";
    String stationName;
    boolean resultSingleFlag = false; // １件の場合、true　複数件の場合false

    // インテント
    Intent intent;

    private AsyncTaskCallback callback = null;;

    public AsyncHttpRailsRequest(Activity activity) {
        mActivity = (MainActivity)activity;
    }

    public interface AsyncTaskCallback {
        void preExecute();
        void postExecute(String result);
        void progressUpdate(int progress);
        void cancel();
    }

    public AsyncHttpRailsRequest( Activity activity, AsyncTaskCallback _callback) {
        this.callback = _callback;
        mActivity = (MainActivity)activity;
    }

    public String singleStationName; // 検索結果が一つだけの時の駅名

    private CallBackTask callbacktask;


    @Override
    protected Object doInBackground(String... params) {
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        try {
             //encodedResult = URLEncoder.encode(params[0], "UTF-8");
            strUrl = strUrl + params[0];
            if(params.length > 1) {
                stationName = params[1];
            }
            String encodedResult = URLEncoder.encode(params[1], "UTF-8");
            strUrl = strUrl + strParam + encodedResult;
            URL url = new URL(strUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();

            ArrayList<String> output = new ArrayList<String>();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                output.add(line);
            }
            is.close();

            setRailsInfomation(output);

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            connection.disconnect();
        }
        if (httpResponseMap.isEmpty()) {
            return null;
        }
        return httpResponseMap;
    }

    private void setRailsInfomation(ArrayList<String> output) {
        int intSize;
        intSize = output.size();
        String outputLine;
        boolean addFlag = false;
        int startPosition;
        int endPosition;
        String stringTemp;
        String stationNameKana = null;
        String stationName = null;
        String railName = null;
        ArrayList<RailsInfoData> railsList = new ArrayList<RailsInfoData>();
        ArrayList<String> destTempList = new ArrayList<String>();
        ArrayList<String> destList = new ArrayList<String>();
        boolean newDestFlag = false;

        for(int i=0; i < intSize; i++) {
            outputLine = output.get(i);

            //     <h1 class="title"><span class="staKana">ひがしぎんざ</span>東銀座</h1>
            if(outputLine.indexOf("staKana") != -1 && newDestFlag == false){
                // 駅名の名前とカナを設定
                stringTemp = outputLine.substring(outputLine.indexOf("staKana") + 9);
                startPosition = 0;
                endPosition = stringTemp.indexOf("</span>");
                stationNameKana = stringTemp.substring(startPosition, endPosition);
                startPosition = 9;
                endPosition = stringTemp.length();
                stringTemp = stringTemp.substring(startPosition, endPosition);
                startPosition = stringTemp.indexOf(">")  + 1; // /span>東高円寺</h1>
                endPosition = stringTemp.indexOf("</h1>");
                stationName = stringTemp.substring(startPosition, endPosition);
//                System.out.println(stationNameKana);
//                System.out.println(stationName);
            }

            if(outputLine.indexOf("<header class=\"labelMedium\">") != -1) {
                newDestFlag = true;
            }
            if((outputLine.indexOf("<h2 class=\"title\">") != -1) && newDestFlag == true && outputLine.indexOf("header") == -1){
                startPosition = outputLine.indexOf(">") + 1;
                endPosition = outputLine.indexOf("</h2>");
                railName = outputLine.substring(startPosition, endPosition);
                continue;
            }

            if(outputLine.indexOf("listRowlink") != -1 ){
                destTempList.clear();
                for(; outputLine.indexOf("/div") == -1; i++) {
                    outputLine = output.get(i);
                    if(outputLine.indexOf("class=\"double\"") != -1) {
                        // 要素の取得終了
                        break;
                    }
                    destTempList.add(outputLine);
                }
                setDestList(destTempList, stationName, stationNameKana, railName);
            }


        }
        if(stationName != null || !stationName.isEmpty()) {
            singleStationName = stationName;
        }


    }

    private void setDestList(ArrayList<String> destTempList, String stationName, String stationNameKana, String railName) {
        int startPosition;
        int endPosition;
        String stringTemp;
        int intSize;
        String destUrl;
        String destName;

        intSize = destTempList.size();

        for(int i=0; i < intSize; i++) {
            stringTemp = destTempList.get(i);
            if( stringTemp.indexOf("href") != -1 && stringTemp.indexOf("time") != -1){
                startPosition = stringTemp.indexOf("=") + 2;
                endPosition = stringTemp.indexOf(">") -1;
                if( startPosition > endPosition) {
                    System.out.println("error.");
                }
                destUrl = stringTemp.substring(startPosition, endPosition);
                loop_inner:
                for(;; i++) {
                    if(i >= intSize) {
                        break;
                    }
                    stringTemp = destTempList.get(i);
                    if(stringTemp.indexOf("title") != -1){
                        startPosition = stringTemp.indexOf(">") + 1;
                        endPosition = stringTemp.indexOf("</dt");
                        destName = stringTemp.substring(startPosition, endPosition);
                        RailsInfoData railsInfoData = new RailsInfoData();
                        railsInfoData.railName = railName;
                        railsInfoData.stationName = stationName;
                        railsInfoData.stationNameKana = stationNameKana;
                        railsInfoData.destUrl = destUrl;
                        railsInfoData.destinationName = destName;
                        httpResponseMap.put(destUrl, railsInfoData);

                        break loop_inner;
                    }

                }

            }
        }
    }

    private void setDestListInfo(ArrayList<String> destTempList, String stationName, String stationNameKana, String railName) {
        int startPosition;
        int endPosition;
        String stringTemp;
        int intSize;
        String destUrl;
        String destName;
        httpResponseMap = new HashMap<String, RailsInfoData>();

        intSize = destTempList.size();

        for(int i=0; i < intSize; i++) {
            stringTemp = destTempList.get(i);
            if( stringTemp.indexOf("href") != -1 && stringTemp.indexOf("time") != -1){
                startPosition = stringTemp.indexOf("=") + 2;
                endPosition = stringTemp.indexOf(">") -1;
                if( startPosition > endPosition) {
                    System.out.println("error.");
                }
                destUrl = stringTemp.substring(startPosition, endPosition);
                loop_inner:
                for(;; i++) {
                    if(i >= intSize) {
                        break;
                    }
                    stringTemp = destTempList.get(i);
                    if(stringTemp.indexOf("title") != -1){
                        startPosition = stringTemp.indexOf(">") + 1;
                        endPosition = stringTemp.indexOf("</dt");
                        destName = stringTemp.substring(startPosition, endPosition);
                        RailsInfoData railsInfoData = new RailsInfoData();
                        railsInfoData.railName = railName;
                        railsInfoData.stationName = stationName;
                        railsInfoData.stationNameKana = stationNameKana;
                        railsInfoData.destUrl = destUrl;
                        railsInfoData.destinationName = destName;
                        httpResponseMap.put(destUrl, railsInfoData);

                        break loop_inner;
                    }

                }

            }
        }
    }
//    private void setMultipuleStation(ArrayList<String> output) {
//        int intSize;
//        intSize = output.size();
//        String outputLine;
//        ArrayList<String> stationList = new ArrayList<String>();
//        boolean addFlag = false;
//        for (int i = 0; i < intSize; i++) {
//            outputLine = output.get(i);
//            if (outputLine.indexOf("class=\"station\"") != -1 || addFlag == true) {
//                addFlag = true;
//                stationList.add(outputLine);
//                if (outputLine.isEmpty() || outputLine.indexOf("</ul>") != -1) {
//                    addFlag = false;
//                    break;
//                }
//            }
//        }
//        requestStation(stationList);
//    }
//
//
//
//    public void requestStation(ArrayList<String> stationList) {
//        int startPosition;
//        int endPosition;
//        boolean keyFlag = false;
//        String stringTemp;
//        String stringTempKey = null;
//        String stringTempValue = null;
//        httpResponseMap = new HashMap<String, RailsInfoData>();
//        railsInfoDataArrayList = new ArrayList<RailsInfoData>();
//
//        int intSize = stationList.size();
//        for(int i=0; i < intSize; i++) {
//            stringTemp = stationList.get(i);
//            if(stringTemp.indexOf("href") != -1 ){
//                startPosition = stringTemp.indexOf("=");
//                endPosition = stringTemp.length();
//                startPosition = startPosition + 2;
//                endPosition = endPosition -2;
//                stringTempValue = stringTemp.substring(startPosition, endPosition);
//                keyFlag = true;
//
//
//            }
//
//            if(stringTemp.indexOf("title") != -1 && keyFlag == true ) {
//                startPosition = stringTemp.indexOf(">") + 1;
//                endPosition = stringTemp.indexOf("/dt") -1;
//                stringTempKey = stringTemp.substring(startPosition, endPosition);
//                RailsInfoData railsInfoData = new RailsInfoData();
//                railsInfoData.stationName = stringTempKey;
//                railsInfoData.stationUrl = stringTempValue;
//
//                httpResponseMap.put(stringTempKey, railsInfoData);
//                keyFlag = false;
//
//            }
//        }
//    }
//
//    public void requestStation(String stringStation) {
//        int startPosition;
//        int endPosition;
//        String stringTemp = stringStation;
//        String stringStationUrl;
//        String stringStationName;
//
//
//         httpResponseMap = new HashMap<String, RailsInfoData>();
//
//        System.out.println(stringStation.length());
//        for(; stringTemp.indexOf("=") != -1; ) {
//            stringTemp = stringTemp.substring(stringTemp.indexOf(">") + 1, stringTemp.length());
//            //System.out.println(stringTemp);
//            startPosition = stringTemp.indexOf("=") +2;
//            endPosition = stringTemp.indexOf("\">");
//            stringStationUrl = stringTemp.substring(startPosition, endPosition);
//            startPosition = endPosition + 2;
//            endPosition = stringTemp.length();
//            stringTemp = stringTemp.substring(startPosition, endPosition);
//            startPosition = 0;
//            endPosition = stringTemp.indexOf("</a>");
//            stringStationName = stringTemp.substring(startPosition, endPosition);
//            startPosition = endPosition;
//            endPosition = stringTemp.length();
//            startPosition = stringTemp.indexOf("/li>") + 4;
//            endPosition = stringTemp.length();
//            stringTemp = stringTemp.substring(startPosition, endPosition);
//            RailsInfoData railsInfoData = new RailsInfoData();
//            railsInfoData.stationUrl = stringStationUrl;
//            httpResponseMap.put(stringStationName, railsInfoData);
//
//            //break;
//        }
//
//        //System.out.println(stringStation.substring(startPosition, endPosition));
//    }

    public void onPostExecute(String string) {
       // ((TextView)mActivity.findViewById(R.id.showRegisteredButton)).setText(string);
    }

    @Override
    public void onPostExecute(Object map) {
        super.onPostExecute(map);
       // ((Button)mActivity.findViewById(R.id.showRegisteredButton)).setText(map.get("渋谷"));
       // mActivity.editText = mActivity.findViewById(R.id.editTextStation);
       // mActivity.listViewStation = mActivity.findViewById(R.id.listViewStation);

//        if(resultSingleFlag == true){
//            mActivity.listViewStation.setOnItemClickListener(mActivity);
//        } else {
//            mActivity.listViewStation.setOnItemClickListener(mActivity);
//           mActivity.stationMap = map;
//            mActivity.stationList = new ArrayList<String>();
//            for (String key : mActivity.stationMap.keySet()) {
//                mActivity.stationList.add(key);
//            }
//
//            mActivity.adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, mActivity.stationList);
//            mActivity.findViewById(R.id.listViewStation);
//            mActivity.listViewStation.setAdapter(mActivity.adapter);
//        }

        //mActivity.arrayAdapter =  new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_dropdown_item);
        mActivity.rosenMap = (HashMap<String, RailsInfoData>)map;
        mActivity.setRosenMap((HashMap<String, RailsInfoData>)map);
        //mActivity.strSearchWord = "aaaa";
        //callbacktask.CallBack(map);
        //mActivity.spinner.setAdapter(mActivity.arrayAdapter);
        // 直列化でmap保存
        //mActivity.intent.putExtra("rosenMap", mActivity.rosenMap);
        MainActivity.rosenStaticMap = (HashMap<String, RailsInfoData>)map;
        Bundle bundle = new Bundle();
        RailsDataMap railsDataMap = new RailsDataMap((HashMap<String, RailsInfoData>)map);
        bundle.putParcelable("rosenMap", railsDataMap);
        // test
        ParcelTest pTest = new ParcelTest("test");
        bundle.putParcelable("test", pTest);
        Intent intent = new Intent(mActivity.getApplicationContext(), MainAddRosenActivity.class);
        intent.putExtra("rosenMap", (HashMap<String, RailsInfoData>)map);
        intent.putExtra(mActivity.getString(R.string.station_result_status), mActivity.getString(R.string.station_result_multi));
        intent.putExtra(mActivity.getString(R.string.station_name), stationName);
        mActivity.startActivity(intent);
    }


    public void setOnCallBack(CallBackTask _cbj) {
        callbacktask = _cbj;
    }

    public static class CallBackTask {
        public void CallBack(Object result) {
        }
    }
}