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
        private final List<String> rows_String = new ArrayList<>();

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
            rows_String.clear();
            System.out.println("wwww  shown id " + ShownStationIndex);


            BusStationObject station = AppUtils.ReadSavedStation_ByIndex(context.getFilesDir(), ShownStationIndex);

            if (station != null) {
                for (String busId : station.getBusIDs()) {
                    rows_String.add("" + busId + ": " + station.getRouteTimings(busId).get(0) + " min");
                }
            }
        }

        @Override
        public int getCount() {
            return rows_String.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.hs_list_element_layout);
            if (position >= 0 && position < rows_String.size()) {
                rv.setTextViewText(R.id.HS_BUSID, rows_String.get(position));
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
            rows_String.clear();
        }
    }
}