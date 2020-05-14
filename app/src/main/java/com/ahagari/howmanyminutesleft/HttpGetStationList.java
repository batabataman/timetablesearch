package com.ahagari.howmanyminutesleft;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpGetStationList extends AsyncTask<String, Void, Void> {

    String strUrl ="https://transit.yahoo.co.jp/station/time/search?srtbl=on&kind=1&done=time&q=";
    String encodedResult;

    public HashMap<String, String> getStationMap() {
        return stationMap;
    }

    private HashMap<String, String> stationMap;

    void runHttpRequest(String target) {


        try {
            String encodedResult = URLEncoder.encode(target, "UTF-8");
        } catch (Exception ex) {

        }
        strUrl = strUrl + encodedResult;
        HttpURLConnection urlConn = null;
        InputStream in = null;
        BufferedReader reader = null;

        try {
            //接続するURLを指定する
            URL url = new URL(strUrl);

            //コネクションを取得する
            urlConn = (HttpURLConnection) url.openConnection();

            urlConn.setRequestMethod("GET");
//			urlConn.setRequestMethod("POST");

            urlConn.connect();

            int status = urlConn.getResponseCode();

            System.out.println("HTTPステータス:" + status);

            if (status == HttpURLConnection.HTTP_OK) {

                in = urlConn.getInputStream();

                reader = new BufferedReader(new InputStreamReader(in));

                ArrayList<String> output = new ArrayList<String>();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.add(line);
                }

                int intSize;
                intSize = output.size();
                String outputLine;
                ArrayList<String> stationList = new ArrayList<String>();
                boolean addFlag = false;
                for(int i=0; i < intSize; i++) {
                    outputLine = output.get(i);
                    if(outputLine.indexOf("elmSearchItem quad") != -1 || addFlag == true) {
                        addFlag = true;
                        stationList.add(outputLine);
                        if(outputLine.indexOf("/ul") != -1) {
                            addFlag = false;
                            break;
                        }
                    }
                }
                // System.out.println("----- start display station list -----");
                for(int i=0; i < stationList.size(); i++) {
                    System.out.println(stationList.get(i));
                }
                //stationList.remove(0);

                // 駅名と路線URLを取得
                requestStation(stationList.get(1));
                //System.out.println(output.toString());
                //System.out.println("---------");
                //System.out.println(formatResult(output).toString());
                //formatResult(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (urlConn != null) {
                    urlConn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestStation(String stringStation) {
        int startPosition;
        int endPosition;
        String stringTemp = stringStation;
        String stringStationUrl;
        String stringStationName;

        stationMap = new HashMap<String, String>();

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
//            System.out.println(stringStationName);
//            System.out.println(stringStationUrl);
            //System.out.println(stringTemp);
            stationMap.put(stringStationName, stringStationUrl);

            //break;
        }

    }


    @Override
    protected Void doInBackground(String... strings) {
        String target = strings[0];
        runHttpRequest(target);
        return null;
    }
}
