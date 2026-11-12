package com.example.busrealtimewidget;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busrealtimewidget.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
public class StationObjectsViewer  extends RecyclerView.Adapter<StationObjectsViewer.MyViewHolder> {

    Context context;
    private ArrayList<BusStationObject> Stations;


    public StationObjectsViewer(Context context) {
        this.context = context;
        this.Stations = new ArrayList<BusStationObject>();

        LoadStationsList();
        //AddTestObjects();
    }

    @NonNull
    @Override
    public StationObjectsViewer.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.bus_station_object_layout, parent, false);

        return new StationObjectsViewer.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationObjectsViewer.MyViewHolder holder, int position) {
        int CurPos = position;

        BusStationObject curStationInfo = this.Stations.get(CurPos);

        holder.StationName.setText("" + curStationInfo.getName());
        holder.StationID.setText("ID: " + curStationInfo.getStationID());
        holder.StationImage.setImageResource(android.R.drawable.ic_menu_camera);


        BusRouteViewer BRV = new BusRouteViewer(this.context, curStationInfo);
        holder.BusesView.setAdapter(BRV);
        holder.BusesView.setLayoutManager(new LinearLayoutManager(this.context));


        holder.ChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.OpenUpdateLayoutForStation(CurPos);
            }
        });

        holder.RemoveStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveStation(CurPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.Stations.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {


        TextView StationName, StationID;
        Button ChangeButton, RemoveStationButton;
        ImageView StationImage;

        RecyclerView BusesView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);


            StationName = itemView.findViewById(R.id.StationName);
            StationID = itemView.findViewById(R.id.StationID);
            StationImage = itemView.findViewById(R.id.StationImage);
            ChangeButton = itemView.findViewById(R.id.ChangeButton);
            RemoveStationButton = itemView.findViewById(R.id.RemoveStationButton);
            BusesView = itemView.findViewById(R.id.Station_BusRecycler);


        }
    }


    private void AddTestObjects() {
        this.Stations.add(new BusStationObject("back home", "20838" , "72", "172"));
        this.Stations.add(new BusStationObject("Morasha south", "27084" , "614", "49", "613", "567"));
        this.Stations.add(new BusStationObject("test 1", "11111" , "100", "101", "102", "103"));
        this.Stations.add(new BusStationObject("test 2", "22222"));
        this.Stations.add(new BusStationObject("test 3", "33333" , "234"));
        this.Stations.add(new BusStationObject("test 4", "44444" , "100", "101", "102", "103"));
        this.Stations.add(new BusStationObject("test 5", "44555" , "100", "2342", "102", "103", "234 234", "234", "1", "34"));
    }

    public void AddNewStation(String name, String ID, String... busID) {
        this.Stations.add(new BusStationObject(name, ID, busID));

        notifyDataSetChanged();
        SaveStationsList();
    }

    public void RemoveStation(int pos) {


        this.Stations.remove(pos);

        notifyDataSetChanged();
        SaveStationsList();


    }

    //region save&load
   public String getStationSaveString(int pos) {
       return this.Stations.get(pos).getSaveString();
    }

    //doesnt update station id
    public void UpdateStation(int pos, String name, String id, String... Busses) {
        this.Stations.get(pos).setName(name);
        this.Stations.get(pos).setBusIDs(Busses);

        SaveStationsList();
    }


    public void SaveStationsList() {
        AppUtils.SaveStations(context.getFilesDir(), this.Stations);
    }

    public void LoadStationsList() {

        ArrayList<BusStationObject> loadedStations = AppUtils.ReadSavedStations(context.getFilesDir());

        //if it didnt fidn any, just clear the list
        if(loadedStations == null)
            this.Stations.clear();

        this.Stations = loadedStations;
    }

    //endregion
}
