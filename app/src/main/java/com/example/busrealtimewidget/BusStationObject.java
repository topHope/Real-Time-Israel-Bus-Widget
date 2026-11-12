package com.example.busrealtimewidget;

import java.util.ArrayList;

public class BusStationObject {


    private String Name;
    private String StationID;



    private ArrayList<String> BusIDs;


    public BusStationObject(String name, String stationID, String... busIDs) {
        Name = name;
        StationID = stationID;
        this.BusIDs = new ArrayList<String>();

        for(String cur : busIDs) {
            this.BusIDs.add(cur);
        }

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
}
