package com.example.busrealtimewidget;

import android.os.Handler;
import android.os.Looper;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//holds the info on a station
public class BusStationObject {


    private String Name;
    private String StationID;



    private ArrayList<String> BusIDs;

    //dict of timings. keys are bus id and values are a list of timeings
    private Map<String, ArrayList<String>> RoutesTimeings_ThisStation;

    private Instant LastRoutesUpdateTime;


    public BusStationObject(String name, String stationID, String... busIDs) {
        Name = name;
        StationID = stationID;

        this.BusIDs = new ArrayList<String>();
        for(String cur : busIDs) {
            this.BusIDs.add(cur);
        }

        this.RoutesTimeings_ThisStation = new LinkedHashMap<>();
        this.LastRoutesUpdateTime = Instant.now().plusSeconds(-100);
        this.UpdateETAs();

    }

    //creates a new object based on loaded string
    public BusStationObject(String LoadString) {
        String[] Splits = LoadString.split(AppUtils.SAVE_SEPS[1]);

        System.out.println("test tttt:   " + Splits.length);
        System.out.println("test tttt:   " + Splits[0] + " || " + Splits[1]); //+ " || " + Splits[2]);

        this.Name = Splits[0];
        this.StationID = Splits[1];

        this.BusIDs = new ArrayList<String>();

        if(Splits.length > 2) {
            String[] busses = Splits[2].split(AppUtils.SAVE_SEPS[2]);
            for (String cur : busses)
                this.BusIDs.add(cur);

        }



    }


    public String getName() {
        return Name;
    }

    public String getStationID() {
        return StationID;
    }

    public String getBusIDInPos(int pos) {
        if(this.BusIDs.size() <= 0)
            return "EEEEE";

        if(pos < 0 || pos >= this.BusIDs.size())
            return this.BusIDs.get(0);

        return this.BusIDs.get(pos);
    }

    public int getBusRoutesAmount() {
        return this.BusIDs.size();
    }


    public String getSaveString() {
        String SaveSep = AppUtils.SAVE_SEPS[1] , BussesSep = AppUtils.SAVE_SEPS[2];

        String BussesSaveString = String.join(BussesSep, this.BusIDs);
        return this.Name + SaveSep + this.StationID + SaveSep + BussesSaveString;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setBusIDs(String... busIDs) {
        this.BusIDs = new ArrayList<String>();

        for(String cur : busIDs) {
            this.BusIDs.add(cur);
        }
    }

    public ArrayList<String> getBusIDs() {
        return BusIDs;
    }


    //updates this object's timings data
    public void UpdateETAs() {

        if(this.LastRoutesUpdateTime == null)
            this.LastRoutesUpdateTime = Instant.now().plusSeconds(-100);


        //if the last update time was less then x sec ago, dont update
        if(!this.LastRoutesUpdateTime.isBefore(Instant.now().minusSeconds(5)))
            return;


        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Map<String, ArrayList<String>> result = AppUtils.getETA_Station(this.StationID);

            mainHandler.post(() -> this.RoutesTimeings_ThisStation = result);
        });

        this.LastRoutesUpdateTime = Instant.now();
    }

    //returns the array of timings for that route
    public ArrayList<String> getRouteTimings(int ListIndex) {
        return  getRouteTimings(this.BusIDs.get(ListIndex));
    }
    public ArrayList<String> getRouteTimings(String RouteID) {

       this.UpdateETAs();

        //if the is no route timings
        if(this.RoutesTimeings_ThisStation == null)
        {
            return new ArrayList<>(Arrays.asList(AppUtils.FailToLoadStr));
        }

        //if the given route id doesnt have any timeings
        if(!this.RoutesTimeings_ThisStation.containsKey(RouteID))
            return new ArrayList<>(Arrays.asList("No route timings"));;





        return this.RoutesTimeings_ThisStation.get(RouteID);

    }
}
