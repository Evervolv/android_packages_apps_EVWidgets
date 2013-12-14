package com.evervolv.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;

public class GlobalActionsWidgetProvider extends AppWidgetProvider {
    // TAG
    public static final String TAG = "Evervolv_GlobalActions";
    private boolean DBG = false;
    // Intent Actions
    public static String GLOBALACTIONS_CHANGED = "com.evervolv.widgets.GLOBALACTIONS_CLICKED";

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
        if (GLOBALACTIONS_CHANGED.equals(intent.getAction())){
            showGlobalActions();
        }
    }

    /**
     * this method is called when the widget is added to the home
     * screen, and so it contains the initial setup of the widget.
     */
    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Intent intent = new Intent(context, getClass());
        intent.setAction(GLOBALACTIONS_CHANGED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.power_widget);
        views.setOnClickPendingIntent(R.id.widget_mask,pendingIntent);
        views.setImageViewResource(R.id.widget_icon, R.drawable.widget_power_icon);
        views.setImageViewResource(R.id.widget_indic, R.drawable.widget_indic_on);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    /**
     * Fake power key press to bring up global actions dialog.
     */
    public void showGlobalActions() {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER);
        InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }
}
