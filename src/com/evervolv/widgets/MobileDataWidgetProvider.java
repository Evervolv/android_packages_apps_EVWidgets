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
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class MobileDataWidgetProvider extends AppWidgetProvider {
    // TAG
    public static final String TAG = "Evervolv_MobileDataWidget";
    private boolean DBG = true;
    // Intent Actions
    public static String MOBILE_DATA_STATE_CHANGED = "com.android.internal.telephony.MOBILE_DATA_CHANGED";
    public static String MOBILE_DATA_CHANGED = "com.evervolv.widgets.MOBILE_DATA_CLICKED";

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
    	if (MOBILE_DATA_CHANGED.equals(intent.getAction())){
    		toggleState(context);

        	int dataState = getDataState(context) ? StateTracker.STATE_ENABLED : StateTracker.STATE_DISABLED;
            updateWidgetView(context,dataState);
    	}
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

			int dataState = getDataState(context) ? StateTracker.STATE_ENABLED : StateTracker.STATE_DISABLED;
    		updateWidgetView(context,dataState);
		}
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context,int state){

	    Intent intent = new Intent(context, getClass());
		intent.setAction(MOBILE_DATA_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_mobile_data_icon);
        // We need to update the Widget GUI
        if (state == StateTracker.STATE_DISABLED){
            views.setImageViewResource(R.id.widget_indic, 0);
        } else if (state == StateTracker.STATE_ENABLED) {
            views.setImageViewResource(R.id.widget_indic,R
                    .drawable.widget_indic_on);
        } else if (state == StateTracker.STATE_UNKNOWN) {
            views.setImageViewResource(R.id.widget_indic, 0);
        }

		ComponentName cn = new ComponentName(context, getClass());
		AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
	}

    /**
     * Gets the state of data
     *
     * @return true if enabled.
     */
    private static boolean getDataState(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            /* Make sure the state change propagates */
            Thread.sleep(100);
        } catch (java.lang.InterruptedException ie) {
        }
        return cm.getMobileDataEnabled();
    }

    /**
     * Toggles the state of data.
     */
    public void toggleState(Context context) {
        boolean enabled = getDataState(context);

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (enabled) {
            cm.setMobileDataEnabled(false);
        } else {
        	cm.setMobileDataEnabled(true);

        }
    }

}
