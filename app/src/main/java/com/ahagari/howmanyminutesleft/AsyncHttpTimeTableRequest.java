package com.ahagari.howmanyminutesleft;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class AsyncHttpTimeTableRequest extends AsyncTask<String, Void, ArrayList<TimeTableData>> {
//public class AsyncHttpTimeTableRequest {
    private MainAddRosenActivity mActivity;
    private ArrayList<TimeTableData> timeTableList;
    private HashMap<String, String> trainTypeMap;
    private HashMap<String, String> destinationMap;

    public static void main(String[] args) {
        System.out.println("start");
        for( int i=0; i < 3; i++){

            StringBuilder stringbuilder = new StringBuilder(strUrl);
            stringbuilder.append(testUrl);
            stringbuilder.append("&");
            stringbuilder.append("kind=");
            stringbuilder.append((new String[] {
                "1", "2", "4"})[i]);
            new AsyncHttpTimeTableRequest().doInBackground((stringbuilder.toString()));
        }
    }
    // 駅名が複数ヒットする場合は駅名がキー
    // 駅名が一つのみHITした場合は路線名がキー
    // 他に何かいい方法があれば変更
    HashMap<String, RailsInfoData> httpResponseMap = new HashMap<String, RailsInfoData>();;
    ArrayList<RailsInfoData> railsInfoDataArrayList;

    static String testUrl = "/station/time/22775/?gid=3381";
    static String strUrl ="https://transit.yahoo.co.jp";
    String weekday = "kind=1";
    String saturday = "kind=2";
    String holiday = "kind=4";
    boolean resultSingleFlag = false; // １件の場合、true　複数件の場合false

    // インテント
    Intent intent;

    public AsyncHttpTimeTableRequest() {

    }
    public AsyncHttpTimeTableRequest(Activity activity) {
        mActivity = (MainAddRosenActivity)activity;
    }

    public String singleStationName; // 検索結果が一つだけの時の駅名

    @Override
    protected ArrayList<TimeTableData> doInBackground(String... params) {
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        try {
            //String encodedResult = URLEncoder.encode(params[0], "UTF-8");
            String strParam = params[0] + "&kind=1";;

            String requestUrl = strUrl + strParam;
            //strUrl = strUrl + params[0] + "&kind=1";
            URL url = new URL(requestUrl);
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

            setTimeTable(output);

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            connection.disconnect();
        }

        return timeTableList;
    }

    private void setTimeTable(ArrayList<String>output) {
        ArrayList<String> hourList = new ArrayList<String>();
        ArrayList<String> trainTypeList = new ArrayList<String>();
        ArrayList<String> destinationList = new ArrayList<String>();
        int index =0;
        String strTmp;
        hourloop:
        for( int i=index; i < output.size(); i++) {
            strTmp = output.get(i);

            if(strTmp.indexOf("<dl id=\"diagramTbl\"") != -1 ){
                //hourLoop:
                for(;i < output.size();){

//                    if(strTmp.indexOf("</tr>") != -1) {
//                        break hourLoop;
//                    }

                    if(strTmp.indexOf("</li></ul></dd></dl>") != -1) {
                        index = i;
                        break hourloop;
                    }
                    hourList.add(strTmp);
                    i++;
                    strTmp = output.get(i);
                }
            }
        }
        setTimeTableList(hourList);

        outloop:
        for(int i=index;i < output.size(); i++) {
            strTmp = output.get(i);
            //trainTypeList.add(strTmp);
            if(strTmp.indexOf("timeNotice1") != -1 ) {
                for ( i = i+1; i < output.size(); i++) {
                    if(strTmp.indexOf("</dd>") != -1) {
                        index = i;
                        break outloop;
                    }
                    trainTypeList.add(strTmp);
                    strTmp = output.get(i);
                }
            }
        }

        outloop2:
        for(int i=index;i < output.size(); i++) {
            strTmp = output.get(i);
            //trainTypeList.add(strTmp);
            if(strTmp.indexOf("timeNotice2") != -1 ) {
                for (i = i+1; i < output.size(); i++) {
                    if(strTmp.indexOf("</dd>") != -1) {
                        index = i;
                        break outloop2;
                    }
                    destinationList.add(strTmp);
                    strTmp = output.get(i);
                }
            }
        }
        
        setTrainTypeMap(trainTypeList);
        setDestinationMap(destinationList);
        setTimeTableListForFile();
        //makeOutputFile();
        System.out.println("end");
    }

    private void setTimeTableList(ArrayList<String> hourList) {
        String strTmp;
        String hour = null;
        int startPosition;
        int endPosition;

        timeTableList = new ArrayList<TimeTableData>();
        for(int i=0; i < hourList.size(); i++){
            strTmp = hourList.get(i);
            if(strTmp.indexOf("hour") != -1) {
                startPosition = strTmp.indexOf("\">") + 2;
                endPosition = strTmp.indexOf("時");
                hour = strTmp.substring(startPosition, endPosition);
            }

            if(strTmp.indexOf("<dt><a href=\"/station/time/") != -1) {
                TimeTableData tData = new TimeTableData();
                if(hour != null) {
                    tData.hour = Integer.parseInt(hour);
                }
                startPosition = strTmp.indexOf("</a>") + "</a>".length();
                strTmp = strTmp.substring(startPosition, strTmp.length());
                startPosition = 0;
                endPosition = strTmp.indexOf("<");
                String minute = strTmp.substring(startPosition, endPosition);
                tData.minute = Integer.parseInt(minute);

                if(strTmp.indexOf("trainType") != -1) {
                    startPosition = strTmp.indexOf("trainType");
                    strTmp = strTmp.substring(startPosition, strTmp.length());
                    startPosition = strTmp.indexOf("\"trainType\">") + "\"trainType\">".length();
                    endPosition = strTmp.indexOf("</dd>");
                    String type = strTmp.substring(startPosition, endPosition);
                    tData.type = type;
                }
                if(strTmp.indexOf("trainFor") != -1) {
                    startPosition = strTmp.indexOf("trainFor");
                    strTmp = strTmp.substring(startPosition, strTmp.length());
                    startPosition = strTmp.indexOf("\"trainFor\">") + "\"trainFor\">".length();
                    endPosition = strTmp.indexOf("</dd>");
                    String destination = strTmp.substring(startPosition, endPosition);
                    tData.destination = destination;
                }

                timeTableList.add(tData);

            }
        }

    }

    private void makeOutputFile() {
        if(timeTableList == null || destinationMap == null || trainTypeMap == null) {
            return;
        }


    }

    private void setTimeTableListForFile() {

        for(int i=0; i < timeTableList.size(); i++){
            TimeTableData tData = timeTableList.get(i);
            if(tData.destination == null || tData.destination.isEmpty()){
                tData.destination = destinationMap.get("無印");
            } else {
                tData.destination = destinationMap.get(tData.destination);
            }

            if(tData.type == null || tData.type.isEmpty()){
                tData.type = trainTypeMap.get("無印");
            } else {
                String strKey = tData.type.replace("[", "").replace("]", "");
                tData.type = trainTypeMap.get(strKey);
            }
            tData.jikoku = tData.hour * 3600 + tData.minute * 60;
            if(tData.hour < 3)
                tData.jikoku = tData.jikoku + 0x15180L;
        }
    }

    private void setDestinationMap(ArrayList<String> destinationList) {
        String strTmp;
        String key;
        String value;
        int startPosition;
        int endPosition;

        destinationMap = new HashMap<String, String>();

        for(int i=0; i < destinationList.size(); i++) {
            strTmp = destinationList.get(i);
            if(strTmp.indexOf("<li") != -1) {
                while(strTmp.indexOf("</li>") != -1) {
                    startPosition = strTmp.indexOf(">") + 1;
                    endPosition = strTmp.indexOf("：");
                    key = strTmp.substring(startPosition, endPosition);
                    startPosition = endPosition + 1;
                    endPosition = strTmp.indexOf("</li>");
                    value = strTmp.substring(startPosition, endPosition);
                    destinationMap.put(key, value);
                    startPosition = endPosition + "</li>".length();
                    strTmp = strTmp.substring(startPosition, strTmp.length());
                }
            }
        }
    }

    private void setTrainTypeMap(ArrayList<String> trainTypeList) {
        String strTmp;
        String key;
        String value;
        int startPosition;
        int endPosition;

        trainTypeMap = new HashMap<String, String>();

        for(int i=0; i < trainTypeList.size(); i++) {
            strTmp = trainTypeList.get(i);
            if(strTmp.indexOf("<li") != -1) {
                if(strTmp.indexOf("<li>") != -1) {
                    startPosition = strTmp.indexOf("<li>") + "<li>".length();
                } else {
                    startPosition = strTmp.indexOf(">") + 1;
                }
                endPosition = strTmp.indexOf("：");
                key = strTmp.substring(startPosition, endPosition);
                startPosition = endPosition + 1;
                endPosition = strTmp.indexOf("</li>");
                value = strTmp.substring(startPosition, endPosition);
                trainTypeMap.put(key, value);
            }
        }
    }

    private void setTimeTableList2(ArrayList<String> hourList) {
        String strTmp;
        String hour;
        int startPosition;
        int endPosition;

        timeTableList = new ArrayList<TimeTableData>();

        for(int i=0; i < hourList.size(); i++){
            strTmp = hourList.get(i);
            if(strTmp.indexOf("<td class=\"hour\">") != -1) {

                startPosition = strTmp.indexOf(">") + 1;
                endPosition = strTmp.indexOf("</td>");
                hour = strTmp.substring(startPosition, endPosition);
                dataLoop:
                for(;i < hourList.size();) {
                    if(strTmp.indexOf("</tr>") != -1){
                        break dataLoop;
                    }
                    TimeTableData tData = new TimeTableData();
                    tData.hour = Integer.parseInt(hour);
                    if(strTmp.indexOf("<dl") != -1 && strTmp.indexOf("<dt") != -1) {
                        startPosition = strTmp.indexOf("<dt>") +4;
                        endPosition = strTmp.indexOf("</dt>");
                        String minute = strTmp.substring(startPosition, endPosition);
                        tData.minute = Integer.parseInt(minute);
                        startPosition = endPosition + 5;
                        strTmp = strTmp.substring(startPosition, strTmp.length());
                        if(strTmp.indexOf("trainType") != -1) {
                            startPosition = strTmp.indexOf("trainType");
                            strTmp = strTmp.substring(startPosition, strTmp.length());
                            startPosition = strTmp.indexOf("\"trainType\">") + "\"trainType\">".length();
                            endPosition = strTmp.indexOf("</dd>");
                            String type = strTmp.substring(startPosition, endPosition);
                            tData.type = type;
                        }
                        if(strTmp.indexOf("trainFor") != -1) {
                            startPosition = strTmp.indexOf("trainFor");
                            strTmp = strTmp.substring(startPosition, strTmp.length());
                            startPosition = strTmp.indexOf("\"trainFor\">") + "\"trainFor\">".length();
                            endPosition = strTmp.indexOf("</dd>");
                            String destination = strTmp.substring(startPosition, endPosition);
                            tData.destination = destination;
                        }
                        timeTableList.add(tData);
                    }
                    i++;
                    strTmp = hourList.get(i);
                }

            }

        }

    }

    public void onPostExecute(String string) {
        //((TextView)mActivity.findViewById(R.id.showRegisteredButton)).setText(string);
    }

    @Override
    public void onPostExecute(ArrayList<TimeTableData> tList) {
        try {
            System.out.println("start.");
            mActivity.getClass().getMethod("saveTimeTableFile", Object.class).invoke(mActivity,tList);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return;
        } catch(Exception ex) {
            ex.printStackTrace();
            return;
        }
    }
}