package com.ahagari.howmanyminutesleft;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainAddRosenActivity extends AppCompatActivity implements View.OnClickListener {


    Spinner spinner2;
    Spinner spinner3;
    Button registerButton;
    Button backButton2;
    HashMap<String, RailsInfoData> rosenMap;
    HashMap<String, RailsInfoData> stationMap;
    ArrayList<RailsInfoData> destList;

    String status;
    String strStationUrl;
    String selectedStationName;
    String selectedDestination;
    String selectedRail;

    String filename;
    boolean resultSingleFlag;

    RailsDataMap railsDataMap;
    ArrayList<String> rosenList = new ArrayList<String>();


    ArrayAdapter<String> arrayAdapter; // プルダウンの選択肢を入れるアダプター
    ArrayAdapter<String> arrayAdapter2;

    // インテント
    Intent intent2;

    // test
    ParcelTest pTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_add_rosen);

        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        registerButton = findViewById(R.id.registerButton);
        backButton2 = findViewById(R.id.backButton2);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        status = bundle.getString(getString(R.string.station_result_status));
        strStationUrl = bundle.getString(getString(R.string.station_url));
        selectedStationName = bundle.getString(getString(R.string.station_name));

        intent2 = new Intent(MainAddRosenActivity.this, TimeTableListActivity.class);

        registerButton.setOnClickListener(this);
        backButton2.setOnClickListener(this);
        arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        spinner2.setOnItemSelectedListener(new SpinnerSelectedListener());
        if(status.equals(getString(R.string.station_result_multi))){
            //new AsyncHttpRailsRequest(this).execute(strStationUrl, selectedStationName);

//            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./rosenMap.txt"))) {
//                 rosenMap= (HashMap<String, RailsInfoData>) ois.readObject();
//            } catch (IOException | ClassNotFoundException e) {
//                e.getMessage();
//            }
            rosenMap = (HashMap<String, RailsInfoData> )bundle.getSerializable("rosenMap");
           // rosenMap = (HashMap<String, RailsInfoData>)bundle.getSerializable("rosenmap");
            //Bundle bundle2 = new Bundle();
            //railsDataMap = bundle2.getParcelable("rosenMap");
            //pTest = bundle2.getParcelable("test");

            setMuitiRailsSpinner();

        } else {
            stationMap = (HashMap<String, RailsInfoData> )bundle.getSerializable(getString(R.string.station_map));
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
            setSingleRailsSpinner();
            //arrayAdapter.add(selectedStationName);
            spinner2.setAdapter(arrayAdapter);

        }
        //stationMap = (HashMap)bundle.getSerializable(getString(R.string.rosen_map));

        //arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        //setMuitiRailsSpinner();
        MyApplication mApp = (MyApplication) this.getApplication();
        System.out.println("map end.");

    }

    @Override
    protected void onResume(){
        super.onResume();;
        if(status.equals(getString(R.string.station_result_multi))){
            setMuitiRailsSpinner();
        } else {
            spinner2.setAdapter(arrayAdapter);

        }
    }

    private void setSingleRailsSpinner() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        RailsInfoData railsInfoData;
        ArrayList<String> arrayList = new ArrayList<String>();
        if(stationMap != null && !stationMap.isEmpty()) {
            for(String key: stationMap.keySet()){
                railsInfoData = stationMap.get(key);
                arrayList.add(railsInfoData.railName);
            }
            for(String s: arrayList) {
                if(!rosenList.contains(s)){
                    rosenList.add(s);
                }
            }
            Collections.sort(rosenList);
            for(String s: rosenList){
                arrayAdapter.add(s);
            }
            spinner2.setAdapter(arrayAdapter);
        }
    }

    private void setMuitiRailsSpinner() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        RailsInfoData railsInfoData;
        ArrayList<String> arrayList = new ArrayList<String>();
        if(rosenMap != null && !rosenMap.isEmpty()) {
            for(String key: rosenMap.keySet()) {
                railsInfoData = rosenMap.get(key);
                arrayList.add(railsInfoData.railName);
            }
            for(String s: arrayList) {
                if(!rosenList.contains(s)){
                    rosenList.add(s);
                }
            }
            Collections.sort(rosenList);
            for(String s: rosenList){
                arrayAdapter.add(s);
            }
            spinner2.setAdapter(arrayAdapter);
        }
    }

    private void setDestSpinner(){

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.registerButton) {
            HashMap<String, RailsInfoData> railsMap;
            if(status.equals(getString(R.string.station_result_multi))){
                railsMap = rosenMap;
            } else {
                railsMap = stationMap;
            }
            registerButton.setEnabled(false);
            selectedDestination = spinner3.getSelectedItem().toString();

            RailsInfoData sendData = new RailsInfoData();
            for(RailsInfoData mapValue: railsMap.values()){
                if(selectedRail.equals(mapValue.railName) &&
                        selectedDestination.equals(mapValue.destinationName)){
                    sendData = mapValue;
                }
            }
            new AsyncHttpTimeTableRequest(this).execute(sendData.destUrl);
        } else  {
            finish();
        }
    }

    public class SpinnerSelectedListener implements android.widget.AdapterView.OnItemSelectedListener{
        public void onItemSelected(AdapterView parent, View view, int position, long id) {
            // Spinner を取得
            Spinner spinner = (Spinner) parent;
            // 選択されたアイテムのテキストを取得
            selectedRail = spinner.getSelectedItem().toString();
            destList = new ArrayList<RailsInfoData>();
            if(status.equals(getString(R.string.station_result_multi))) {
                //TextView textView1 = (TextView)findViewById(R.id.textView1);
                for (RailsInfoData mapValue : rosenMap.values()) {
                    if (selectedRail.equals(mapValue.railName)) {
                        destList.add(mapValue);
                    }
                }

            } else {
                for(RailsInfoData mapValue : stationMap.values()){
                    if (selectedRail.equals(mapValue.railName)) {
                        destList.add(mapValue);
                    }
                }
            }
            arrayAdapter2.clear();
            for (int i = 0; i < destList.size(); i++) {
                arrayAdapter2.add(destList.get(i).destinationName);
            }
            spinner3.setAdapter(arrayAdapter2);
            //textView1.setText(str);
        }

        // 何も選択されなかった時の動作
        public void onNothingSelected(AdapterView parent) {
        }
    }

    public void saveTimeTableFile(Object o) {
        FileOutputStream outputStream;
        FileInputStream inputStream;
        String lineBuffer;

        ArrayList<TimeTableData> tList = (ArrayList<TimeTableData>)o;

        filename = String.format("%s-%s-%s.txt", selectedRail, selectedStationName, selectedDestination);


        try {

            File file = this.getFileStreamPath("index.txt");
            boolean isExists = file.exists();
            if(!isExists){
                outputStream = openFileOutput("index.txt", Context.MODE_PRIVATE);
                outputStream.close();
            }
            inputStream = openFileInput("index.txt");

            boolean fileExistFlag = false;
            BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            String indexTxtContent = filename.replace(".txt", "");
            while( (lineBuffer = reader.readLine()) != null ){
                if(indexTxtContent.equals(lineBuffer)){
                    fileExistFlag = true;
                }
            }

            if(!fileExistFlag){
                outputStream = openFileOutput("index.txt", Context.MODE_APPEND);
                String outputStr = indexTxtContent;
                outputStream.write(outputStr.getBytes());
                outputStream.write("\n".getBytes());
                outputStream.close();
            }
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            for(int i=0; i < tList.size(); i++){
                TimeTableData tData = tList.get(i);
                String contentLine = String.format("%s,%s,%s,%s,%s\n",
                        tData.hour, tData.minute, tData.jikoku, tData.type, tData.destination);
                outputStream.write(contentLine.getBytes());
            }
//            fileContents = fileContents + "\n";
//            outputStream.write(fileContents.getBytes());
//            outputStream.write("ああああiiiiii".getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            inputStream = openFileInput(filename);

            BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            while( (lineBuffer = reader.readLine()) != null ){
                System.out.println(lineBuffer);
            }
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }

        intent2.putExtra(getString(R.string.timetable_list_file), filename);
        startActivity(intent2);
        registerButton.setEnabled(true);
    }

}
