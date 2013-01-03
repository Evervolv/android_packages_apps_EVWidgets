/*
 * Copyright (C) 2012 The Evervolv Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evervolv.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class WifiApWidgetProvider extends AppWidgetProvider {
    // TAG
    public static final String TAG = "Evervolv_WifiApWidget";
    private boolean DBG = false;
    // Intent Actions
    public static String WIFIAP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static String WIFIAP_CHANGED = "com.evervolv.widgets.WIFIAP_CLICKED";

    private static final StateTracker sWifiApState = new WifiApStateTracker();

    @Override
    public void onEnabled(Context context){
		PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context.getPackageName(),
                WifiApWidgetProvider.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
    	if (DBG) Log.d(TAG,"Received request to remove last widget");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context.getPackageName(),
                WifiApWidgetProvider.class.getName()),
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
    	if (WIFIAP_CHANGED.equals(intent.getAction())){
	    	int result = sWifiApState.getActualState(context);
	    	if (result == StateTracker.STATE_DISABLED){
    	    	sWifiApState.requestStateChange(context,true);
    	    } else if (result == StateTracker.STATE_ENABLED){
    	    	sWifiApState.requestStateChange(context,false);
    	    } else {
    	        // we must be between on and off so we do nothing
    	    }
    	}
        if (WIFIAP_STATE_CHANGED.equals(intent.getAction())){
            int wifiApState = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, -1);
            updateWidgetView(context,WifiApStateTracker.wifiApStateToFiveState(wifiApState));
            sWifiApState.onActualStateChange(context,intent);
        }
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context,int state){

	    Intent intent = new Intent(context, WifiApWidgetProvider.class);
		intent.setAction(WIFIAP_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_wifiap_icon);
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

		ComponentName cn = new ComponentName(context, WifiApWidgetProvider.class);
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

			int wifiApState = sWifiApState.getActualState(context);
    		updateWidgetView(context,wifiApState);
		}
    }

    /**
     * Subclass of StateTracker to get/set Wifi AP state.
     */
    private static final class WifiApStateTracker extends StateTracker {
        @Override
        public int getActualState(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                return wifiApStateToFiveState(wifiManager.getWifiApState());
            }
            return StateTracker.STATE_UNKNOWN;
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {

            final WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Log.d(TAG, "No wifiManager.");
                return;
            }

            // Actually request the Wi-Fi AP change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    /**
                     * Disable Wifi if enabling tethering
                     */
                    int wifiState = wifiManager.getWifiState();
                    if (desiredState
                            && ((wifiState == WifiManager.WIFI_STATE_ENABLING) || (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
                        wifiManager.setWifiEnabled(false);
                    }

                    wifiManager.setWifiApEnabled(null, desiredState);
                    return null;
                }
            }.execute();
        }

		@Override
		public void onActualStateChange(Context context, Intent intent) {

            if (!WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, -1);
            setCurrentState(context, wifiApStateToFiveState(wifiState));
		}
        /**
         * Converts WifiManager's state values into our
         * Wifi/WifiAP/Bluetooth-common state values.
         */
        private static int wifiApStateToFiveState(int wifiState) {
            switch (wifiState) {
                case WifiManager.WIFI_AP_STATE_DISABLED:
                    return StateTracker.STATE_DISABLED;
                case WifiManager.WIFI_AP_STATE_ENABLED:
                    return StateTracker.STATE_ENABLED;
                case WifiManager.WIFI_AP_STATE_DISABLING:
                    return StateTracker.STATE_TURNING_OFF;
                case WifiManager.WIFI_AP_STATE_ENABLING:
                    return StateTracker.STATE_TURNING_ON;
                default:
                    return StateTracker.STATE_UNKNOWN;
            }
        }


    }
}
