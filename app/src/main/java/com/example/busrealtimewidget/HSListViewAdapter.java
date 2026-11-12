package com.example.busrealtimewidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

public class HSListViewAdapter extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new HSListViewFactory(getApplicationContext(), intent);
    }

    static class HSListViewFactory implements RemoteViewsFactory {

        private final Context context;
        private int ShownStationIndex;
        private final List<String> Busses = new ArrayList<>();

        HSListViewFactory(Context context, Intent intent) {
            this.context = context;
            this.ShownStationIndex = intent.getIntExtra("cur_shown_station_index", -1);
        }

        @Override
        public void onCreate() {  }

        // Runs when notifyAppWidgetViewDataChanged is called. Off the main thread,
        // so reading the file (or fetching over the network) is fine here.
        @Override
        public void onDataSetChanged() {
            Busses.clear();
            System.out.println("wwww  shown id " + ShownStationIndex);


            BusStationObject station = AppUtils.ReadSavedStation_ByIndex(context.getFilesDir(), ShownStationIndex);

            if (station != null) {
                for (String busId : station.getBusIDs()) {
                    Busses.add("" + busId + ": " + AppUtils.get_eta_text(String.valueOf(station.getStationID()), busId) + " min");
                }
            }
        }

        @Override
        public int getCount() {
            return Busses.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.hs_list_element_layout);
            if (position >= 0 && position < Busses.size()) {
                rv.setTextViewText(R.id.HS_BUSID, Busses.get(position));
                //rv.setTextViewText(R.id.HS_BUSID, rows.get(position));
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onDestroy() {
            Busses.clear();
        }
    }
}