package com.evervolv.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.AsyncTask;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class WifiWidgetProvider extends AppWidgetProvider {
    // TAG
    public static final String TAG = "Evervolv_WifiWidget";
    private boolean DBG = false;
    // Intent Actions
    public static String WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED";
    public static String WIFI_CHANGED = "com.evervolv.wifi.WIFI_CLICKED";

    private static final StateTracker sWifiState = new WifiStateTracker();

    @Override
    public void onEnabled(Context context){
		PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".WifiWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
    	if (DBG) Log.d(TAG,"Received request to remove last widget");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".WifiWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context,appWidgetIds);
        if (DBG) Log.d(TAG,"Received request to remove a widget");
    }

    @Override
    public void onUpdate(Context context,
			 AppWidgetManager appWidgetManager,
			 int[] appWidgetIds){
    	if (DBG) Log.d(TAG, "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    	updateWidget(context, appWidgetManager, appWidgetIds);
    }


    /**
	* this method will receive all Intents that it register fors in
	* the android manifest file.
	*/
    @Override
    public void onReceive(Context context, Intent intent){
    	if (DBG) Log.d(TAG, "onReceive - " + intent.toString());
    	super.onReceive(context, intent);
    	if (WIFI_CHANGED.equals(intent.getAction())){
	    	int result = sWifiState.getActualState(context);
	    	if (result == StateTracker.STATE_DISABLED){
    	    	sWifiState.requestStateChange(context,true);
    	    } else if (result == StateTracker.STATE_ENABLED){
    	    	sWifiState.requestStateChange(context,false);
    	    } else {
    	        // we must be between on and off so we do nothing
    	    }
    	}
        if (WIFI_STATE_CHANGED.equals(intent.getAction())){
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            updateWidgetView(context,WifiStateTracker.wifiStateToFiveState(wifiState));
            sWifiState.onActualStateChange(context,intent);
        }
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context,int state){

	    Intent intent = new Intent(context, WifiWidgetProvider.class);
		intent.setAction(WIFI_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.power_widget);
	    views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

		views.setImageViewResource(R.id.widget_icon, R.drawable.widget_wifi_icon);
		// We need to update the Widget GUI
		if (state == StateTracker.STATE_DISABLED){
			views.setImageViewResource(R.id.widget_indic, 0);
		} else if (state == StateTracker.STATE_ENABLED) {
			views.setImageViewResource(R.id.widget_indic,R
			        .drawable.widget_indic_on);
		} else if (state == StateTracker.STATE_TURNING_ON) {
			views.setImageViewResource(R.id.widget_indic,R
			        .drawable.widget_indic_tween);
		} else if (state == StateTracker.STATE_TURNING_OFF) {
			views.setImageViewResource(R.id.widget_indic,R
			        .drawable.widget_indic_tween);
		} else if (state == StateTracker.STATE_UNKNOWN) {
			views.setImageViewResource(R.id.widget_indic, 0);
		}

		ComponentName cn = new ComponentName(context, WifiWidgetProvider.class);
		AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
	}

	/**
	* this method is called when the widget is added to the home
	* screen, and so it contains the initial setup of the widget.
	*/
    public void updateWidget(Context context,
    			 AppWidgetManager appWidgetManager,
    			 int[] appWidgetIds){
    	for (int i=0;i<appWidgetIds.length;++i){

	    	int appWidgetId = appWidgetIds[i];

			int wifiState = sWifiState.getActualState(context);
    		updateWidgetView(context,wifiState);
		}
    }

    /**
     * Subclass of StateTracker to get/set WiFistate.
     */
    private static final class WifiStateTracker extends StateTracker {

        /**
        * Uses the WifiManager to get the actual state
        */
        @Override
        public int getActualState(Context context) {
        	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                return wifiStateToFiveState(wifiManager.getWifiState());
            }
            return StateTracker.STATE_UNKNOWN;
        }

        /**
        * Request the change of the wifi between on/off
        */
        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
            final WifiManager wifiManager =
                    (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Log.d(TAG, "No wifiManager.");
                return;
            }

            // Actually request the wifi change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    /**
                     * Disable tethering if enabling Wifi
                     */
                    int wifiApState = wifiManager.getWifiApState();
                    if (desiredState && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                                         (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                        wifiManager.setWifiApEnabled(null, false);
                    }

                    wifiManager.setWifiEnabled(desiredState);
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            setCurrentState(context, wifiStateToFiveState(wifiState));
        }

        /**
         * Converts WifiManager's state values into our
         * WiFi-common state values.
         */
        private static int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    return StateTracker.STATE_DISABLED;
                case WifiManager.WIFI_STATE_ENABLED:
                    return StateTracker.STATE_ENABLED;
                case WifiManager.WIFI_STATE_DISABLING:
                    return StateTracker.STATE_TURNING_OFF;
                case WifiManager.WIFI_STATE_ENABLING:
                    return StateTracker.STATE_TURNING_ON;
                default:
                    return StateTracker.STATE_UNKNOWN;
            }
        }
    }

}
