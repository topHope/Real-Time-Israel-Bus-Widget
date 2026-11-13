package com.example.busrealtimewidget;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AppUtils {

    public static final String SavedStationsFileName = "TrackedStationsData.txt";
    public static final String FailToLoadStr = "&NA&";
    public static final String[] SAVE_SEPS = new String[]{"&SEP0&", "&SEP1&", "#"};


    //general read&write
    public static String ReadData(File Path_String, String SaveFileName) {

        File readFrom = new File(Path_String, SaveFileName);
        System.out.println("readPath pppp  " + readFrom.getAbsolutePath());


        byte[] content = new byte[(int) readFrom.length()];
        String Result = FailToLoadStr;


        try {
            FileInputStream stream = new FileInputStream(readFrom);
            stream.read(content);
            Result = new String(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result;
    }

    public static void SaveData(File SaveToPath, String SaveFileName, String SavedData) {


        File path = new File(SaveToPath, SaveFileName);
        System.out.println("savePath pppp  " + path.getAbsolutePath());


        try {
            FileOutputStream writer = new FileOutputStream(path);
            writer.write(SavedData.getBytes());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    //stations r&w

    //given a path, itll load the stations and returns the array of them
    public static ArrayList<BusStationObject> ReadSavedStations(File path) {


        String SaveString = ReadData(path, SavedStationsFileName);

        System.out.println("SSSS  " + SaveString);
        if (SaveString.equals(AppUtils.FailToLoadStr)) {
            return  new ArrayList<>();
        }


        ArrayList<BusStationObject> Stations = new ArrayList<>();
        if (Stations != null)
            Stations.clear();


        String[] LoadedStations = SaveString.split(AppUtils.SAVE_SEPS[0]);
        for (int i = 0; i < LoadedStations.length; i++) {
            System.out.println("test sssss    " + LoadedStations[i]);
            Stations.add(new BusStationObject(LoadedStations[i]));
        }

        return Stations;
    }

    public static BusStationObject ReadSavedStation_ByID(File path, String id) {

        ArrayList<BusStationObject> stations = AppUtils.ReadSavedStations(path);
        if (stations == null || stations.isEmpty()) return null;

        //find the station with the right id and return it
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getStationID().equals(id)) return stations.get(i);
        }

        //if didnt find any, return null
        return null;
    }

    public static BusStationObject ReadSavedStation_ByIndex(File path, int index) {
        ArrayList<BusStationObject> stations = AppUtils.ReadSavedStations(path);
        if (stations == null || stations.isEmpty()) return null;
        if (index >= stations.size()) return null;


        return stations.get(index);
    }


    //given a list of stations, itll save them to memory
    public static void SaveStations(File path, ArrayList<BusStationObject> stations) {

        String[] Saves = new String[stations.size()];
        for (int i = 0; i < stations.size(); i++) {
            Saves[i] = stations.get(i).getSaveString();
        }

        String SaveStr = String.join(SAVE_SEPS[0], Saves);
        System.out.println("test llll    " + SaveStr);

        SaveData(path, SavedStationsFileName, SaveStr);
    }


    //given a station id and a line id itll return the string needed to show on the eta text field
    public static String get_eta_text(String StationId, String BusID) {
        //return "" + StationId + " | " + BusID;

        String theURL = generateURL(StationId);

        String rawData = AppUtils.FailToLoadStr;
        try {
            rawData = fetchETAData(theURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(rawData.equals(AppUtils.FailToLoadStr))
            return AppUtils.FailToLoadStr;

        String result = AppUtils.FailToLoadStr;

        try {
            result = parseRawETAData(rawData).get(BusID).get(0);
        } catch (Exception e) {

        }

        return result;


    }

    //will create a thread to use.
    //returns the list of etas to the OnResult
    private static void callThread_getETA(String stationId, String busId, Consumer<List<String>> onResult) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            List<String> etas;
            try {
                String raw = fetchETAData(generateURL(stationId));
                Map<String, ArrayList<String>> etaByLine = parseRawETAData(raw);
                etas = etaByLine.get(busId);
                if (etas == null) {
                    etas = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                etas = new ArrayList<>();
            }

            List<String> result = etas;
            mainHandler.post(() -> onResult.accept(result));
        }).start();
    }

    //returns the eta of all routes in the given station
    //returns a dict of arrays of timings -> routeID : array<String> [x, x, x, x]
    public static Map<String, ArrayList<String>> getETA_Station(String stationID) {
        String theURL = generateURL(stationID);

        String rawData = AppUtils.FailToLoadStr;
        try {
            rawData = fetchETAData(theURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(rawData.equals(AppUtils.FailToLoadStr))
            return null;

        try {
            return parseRawETAData(rawData);
        } catch (Exception e) {

        }

        return null;
    }


    //given a station id, itll create the url needed to get the real time info on that
    //using busnearby (hopeon/busnearby/egg)
    private static String generateURL(String stationId) {
        ZonedDateTime today = ZonedDateTime.now(ZoneId.of("Asia/Jerusalem"));
        String key = today.getDayOfMonth() + "/" + (today.getMonthValue() - 1) + "/" + today.getYear();

        byte[] bytes = ("mref=" + stationId + "&ex=1").getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] ^ key.charAt(i % key.length()));
        }

        String encodedQuery = Base64.getEncoder().encodeToString(bytes);
        return "https://app.busnearby.co.il/stops?" + encodedQuery;
    }

    //will use the url to fetch the eta data
    //will return the raw eta data
    private static String fetchETAData(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Accept-Language", "he");
        connection.setRequestProperty("X-BNB-VARIANT", "busnearby");

        try {
            if (connection.getResponseCode() != 200) {
                throw new IOException("HTTP " + connection.getResponseCode());
            }

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String textLine;
                while ((textLine = reader.readLine()) != null) {
                    body.append(textLine);
                }
            }
            return body.toString();
        } finally {
            connection.disconnect();
        }
    }


    //given the raw eta data, itll give a dict based on routes id of list of etas
    // like this:   <route id> : array<string> [x, x, x, x]
    private static Map<String, ArrayList<String>> parseRawETAData(String rawJson) {
        Map<String, ArrayList<String>> etaByLine = new LinkedHashMap<>();
        OffsetDateTime now = OffsetDateTime.now();
        ZoneId israel = ZoneId.of("Asia/Jerusalem");
        DateTimeFormatter clock = DateTimeFormatter.ofPattern("HH:mm");

        JSONObject root;
        try {
            root = new JSONObject(rawJson);
        } catch (JSONException e) {
            return etaByLine;
        }

        Iterator<String> stopCodes = root.keys();
        while (stopCodes.hasNext()) {
            JSONArray arrivals = root.optJSONArray(stopCodes.next());
            if (arrivals == null) {
                continue;
            }

            for (int i = 0; i < arrivals.length(); i++) {
                JSONObject arrival = arrivals.optJSONObject(i);
                if (arrival == null || !arrival.has("line") || !arrival.has("expectedArrival")) {
                    continue;
                }

                String line = arrival.optString("line");
                OffsetDateTime eta = OffsetDateTime.parse(arrival.optString("expectedArrival"));
                long minutes = Duration.between(now, eta).toMinutes();

                if (minutes < 0) {
                    continue;
                }

                String shown = minutes > 15 ? eta.atZoneSameInstant(israel).format(clock) : String.valueOf(minutes);
                etaByLine.computeIfAbsent(line, k -> new ArrayList<>()).add(shown);
            }
        }

        return etaByLine;
    }


}
