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
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class SyncWidgetProvider extends AppWidgetProvider{

    private static final StateTracker sSyncState = new SyncStateTracker();

    // TAG
    public static final String TAG = "Evervolv_SyncWidget";
    private boolean DBG = false;
    // Intent Actions
    public static String SYNC_STATE_CHANGED = "com.android.sync.SYNC_CONN_STATUS_CHANGED";
    public static String SYNC_CHANGED = "com.evervolv.widgets.SYNC_CLICKED";

    @Override
    public void onEnabled(Context context){
		PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".SyncWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
    	if (DBG) Log.d(TAG,"Received request to remove last widget");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".SyncWidgetProvider"),
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
	* this method will receive all Intents that it registers for in
	* the android manifest file.
	*/
    @Override
    public void onReceive(Context context, Intent intent){
    	if (DBG) Log.d(TAG, "onReceive - " + intent.toString());
    	super.onReceive(context, intent);
    	if (SYNC_CHANGED.equals(intent.getAction())){
	    	int result = sSyncState.getActualState(context);
	    	if (result == StateTracker.STATE_DISABLED){
    	    	sSyncState.requestStateChange(context,true);
    	    } else if (result == StateTracker.STATE_ENABLED){
    	    	sSyncState.requestStateChange(context,false);
    	    } else {
    	        // we must be between on and off so we do nothing
    	    }
    	}
        if (SYNC_STATE_CHANGED.equals(intent.getAction())){
            int syncState = sSyncState.getActualState(context);
            updateWidgetView(context,syncState);
            sSyncState.onActualStateChange(context,intent);
        }
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context,int state){

	    Intent intent = new Intent(context, SyncWidgetProvider.class);
		intent.setAction(SYNC_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_sync_icon);
		// We need to update the Widget GUI
		if (state == StateTracker.STATE_DISABLED){
            views.setImageViewResource(R.id.widget_indic, 0);
		} else if (state == StateTracker.STATE_ENABLED) {
            views.setImageViewResource(R.id.widget_indic,R
                    .drawable.widget_indic_on);
		} else if (state == StateTracker.STATE_UNKNOWN) {
            views.setImageViewResource(R.id.widget_indic, 0);
		}

		ComponentName cn = new ComponentName(context, SyncWidgetProvider.class);
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

			int syncState = sSyncState.getActualState(context);
    		updateWidgetView(context,syncState);
		}
    }

    /**
     * Gets the state of background data.
     *
     * @param context
     * @return true if enabled
     */
    private static boolean getBackgroundDataState(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getBackgroundDataSetting();
    }


    /**
     * Subclass of StateTracker for sync state.
     */
    private static final class SyncStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            boolean on = getBackgroundDataState(context) &&
                    ContentResolver.getMasterSyncAutomatically();
            return on ? STATE_ENABLED : STATE_DISABLED;
        }

        @Override
        public void onActualStateChange(Context context, Intent unused) {
            setCurrentState(context, getActualState(context));
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            final ConnectivityManager connManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final boolean backgroundData = getBackgroundDataState(context);
            final boolean sync = ContentResolver.getMasterSyncAutomatically();

            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... args) {
                    // Turning sync on.
                    if (desiredState) {
                        if (!backgroundData) {
                            connManager.setBackgroundDataSetting(true);
                        }
                        if (!sync) {
                            ContentResolver.setMasterSyncAutomatically(true);
                        }
                        return true;
                    }

                    // Turning sync off
                    if (sync) {
                        ContentResolver.setMasterSyncAutomatically(false);
                    }
                    return false;
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
