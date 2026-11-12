package com.example.busrealtimewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import android.content.ComponentName;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;


public class BusInfoHomeScreenWidget extends AppWidgetProvider {


    private static final String OnClickLeft = "HS_ClickLeft", OnClickRight = "HS_ClickRight", OnClickRefresh = "HS_ClickRefresh";

    private static int CurShownStationNum = 0;
    private ArrayList<BusStationObject> AllStations;



    // Called when the widget needs to be updated (on interval, or when first added)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // Builds/refreshes the RemoteViews for a single widget instance
    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        //setup layout
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bus_info_home_screen_widget);
        views.setTextViewText(R.id.HSWidget_Station_name, AppUtils.FailToLoadStr);
        views.setTextViewText(R.id.HSWidget_Station_ID, "$" + CurShownStationNum);

        //setup buttons
        views.setOnClickPendingIntent(R.id.HSWidget_NextButton, getPendingSelfIntent(context, OnClickLeft));
        views.setOnClickPendingIntent(R.id.HSWidget_ReloadButton, getPendingSelfIntent(context, OnClickRefresh));
        System.out.println("wwww  id " +  widgetId);


        //setup list
        Intent serviceIntent = new Intent(context, HSListViewAdapter.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntent.putExtra("cur_shown_station_index", CurShownStationNum);


        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setRemoteAdapter(R.id.HS_BusesList, serviceIntent);


        UpdateTextsBasedOnStation(views);


        appWidgetManager.updateAppWidget(widgetId, views);
    }

    //clears the data shown
    private void ClearWidgetInfo(RemoteViews views) {
        views.setTextViewText(R.id.HSWidget_Station_name, "No station selected");
        views.setTextViewText(R.id.HSWidget_Station_ID, "" + AppUtils.FailToLoadStr + " $" +CurShownStationNum );
    }


    //updates the texts based on the currently selected station
    private void UpdateTextsBasedOnStation(RemoteViews views) {
        ClearWidgetInfo(views);
        checkShownStationNum();

        if(this.AllStations == null)
            return;

        BusStationObject curShownStation = this.AllStations.get(this.CurShownStationNum);

        if(curShownStation == null)
            return;;
        views.setTextViewText(R.id.HSWidget_Station_name, curShownStation.getName());
        views.setTextViewText(R.id.HSWidget_Station_ID, "" + curShownStation.getStationID() );

    }


    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    // Called when a widget broadcasts an action (e.g. your button click above)
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        switch (intent.getAction()) {

            case OnClickLeft:
                CurShownStationNum++;
                break;
            case OnClickRight:
                CurShownStationNum--;
                break;

            case OnClickRefresh:
                break;

            default:
                break;
        }

        refreshWidget(context);

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    //refreshes widget and reloads data
    private void refreshWidget(Context context) {

        //update loaded data
        this.AllStations = AppUtils.ReadSavedStations(context.getFilesDir());

        //make sure the number is valid
        checkShownStationNum();


        //update shown info on widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, BusInfoHomeScreenWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.HS_BusesList);
        }


    }

    //checks if the shown station num is a valid shown station,
    //if its not -> fix it
    //if there are no stations -> reset
    private void checkShownStationNum() {
        if(this.AllStations == null) {
            this.CurShownStationNum = 0;
            return;
        }

        if(this.CurShownStationNum < 0 || this.CurShownStationNum >= this.AllStations.size())
            this.CurShownStationNum = 0;
    }
}

