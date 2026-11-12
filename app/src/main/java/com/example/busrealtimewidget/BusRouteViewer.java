package com.example.busrealtimewidget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BusRouteViewer extends RecyclerView.Adapter<BusRouteViewer.MyViewHolder>{

    Context context;
    private BusStationObject StationObject;

    public BusRouteViewer(Context context, BusStationObject stationObject) {
        this.context = context;
        StationObject = stationObject;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.bus_info_layout, parent, false);

        return new BusRouteViewer.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int CurPos = position;
        String routeID = StationObject.getBusIDInPos(CurPos);

        holder.RouteID.setText("" + routeID);
        holder.RouteImage.setImageResource(android.R.drawable.arrow_up_float);
        getBusRealTimeETA(StationObject.getStationID(), routeID, result -> {
            holder.ETA.setText(result);
         });

    }

    @Override
    public int getItemCount() {
        return this.StationObject.getBusRoutesAmount();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {


        TextView RouteID, ETA;
        ImageView RouteImage;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);


            RouteID = itemView.findViewById(R.id.LineID);
            ETA = itemView.findViewById(R.id.Eta);
            RouteImage = itemView.findViewById(R.id.LineImage);


        }
    }



    public void getBusRealTimeETA(String StationID, String BusID, Consumer<String> callback) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = AppUtils.get_eta_text(StationID, BusID);
            //result = getMinutesUntilNextBus(StationID, BusID);

            mainHandler.post(() -> callback.accept(result));
        });
    }

    //old eta real time loader using curlbus (search on github if u want...)
    public String getMinutesUntilNextBus(String stopId, String routeNumber) {
        try {
            String urlStr = "https://curlbus.app/" + stopId;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);


            /// //
            int code = conn.getResponseCode();
            if (code != 200) {
                String err = "";
                try (BufferedReader r = new BufferedReader(new InputStreamReader(
                        code >= 400 ? conn.getErrorStream() : conn.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) err += line;
                } catch (Exception ignore) {}
                System.out.println("" + stopId + "   HTTP " + code + " body: " + err);
                return "ER Code200 (" + code + ")";
            }
            /// //

            if (conn.getResponseCode() != 200) return "ER Code200";

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            JSONObject data = new JSONObject(sb.toString());
            if (data.has("errors") && !data.isNull("errors") && data.getJSONArray("errors").length() > 0) return "ER NL DTA";

            JSONObject visits = data.optJSONObject("visits");
            if (visits == null) return "ER NO Visits";
            JSONArray arrivals = visits.optJSONArray(stopId);
            if (arrivals == null) return "ER NO Arrivals";

            OffsetDateTime now = OffsetDateTime.now();
            int bestEta = Integer.MAX_VALUE;



            for (int i = 0; i < arrivals.length(); i++) {
                JSONObject a = arrivals.getJSONObject(i);
                if (!routeNumber.equals(a.optString("line_name"))) continue;

                String etaStr = a.optString("eta", null);
                if (etaStr == null) continue;

                int minutes = (int) Math.max(0, ChronoUnit.MINUTES.between(now, OffsetDateTime.parse(etaStr.replace(" ", "T"))));
                if (minutes < bestEta) bestEta = minutes;


            }

            //return String.valueOf(bestEta);
            return bestEta == Integer.MAX_VALUE ? "ER MaxValue" : "ETA: " + String.valueOf(bestEta) + "min";

        } catch (Exception e) {
            System.out.println("E5RROR ETA: " + "" + e);
            return "ER: " + e;
        }
    }
}
