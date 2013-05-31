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
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class AirplaneModeWidgetProvider extends AppWidgetProvider {

    // TAG
    public static final String TAG = "Evervolv_AirplaneModeWidget";
    private boolean DBG = false;
    // Intent Actions
    public static String AIRPLANEMODE_CHANGED = "com.evervolv.widgets.AIRPLANEMODE_CLICKED";

    @Override
    public void onUpdate(Context context,
			 AppWidgetManager appWidgetManager,
			 int[] appWidgetIds){
    	if (DBG) Log.d(TAG, "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    	updateWidget(context, appWidgetManager, appWidgetIds);
    }

    /**
	* this method will receive all Intents that it registers for in
	* the android manifest file.
	*/
    @Override
    public void onReceive(Context context, Intent intent){
    	if (DBG) Log.d(TAG, "onReceive - " + intent.toString());
    	super.onReceive(context, intent);

    	if (AIRPLANEMODE_CHANGED.equals(intent.getAction())){
    		toggleState(context);
    	}
    	if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
			int airplanemodeState = getAirplaneModeState(context) ? 1 : 0;
    		updateWidgetView(context,airplanemodeState);
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

	    	//on or off
			int airplanemodeState = getAirplaneModeState(context) ? 1 : 0;
    		updateWidgetView(context,airplanemodeState);
		}
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context,int state){

	    Intent intent = new Intent(context, getClass());
		intent.setAction(AIRPLANEMODE_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_airplane_icon);
        // We need to update the Widget GUI
        if (state == StateTracker.STATE_DISABLED){
            views.setImageViewResource(R.id.widget_indic, 0);
        } else if (state == StateTracker.STATE_ENABLED) {
            views.setImageViewResource(R.id.widget_indic,R
                    .drawable.widget_indic_on);
        }

		ComponentName cn = new ComponentName(context, getClass());
		AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
	}

    /**
     * Gets the state of Airplane.
     *
     * @param context
     * @return true if enabled.
     */
    private static boolean getAirplaneModeState(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    /**
     * Toggles the state of Airplane
     *
     * @param context
     */
    public void toggleState(Context context) {
        boolean state = getAirplaneModeState(context);
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                state ? 0 : 1);
        // notify change
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", state);
        context.sendBroadcast(intent);
    }

}
