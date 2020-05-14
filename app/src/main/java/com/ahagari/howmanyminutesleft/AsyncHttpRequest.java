package com.ahagari.howmanyminutesleft;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class AsyncHttpRequest extends AsyncTask<String, Void, HashMap<String, RailsInfoData>> {
    private MainActivity mActivity;

    // 駅名が複数ヒットする場合は駅名がキー
    // 駅名が一つのみHITした場合は路線名がキー
    // 他に何かいい方法があれば変更
    HashMap<String, RailsInfoData> httpResponseMap = new HashMap<String, RailsInfoData>();;
    ArrayList<RailsInfoData> railsInfoDataArrayList;

    String strUrl ="https://transit.yahoo.co.jp/station/time/search?srtbl=on&kind=1&done=time&q=";
    boolean resultSingleFlag = false; // １件の場合、true　複数件の場合false

    // インテント
    Intent intent;

    public AsyncHttpRequest(Activity activity) {
        mActivity = (MainActivity)activity;
    }

    public String singleStationName; // 検索結果が一つだけの時の駅名

    @Override
    protected HashMap<String, RailsInfoData> doInBackground(String... params) {
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        try {
            String encodedResult = URLEncoder.encode(params[0], "UTF-8");
            strUrl = strUrl + encodedResult;
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

            if(sb.toString().indexOf("件)</span>") == -1){
                // 駅名の検索結果が一つの場合
                resultSingleFlag = true;
                mActivity.resultSingleFlag = true;
                setRailsInfomation(output);
            } else {
                // 駅名の検索結果が複数の場合
                setMultipuleStation(output);
            }

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

            if(outputLine.indexOf("</section></section>") != -1) {
                break;
            }

            if(outputLine.indexOf("class=\"double\"") != -1){
                break;
            }
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

            if(outputLine.indexOf("listRowlink") != -1 && newDestFlag == true){
                destTempList.clear();
                for(; outputLine.indexOf("/div") == -1; i++) {
                    outputLine = output.get(i);
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
        String destUrl = null;
        String destName;
        boolean noAddFlag = false;

        intSize = destTempList.size();

        for(int i=0; i < intSize; i++) {
            stringTemp = destTempList.get(i);
            if( stringTemp.indexOf("href") != -1 && stringTemp.indexOf("time") != -1){
                startPosition = stringTemp.indexOf("=") + 2;
                endPosition = stringTemp.indexOf(">") -1;
                if( startPosition > endPosition) {
                    System.out.println("error.");
                }
                // 路線情報取得中止
                if(stringTemp.indexOf("class=\"double\"") != -1){
                    break;
                }
                try {
                    destUrl = stringTemp.substring(startPosition, endPosition);
                } catch (StringIndexOutOfBoundsException ex) {
                    ex.getMessage();
                    break;
                }
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

    private void setMultipuleStation(ArrayList<String> output) {
        int intSize;
        intSize = output.size();
        String outputLine;
        ArrayList<String> stationList = new ArrayList<String>();
        boolean addFlag = false;
        for (int i = 0; i < intSize; i++) {
            outputLine = output.get(i);
            if (outputLine.indexOf("class=\"station\"") != -1 || addFlag == true) {
                addFlag = true;
                stationList.add(outputLine);
                if(outputLine.indexOf("class=\"double\"") != -1) {
                    // 要素の取得終了
                    break;
                }
                if (outputLine.isEmpty() || outputLine.indexOf("</ul>") != -1) {
                    addFlag = false;
                    break;
                }
            }
        }
        requestStation(stationList);
    }

    

    public void requestStation(ArrayList<String> stationList) {
        int startPosition;
        int endPosition;
        boolean keyFlag = false;
        String stringTemp;
        String stringTempKey = null;
        String stringTempValue = null;
        httpResponseMap = new HashMap<String, RailsInfoData>();
        railsInfoDataArrayList = new ArrayList<RailsInfoData>();

        int intSize = stationList.size();
        for(int i=0; i < intSize; i++) {
            stringTemp = stationList.get(i);
            if(stringTemp.indexOf("href") != -1 ){
                startPosition = stringTemp.indexOf("=");
                endPosition = stringTemp.length();
                startPosition = startPosition + 2;
                endPosition = endPosition -2;
                stringTempValue = stringTemp.substring(startPosition, endPosition);
                keyFlag = true;


            }

            if(stringTemp.indexOf("title") != -1 && keyFlag == true ) {
                startPosition = stringTemp.indexOf(">") + 1;
                endPosition = stringTemp.indexOf("/dt") -1;
                stringTempKey = stringTemp.substring(startPosition, endPosition);
                RailsInfoData railsInfoData = new RailsInfoData();
                railsInfoData.stationName = stringTempKey;
                railsInfoData.stationUrl = stringTempValue;

                httpResponseMap.put(stringTempKey, railsInfoData);
                keyFlag = false;

            }
        }
    }

    public void requestStation(String stringStation) {
        int startPosition;
        int endPosition;
        String stringTemp = stringStation;
        String stringStationUrl;
        String stringStationName;


         httpResponseMap = new HashMap<String, RailsInfoData>();

        System.out.println(stringStation.length());
        for(; stringTemp.indexOf("=") != -1; ) {
            stringTemp = stringTemp.substring(stringTemp.indexOf(">") + 1, stringTemp.length());
            //System.out.println(stringTemp);
            startPosition = stringTemp.indexOf("=") +2;
            endPosition = stringTemp.indexOf("\">");
            stringStationUrl = stringTemp.substring(startPosition, endPosition);
            startPosition = endPosition + 2;
            endPosition = stringTemp.length();
            stringTemp = stringTemp.substring(startPosition, endPosition);
            startPosition = 0;
            endPosition = stringTemp.indexOf("</a>");
            stringStationName = stringTemp.substring(startPosition, endPosition);
            startPosition = endPosition;
            endPosition = stringTemp.length();
            startPosition = stringTemp.indexOf("/li>") + 4;
            endPosition = stringTemp.length();
            stringTemp = stringTemp.substring(startPosition, endPosition);
            RailsInfoData railsInfoData = new RailsInfoData();
            railsInfoData.stationUrl = stringStationUrl;
            httpResponseMap.put(stringStationName, railsInfoData);

            //break;
        }

        //System.out.println(stringStation.substring(startPosition, endPosition));
    }

    public void onPostExecute(String string) {
        //((TextView)mActivity.findViewById(R.id.showRegisteredButton)).setText(string);
    }

    @Override
    public void onPostExecute(HashMap<String, RailsInfoData> map) {
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

        mActivity.arrayAdapter =  new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_dropdown_item);
        mActivity.stationMap = map;
        mActivity.resultSingleFlag = resultSingleFlag;
        mActivity.intent.putExtra(mActivity.getString(R.string.station_map), mActivity.stationMap);
        if(resultSingleFlag == true){

            mActivity.arrayAdapter.add(singleStationName);
        } else {
            for(String key: mActivity.stationMap.keySet()){
                mActivity.arrayAdapter.add(key);
            }
        }
        mActivity.spinner.setAdapter(mActivity.arrayAdapter);
    }
}