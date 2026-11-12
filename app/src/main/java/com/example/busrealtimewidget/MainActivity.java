package com.example.busrealtimewidget;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



public class MainActivity extends AppCompatActivity {

    private static MainActivity MA_Instance;

    private StationObjectsViewer SOV;




    //region on app start

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MA_Instance = this;
        ReturnHomeButton();
    }

    private void SetUpStationsViewer() {

        RecyclerView StationViewer = findViewById(R.id.MainBusLayout);

        if(SOV == null)
            SOV = new StationObjectsViewer(this);
        StationViewer.setAdapter(SOV);
        StationViewer.setLayoutManager(new LinearLayoutManager(this));

    }

    //endregion



    private void ReturnHomeButton() {
        setContentView(R.layout.home_layout);
        SetUpStationsViewer();
        //SOV.LoadStationsList();

        findViewById(R.id.AddButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewStationButton();
            }
        });

        findViewById(R.id.ReloadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnHomeButton();
            }
        });



    }

    //region add new station buttons
    private void AddNewStationButton() {
        setContentView(R.layout.add_station_layout);


        findViewById(R.id.NewStation_CancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnHomeButton();
            }
        });

        findViewById(R.id.NewStation_addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmAddButton();
            }
        });


    }



    private void ConfirmAddButton() {


        EditText StationName = findViewById(R.id.NewStation_name);
        EditText StationID = findViewById(R.id.NewStation_ID);

        String NewStationNameText = StationName.getText().toString();
        String NewStationIDText = StationID.getText().toString();

        if(NewStationNameText.equals("") || NewStationIDText.equals(""))
            return;

        SOV.AddNewStation(NewStationNameText, NewStationIDText);

        ReturnHomeButton();

    }


    //endregion


    //region update station layout

    public static void OpenUpdateLayoutForStation(int stationNum) {
        MA_Instance.setContentView(R.layout.change_station_layout);



        //load
        EditText StationName = MA_Instance.findViewById(R.id.UpdateStation_Name);
        EditText Busses = MA_Instance.findViewById(R.id.UpdateStation_Stations);

        String LoadString = MA_Instance.SOV.getStationSaveString(stationNum);
        String[] params = LoadString.split(AppUtils.SAVE_SEPS[1]);



        StationName.setText(params[0]);

        if(params.length > 2) {
            Busses.setText(params[2]);
        } else {
            Busses.setText("");
        }

        //layout buttons

        MA_Instance.findViewById(R.id.UpdateStations_updateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MA_Instance.SOV.UpdateStation(stationNum, StationName.getText().toString(), "",  Busses.getText().toString().split("#"));
                MA_Instance.ReturnHomeButton();
            }
        });


        MA_Instance.findViewById(R.id.UpdateStation_CancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MA_Instance.ReturnHomeButton();
            }
        });
    }



    //endregion



}