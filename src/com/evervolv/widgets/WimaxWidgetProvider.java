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
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.os.AsyncTask;
import android.net.wimax.WimaxHelper;
import android.net.wimax.WimaxManagerConstants;

import com.evervolv.widgets.R;

public class WimaxWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "Evervolv_WimaxWidget";
    private boolean DBG = false;

    public static String WIMAX_ENABLED_CHANGED = "com.htc.net.wimax.WIMAX_ENABLED_CHANGED";
    public static String FOURG_ENABLED_CHANGED = "android.net.fourG.NET_4G_STATE_CHANGED";
    public static String WIMAX_CHANGED = "com.evervolv.widgets.WIMAX_CLICKED";

    private static final StateTracker sWimaxState = new WimaxStateTracker();

    @Override
    public void onEnabled(Context context){
    	if (DBG) Log.d(TAG,"WimaxWidgetProvider::onEnabled");
		PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".WimaxWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
    	if (DBG) Log.d(TAG,"Received request to remove last widget");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".WimaxWidgetProvider"),
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
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (DBG) Log.d(TAG,"WimaxWidgetProvider::onUpdate appWidgetIds"+appWidgetIds);
    	updateWidget(context, appWidgetManager, appWidgetIds);
    }

    public void updateWidget(Context context){
    	if (DBG) Log.d(TAG,"WimaxWidgetProvider::updateWidget(context)");
        ComponentName thisWidget = new ComponentName(context, WimaxWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        updateWidget(context,appWidgetManager,appWidgetIds);
    }

    /**
	* this method will receive all Intents that it register fors in
	* the android manifest file.
	*/
    @Override
    public void onReceive(Context context, Intent intent){
    	super.onReceive(context, intent);
    	if (DBG) Log.d(TAG,"WimaxWidgetProvider::onReceive\n"+intent.getAction());

    	if (WIMAX_CHANGED.equals(intent.getAction())){
	    	int result = sWimaxState.getActualState(context);
	    	if (result == StateTracker.STATE_DISABLED){
    	    	sWimaxState.requestStateChange(context,true);
    	    } else if (result == StateTracker.STATE_ENABLED){
    	    	sWimaxState.requestStateChange(context,false);
    	    } else {
    	        // we must be between on and off so we do nothing
    	    }

        }

        if (WIMAX_ENABLED_CHANGED.equals(intent.getAction())) {
            int wimaxState = intent.getIntExtra(WimaxManagerConstants.CURRENT_WIMAX_ENABLED_STATE,
                    WimaxManagerConstants.NET_4G_STATE_UNKNOWN);
            updateWidgetView(context,WimaxStateTracker.wimaxStateToFiveState(wimaxState));
            sWimaxState.onActualStateChange(context, intent);
        } else if (FOURG_ENABLED_CHANGED.equals(intent.getAction())) {
            int wimaxState = intent.getIntExtra(WimaxManagerConstants.EXTRA_4G_STATE,
                    WimaxManagerConstants.NET_4G_STATE_UNKNOWN);
            updateWidgetView(context, WimaxStateTracker.wimaxStateToFiveState(wimaxState));
            sWimaxState.onActualStateChange(context, intent);
        }
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context, int state){

		if (DBG) Log.d(TAG,"WimaxWidgetProvider::updateWidgetView");
        Intent intent = new Intent(context, WimaxWidgetProvider.class);
		intent.setAction(WIMAX_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_wimax_icon);
        Log.d(TAG, "state: " + state);
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

    	ComponentName cn = new ComponentName(context, WimaxWidgetProvider.class);
        AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
	}

	/**
	* this method is called when the widget is added to the home
	* screen, and so it contains the initial setup of the widget.
	*/
    public void updateWidget(Context context,
    			 AppWidgetManager appWidgetManager,
    			 int[] appWidgetIds){

    	for (int i=0;i<appWidgetIds.length;i++){

	    	int appWidgetId = appWidgetIds[i];
	    	if (DBG) Log.d(TAG,"appWidgetId: "+appWidgetId);
			int wimaxState = sWimaxState.getActualState(context);
    		updateWidgetView(context,wimaxState);
		}

    }

    /**
     * Subclass of StateTracker to get/set WiMAX state.
     */
    private static final class WimaxStateTracker extends StateTracker {
        @Override
        public int getActualState(Context context) {
            if (WimaxHelper.isWimaxSupported(context)) {
                return wimaxStateToFiveState(WimaxHelper.getWimaxState(context));
            }
            return STATE_UNKNOWN;
        }

        @Override
        protected void requestStateChange(final Context context,
                final boolean desiredState) {
            if (!WimaxHelper.isWimaxSupported(context)) {
                Log.e(TAG, "WiMAX is not supported");
                return;
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    WimaxHelper.setWimaxEnabled(context, desiredState);
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            String action = intent.getAction();
            int wimaxState;

            if (action.equals(WimaxManagerConstants.NET_4G_STATE_CHANGED_ACTION)) {
                wimaxState = intent.getIntExtra(WimaxManagerConstants.EXTRA_4G_STATE,
                                                WimaxManagerConstants.NET_4G_STATE_UNKNOWN);
            } else if (action.equals(WimaxManagerConstants.WIMAX_ENABLED_CHANGED_ACTION)
                    || FOURG_ENABLED_CHANGED.equals(intent.getAction())) {
                wimaxState = intent.getIntExtra(WimaxManagerConstants.CURRENT_WIMAX_ENABLED_STATE,
                                                WimaxManagerConstants.NET_4G_STATE_UNKNOWN);
            } else {
                return;
            }
            int widgetState = wimaxStateToFiveState(wimaxState);
            setCurrentState(context, widgetState);
        }

        /**
         * Converts Wimax4GManager's state values into our
         * WiMAX-common state values.
         * Also compatible with WimaxController state values.
         */
        private static int wimaxStateToFiveState(int wimaxState) {
            switch (wimaxState) {
                case WimaxManagerConstants.NET_4G_STATE_DISABLED:
                    return StateTracker.STATE_DISABLED;
                case WimaxManagerConstants.NET_4G_STATE_ENABLED:
                    return StateTracker.STATE_ENABLED;
                case WimaxManagerConstants.NET_4G_STATE_ENABLING:
                    return StateTracker.STATE_TURNING_ON;
                case WimaxManagerConstants.NET_4G_STATE_DISABLING:
                    return StateTracker.STATE_TURNING_OFF;
                default:
                    return StateTracker.STATE_UNKNOWN;
            }
        }
    }
}
