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
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class AutoRotateWidgetProvider  extends AppWidgetProvider{

    // TAG
    public static final String TAG = "Evervolv_AutoRotateWidget";
    private boolean DBG = false;
    // Intent Actions
    public static String AUTOROTATE_CHANGED = "com.evervolv.widgets.ORIENTATION_CLICKED";

    private Context mContext;
    private WidgetSettingsObserver mObserver = null;

    @Override
    public void onEnabled(Context context){
        mContext = context;
        mObserver = new WidgetSettingsObserver(new Handler());
        mObserver.observe();
		PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".AutoRotateWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager
                .DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
        if (DBG) Log.d(TAG,"Received request to remove last widget");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.evervolv.widgets",
                ".AutoRotateWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager
                .DONT_KILL_APP);
        if (mObserver != null) {
            mObserver.unobserve();
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context,appWidgetIds);
        if (DBG) Log.d(TAG,"Received request to remove a widget");
        if (mObserver != null) {
            mObserver.unobserve();
        }
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
    	if (AUTOROTATE_CHANGED.equals(intent.getAction())){
    	    toggleOrientationState(context);
    	}
    }

	/**
	* this method is called when the widget is added to the home
	* screen, and so it contains the initial setup of the widget.
	*/
    public void updateWidget(Context context,
    			 AppWidgetManager appWidgetManager,
    			 int[] appWidgetIds) {
    	for (int i=0;i<appWidgetIds.length;++i){

	    	int appWidgetId = appWidgetIds[i];

	    	//on or off
			boolean orientationState = getAutoRotationState(context);
    		updateWidgetView(context, orientationState ? 1 : 0);
		}
    }

	/**
	* Method to update the widgets GUI
	*/
	private void updateWidgetView(Context context, int state) {

	    Intent intent = new Intent(context, AutoRotateWidgetProvider.class);
		intent.setAction(AUTOROTATE_CHANGED);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	            0, intent, 0);
	    RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);

        views.setImageViewResource(R.id.widget_icon, R.drawable
                .widget_auto_rotate_icon);

        if (state == StateTracker.STATE_DISABLED){
            views.setImageViewResource(R.id.widget_indic, 0);
        } else if (state == StateTracker.STATE_ENABLED) {
            views.setImageViewResource(R.id.widget_indic,R
                    .drawable.widget_indic_on);
        }

		ComponentName cn = new ComponentName(context,
		        AutoRotateWidgetProvider.class);
		AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
	}

    protected static void toggleOrientationState(Context context) {
        setAutoRotation(!getAutoRotationState(context));
    }

    private static boolean getAutoRotationState(Context context) {
        ContentResolver cr = context.getContentResolver();
        return 0 != Settings.System.getInt(cr, Settings.System
                .ACCELEROMETER_ROTATION, 0);
    }

    private static void setAutoRotation(final boolean autorotate) {
        AsyncTask.execute(new Runnable() {
                public void run() {
                    try {
                        IWindowManager wm = IWindowManager.Stub.asInterface(
                                ServiceManager.getService(Context
                                        .WINDOW_SERVICE));
                        if (autorotate) {
                            wm.thawRotation();
                        } else {
                            wm.freezeRotation(-1);
                        }
                    } catch (RemoteException exc) {
                        Log.w(TAG, "Unable to save auto-rotate setting");
                    }
                }
            });
    }

    private class WidgetSettingsObserver extends ContentObserver {
        public WidgetSettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(Settings
                    .System.ACCELEROMETER_ROTATION), false, this);
        }

        public void unobserve() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChangeUri(Uri uri, boolean selfChange) {
            updateWidgetView(mContext, getAutoRotationState(mContext) ? 1 : 0);
        }
    }

}