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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class GpsWidgetProvider extends AppWidgetProvider{

    // TAG
    public static final String TAG = "Evervolv_GpsWidget";
    private boolean DBG = false;
    // Intent Actions
    public static String GPS_STATE_CHANGED = "android.location.PROVIDERS_CHANGED";
    public static String GPS_CHANGED = "com.evervolv.widgets.GPS_CLICKED";
    private static final StateTracker sGpsState = new GpsStateTracker();

    @Override
    public void onEnabled(Context context){
		PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context.getPackageName(),
                GpsWidgetProvider.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
    	if (DBG) Log.d(TAG,"Received request to remove last widget");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context.getPackageName(),
                GpsWidgetProvider.class.getName()),
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
    	if (GPS_CHANGED.equals(intent.getAction())){
	    	int result = sGpsState.getActualState(context);
	    	if (result == StateTracker.STATE_DISABLED){
    	    	sGpsState.requestStateChange(context,true);
    	    } else if (result == StateTracker.STATE_ENABLED){
    	    	sGpsState.requestStateChange(context,false);
    	    } else {
    	        // we must be between on and off so we do nothing
    	    }
    	}
        if (GPS_STATE_CHANGED.equals(intent.getAction())){
            int gpsState = sGpsState.getActualState(context);
            updateWidgetView(context,gpsState);
            sGpsState.onActualStateChange(context,intent);
        }
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context,int state){

	    Intent intent = new Intent(context, GpsWidgetProvider.class);
		intent.setAction(GPS_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_gps_icon);
        // We need to update the Widget GUI
        if (state == StateTracker.STATE_DISABLED){
            views.setImageViewResource(R.id.widget_indic, 0);
        } else if (state == StateTracker.STATE_ENABLED) {
            views.setImageViewResource(R.id.widget_indic, R.drawable
                    .widget_indic_on);
        } else if (state == StateTracker.STATE_UNKNOWN) {
            views.setImageViewResource(R.id.widget_indic, 0);
        }

		ComponentName cn = new ComponentName(context, GpsWidgetProvider.class);
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

			int gpsState = sGpsState.getActualState(context);
    		updateWidgetView(context,gpsState);
		}
    }



    /**
     * Subclass of StateTracker for GPS state.
     */
    private static final class GpsStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            ContentResolver resolver = context.getContentResolver();
            boolean on = Settings.Secure.isLocationProviderEnabled(
                resolver, LocationManager.GPS_PROVIDER);
            return on ? STATE_ENABLED : STATE_DISABLED;
        }

        @Override
        public void onActualStateChange(Context context, Intent unused) {
            // Note: the broadcast location providers changed intent
            // doesn't include an extras bundles saying what the new value is.
            setCurrentState(context, getActualState(context));
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            final ContentResolver resolver = context.getContentResolver();
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... args) {
                    Settings.Secure.setLocationProviderEnabled(
                        resolver,
                        LocationManager.GPS_PROVIDER,
                        desiredState);
                    return desiredState;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    setCurrentState(
                        context,
                        result ? STATE_ENABLED : STATE_DISABLED);
                }
            }.execute();
        }
    }

}
