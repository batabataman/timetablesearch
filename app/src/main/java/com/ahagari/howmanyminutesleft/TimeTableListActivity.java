package com.ahagari.howmanyminutesleft;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TimeTableListActivity extends AppCompatActivity implements View.OnClickListener {


    int intBackGroundColor;

    // View
    TextView  textView2;
    Button backButton;
    ListView listView;
    ConstraintLayout constraintLayout;

    // インテント
    Intent intent;

    String filename;

    InputStream inputStream;
    String lineBuffer;
    List<String> timeTableAfterList;
    List<String> timeTableBeforeList;
    List<String> timeTableList;

    ArrayList<CustomListData> afterViewList;
    ArrayList<CustomListData> beforeViewList;
    List<CustomListData> viewItems;

    ArrayAdapter<String> adapter; // listViewとデータをつなぐアダプター
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table_list);

        textView2 = findViewById(R.id.textView2);
        backButton = findViewById(R.id.button2);
        listView = findViewById(R.id.listView);
        constraintLayout = findViewById(R.id.constraintLayout);
        intBackGroundColor = R.color.color_black;
        constraintLayout.setBackgroundResource(intBackGroundColor);

        intent = getIntent();
        Bundle bundle = intent.getExtras();

        // 時刻取得
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        long nowJikoku = hour * 3600 + minutes * 60 + second;
        if(hour < 3)
            nowJikoku = nowJikoku + 0x15180L;

        filename = bundle.getString(getString(R.string.timetable_list_file));

        ArrayList<String> fileList = new ArrayList<String>();
        try {
            inputStream = openFileInput(filename);

            BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));

            while( (lineBuffer = reader.readLine()) != null ){
                System.out.println(lineBuffer);
                fileList.add(lineBuffer);

//                String[] lineArray = lineBuffer.split(",");
//                timeTableList.add(String.format("%s時%s分\n%s %s",
//                        lineArray[0], lineArray[1], lineArray[3].equals("null") ? "" : lineArray[3] , lineArray[4]));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int startIndex =0;
        int j=0;
        timeTableAfterList = new ArrayList<String>();
        afterViewList = new ArrayList<CustomListData>();
        for( int i=0; i < fileList.size(); i++){
            String strTmp = fileList.get(i);
            String[] lineArray = strTmp.split(",");

            int stationTime = Integer.parseInt(lineArray[2]);
            if(nowJikoku > stationTime){
                continue;
            }
            if( j==0) {
                startIndex = i - 1;
            }

            if(j > 9) {
                break;
            }
            long  longTmp = stationTime - nowJikoku;
            long dispHour = longTmp / 60 / 60;
            long dispMinute = (longTmp % 3600) / 60;
            String dispLeftTime;
            if(dispHour > 0) {
                dispLeftTime = String.format("あと%s時間%s分", String.valueOf(dispHour), String.valueOf(dispMinute));
            } else {
                dispLeftTime = String.format("あと%s分", String.valueOf(dispMinute));
            }
            timeTableAfterList.add(String.format("%s時%s分\n%s\n%s %s",
                    lineArray[0], lineArray[1], dispLeftTime, lineArray[3].equals("null") ? "" : lineArray[3] , lineArray[4]));

            // tData.hour, tData.minute, tData.jikoku, tData.type, tData.destination);
            CustomListData customItem = new CustomListData();
            customItem.setListTextIndex(getTextIndex(j));
            customItem.setListTextTime(String.format("%s時%s分",lineArray[0], lineArray[1]));
            customItem.setListTextTrainType(lineArray[3].equals("null") ? "" : lineArray[3]);
            customItem.setListTextDest(lineArray[4] + "行き");
            customItem.setListTextCountTime(dispLeftTime);
            afterViewList.add(customItem);
            j++;
        }



        // 表示する時刻が10件未満の場合、翌日の時刻を表示
        if(timeTableAfterList.size() < 10) {
            addTomorrowTimeTable(fileList, nowJikoku);
        }

        ArrayList<String> beforeList = new ArrayList<String>();
        if(startIndex > 0) {
            int i = startIndex;
            int count =0;
            while(i >= 0) {
                if(count > 9) {
                    break;
                }
                String strTmp = fileList.get(i);
                beforeList.add(strTmp);
                i--;
                count++;
            }
        }

        timeTableBeforeList = new ArrayList<String>();
        beforeViewList = new ArrayList<CustomListData>();
        Collections.reverse(beforeList);
        for(int i=0; i < beforeList.size(); i++){
            String strTmp = beforeList.get(i);
            String[] lineArray = strTmp.split(",");
            int stationTime = Integer.parseInt(lineArray[2]);
            long longTmp = nowJikoku -stationTime;
            long dispHour = longTmp / 60 / 60;
            long dispMinute = (longTmp % 3600) / 60;
            String dispPastTime;
            if(dispHour > 0) {
                dispPastTime = String.format("%s時間%s分前", String.valueOf(dispHour), String.valueOf(dispMinute));
            } else {
                dispPastTime = String.format("%s分前", String.valueOf(dispMinute));
            }

            timeTableBeforeList.add(String.format("%s時%s分\n%s\n%s %s",
                    lineArray[0], lineArray[1], dispPastTime, lineArray[3].equals("null") ? "" : lineArray[3] , lineArray[4]));

            CustomListData customItem = new CustomListData();
            customItem.setListTextIndex("出発済み");
            customItem.setListTextTime(String.format("%s時%s分",lineArray[0], lineArray[1]));
            // tData.hour, tData.minute, tData.jikoku, tData.type, tData.destination);
            customItem.setListTextTrainType(lineArray[3].equals("null") ? "" : lineArray[3]);
            customItem.setListTextDest(lineArray[4] + "行き");
            customItem.setListTextCountTime(dispPastTime);
            beforeViewList.add(customItem);
        }

        timeTableList = new ArrayList<String>();
        timeTableList.addAll(timeTableBeforeList);
        timeTableList.addAll(timeTableAfterList);

        viewItems = new ArrayList<CustomListData>();
        viewItems.addAll(beforeViewList);
        viewItems.addAll(afterViewList);



        textView2.setText(filename);
        textView2.setTextColor(Color.GREEN);

        MyListArrayAdapter customAdapter = new MyListArrayAdapter(this, R.layout.list, viewItems);
        //adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, timeTableList);
        listView.setAdapter(customAdapter);
        if(beforeViewList != null || beforeViewList.size() > 1) {
            listView.setSelection(beforeViewList.size());
        }
        backButton.setOnClickListener(this);
        //listView.setEnabled(false);
    }


    private String getTextIndex(int j){
        String[] indexString = new String[]{"先発", "次発", "次々発"};
        if(j < 3) {
            return indexString[j];
        }

        return j + "番目";
    }

    private void addTomorrowTimeTable(ArrayList<String> fileList, long nowJikoku) {
//        int timeTableListIndex = 0;
//        if(timeTableAfterList != null && timeTableAfterList.size() > 0) {
//            timeTableListIndex =timeTableAfterList.size();
//        }
        int countIndex = fileList.size() - 1;
        for(int i=0; i < 10; i++ ){
            if(i > fileList.size() - 1){
                break;
            }
            String strTmp = fileList.get(i);
            String[] lineArray = strTmp.split(",");
            int stationTime = Integer.parseInt(lineArray[2]);
            long  longTmp = stationTime + 86400 - nowJikoku;
            long dispHour = longTmp  / 60 / 60;
            long dispMinute = (longTmp % 3600) / 60;
            String dispLeftTime;
            if(dispHour > 0) {
                dispLeftTime = String.format("あと%s時間%s分", String.valueOf(dispHour), String.valueOf(dispMinute));
            } else {
                dispLeftTime = String.format("あと%s分", String.valueOf(dispMinute));
            }
            timeTableAfterList.add(String.format("%s時%s分\n%s\n%s %s",
                    lineArray[0], lineArray[1], dispLeftTime, lineArray[3].equals("null") ? "" : lineArray[3] , lineArray[4]));
            CustomListData customItem = new CustomListData();
            customItem.setListTextIndex(getTextIndex(countIndex));
            customItem.setListTextTime(String.format("%s時%s分",lineArray[0], lineArray[1]));
            // tData.hour, tData.minute, tData.jikoku, tData.type, tData.destination);
            customItem.setListTextTrainType(lineArray[3].equals("null") ? "" : lineArray[3]);
            customItem.setListTextDest(lineArray[4] + "行き");
            customItem.setListTextCountTime(dispLeftTime);
            afterViewList.add(customItem);
            countIndex++;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button2) {
            finish();
        }
    }
}
