package com.ahagari.howmanyminutesleft;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, android.text.TextWatcher {

//    TODO 1. searchButtonボタンを押した場合
//　　-> spinnerに駅名の検索結果を表示
//         -> 検索用に入力した文字列を消す
//    TODO 2. 決定ボタンを押した場合
//　　->　路線のプルダウンを表示
//    TODO 3.  駅名の一覧が取得できない場合
//　　→「駅名の取得に失敗しました」と出力する

    // View
    EditText editText;
    Spinner spinner;
    Button searchButton, button;//showRegisteredButton,
    ListView listView;

    ArrayAdapter<String> adapter; // listViewとデータをつなぐアダプター
    ArrayAdapter<String> adapter2; // listViewとデータをつなぐアダプター

    // ボタン判別用
    int intButtonId;

    // 検索用文字列
    String strSearchWord;

    // リスト
    ArrayList<String> stationList;
    ArrayList<String> timeTableDispList;

    // Http用
    HttpGetStationList httpGetStationList;
    HashMap<String, RailsInfoData> stationMap;
    HashMap<String, RailsInfoData> rosenMap = new HashMap<String, RailsInfoData>();
    public static HashMap<String, RailsInfoData> rosenStaticMap;

    boolean resultSingleFlag = false;

    // 洗濯した駅名
    String selectedStationName;

    // インテント
    Intent intent;


    ArrayAdapter<String> arrayAdapter; // プルダウンの選択肢を入れるアダプター

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View初期化
        editText = findViewById(R.id.editTextStation);
        spinner = findViewById(R.id.spinner);
        searchButton = findViewById(R.id.searchButton);
        //showRegisteredButton = findViewById(R.id.showRegisteredButton);
        button = findViewById(R.id.button);
        listView = findViewById(R.id.listView);

        // クリックリスナー
        searchButton.setOnClickListener(this);
        //showRegisteredButton.setOnClickListener(this);
        button.setOnClickListener(this);

        // 時刻表のリスト表示
        dispTimeTable();

        // ArrayList初期化
        stationList = new ArrayList<String>();

        // HTTPリクエスト用
        httpGetStationList = new HttpGetStationList();
        stationMap = new HashMap<String, RailsInfoData>();

        editText.addTextChangedListener(this);
        button.setEnabled(false);

        intent = new Intent(MainActivity.this, MainAddRosenActivity.class);



    }

    private void dispTimeTable() {
        timeTableDispList = new ArrayList<String>();
        File file = this.getFileStreamPath("index.txt");
        FileInputStream inputStream;
        boolean isExists = file.exists();
        String lineBuffer;
        try {
            if (isExists) {
                inputStream = openFileInput("index.txt");
                BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                while( (lineBuffer = reader.readLine()) != null ) {
                    System.out.println(lineBuffer);
                    timeTableDispList.add(lineBuffer);
                }
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, timeTableDispList);
        listView.setAdapter(adapter2);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();

        // 初期表示時には検索ボタンは使えない
        searchButton.setEnabled(false);
        Collections.sort(stationList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stationList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);

        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, timeTableDispList);
        listView.setAdapter(adapter2);

        // クリックリスナー
        searchButton.setOnClickListener(this);
        //showRegisteredButton.setOnClickListener(this);
        button.setOnClickListener(this);

        // 時刻表のリスト表示
        dispTimeTable();
    }

    @Override
    public void onClick(View v) {

        intButtonId = v.getId();

        if (intButtonId == R.id.searchButton) {
            strSearchWord = String.valueOf(editText.getText());
//
//            httpGetStationList.execute(strSearchWord);
//            stationMap = httpGetStationList.getStationMap();
//            for (String key : stationMap.keySet()) {
//                stationList.add(key);
//            }
//            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stationList);
//            listViewStation.setAdapter(adapter);
//            editText.setText("");
            new AsyncHttpRequest(this).execute(strSearchWord);
//            if (resultSingleFlag == true) {
//                intent = new Intent(MainActivity.this, MainAddRosenActivity.class);
//                intent.putExtra(getString(R.string.intent_key_status), getString(R.string.station_result_single));
//                MyApplication mApp = (MyApplication) this.getApplication();
//                mApp.setStationMap(stationMap);
//                startActivity(intent);
//            } else {
                //listViewStation.setOnItemClickListener(this);

            editText.setText("");
            button.setEnabled(true);
//            }
        } else if (intButtonId == R.id.button) {
            selectedStationName = (String)spinner.getSelectedItem();
            if(resultSingleFlag == false ) {
                RailsInfoData railsInfoData = stationMap.get(selectedStationName);
                String strStationUrl = railsInfoData.stationUrl;
                AsyncHttpRailsRequest asyncHttpRailsRequest = new AsyncHttpRailsRequest(this);
                //new AsyncHttpRailsRequest(this).execute(strStationUrl, selectedStationName);

                asyncHttpRailsRequest.setOnCallBack(new AsyncHttpRailsRequest.CallBackTask(){

                    @Override
                    public void CallBack(Object result) {
                        super.CallBack(result);
                        // ※１
                       // resultにはdoInBackgroundの返り値が入ります。
                        // ここからAsyncTask処理後の処理を記述します。
                        //rosenMap = (HashMap<String, RailsInfoData>)result;
                        //intent.putExtra(getString(R.string.station_name), selectedStationName);
                        //intent.putExtra(getString(R.string.station_url), strStationUrl);
                        //intent.putExtra(getString(R.string.station_result_status), getString(R.string.station_result_multi));
                        //startActivity(intent);
                    }

                });
                asyncHttpRailsRequest.execute(strStationUrl, selectedStationName);



//                intent.putExtra(getString(R.string.station_name), selectedStationName);
//                intent.putExtra(getString(R.string.station_url), strStationUrl);
//                intent.putExtra(getString(R.string.station_result_status), getString(R.string.station_result_multi));
//                startActivity(intent);
            } else {
                //intent.putExtra(getString(R.string.station_map), stationMap);
                intent.putExtra(getString(R.string.station_result_status), getString(R.string.station_result_single));
                intent.putExtra(getString(R.string.station_name), selectedStationName);
                startActivity(intent);
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        intent = new Intent(MainActivity.this, TimeTableListActivity.class);
        String filename = timeTableDispList.get(position) + ".txt";
        intent.putExtra(getString(R.string.timetable_list_file), filename);
        startActivity(intent);
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        listView.setAdapter(adapter);

        // 長押しした後のタップ処理を検知させないため
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(editText.getText().length() == 0)
        {
            searchButton.setEnabled(false);
            return;
        } else {
            searchButton.setEnabled(true);
            return;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void setRosenMap(HashMap<String, RailsInfoData> map) {
        this.rosenMap = map;
    }
}
