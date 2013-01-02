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
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class GoToSleepWidgetProvider extends AppWidgetProvider{

    // TAG
    public static final String TAG = "Evervolv_GoToSleepWidget";
    private boolean DBG = false;
    // Intent Actions
    public static String GOTOSLEEP_CHANGED = "com.evervolv.widgets.GOTOSLEEP_CLICKED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
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
        if (GOTOSLEEP_CHANGED.equals(intent.getAction())){
            goToSleep(context);
        }
    }

    /**
     * this method is called when the widget is added to the home
     * screen, and so it contains the initial setup of the widget.
     */
    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Intent intent = new Intent(context, getClass());
        intent.setAction(GOTOSLEEP_CHANGED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);
        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_go_to_sleep_icon);
        views.setImageViewResource(R.id.widget_indic, R.drawable.widget_indic_on);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    /**
     * Turns off screen
     */
    public void goToSleep(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.goToSleep(SystemClock.uptimeMillis());
    }

}
