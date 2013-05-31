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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.evervolv.widgets.R;

public class DialpadWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "PhoneWidgetProvider";

    public static String DIALPAD_ONE     = "com.evervolv.widgets.DIALPAD_ONE";
    public static String DIALPAD_TWO     = "com.evervolv.widgets.DIALPAD_TWO";
    public static String DIALPAD_THREE   = "com.evervolv.widgets.DIALPAD_THREE";
    public static String DIALPAD_FOUR    = "com.evervolv.widgets.DIALPAD_FOUR";
    public static String DIALPAD_FIVE    = "com.evervolv.widgets.DIALPAD_FIVE";
    public static String DIALPAD_SIX     = "com.evervolv.widgets.DIALPAD_SIX";
    public static String DIALPAD_SEVEN   = "com.evervolv.widgets.DIALPAD_SEVEN";
    public static String DIALPAD_EIGHT   = "com.evervolv.widgets.DIALPAD_EIGHT";
    public static String DIALPAD_NINE    = "com.evervolv.widgets.DIALPAD_NINE";
    public static String DIALPAD_ZERO    = "com.evervolv.widgets.DIALPAD_ZERO";
    public static String DIALPAD_STAR    = "com.evervolv.widgets.DIALPAD_STAR";
    public static String DIALPAD_HASH    = "com.evervolv.widgets.DIALPAD_HASH";
    public static String DIALPAD_DELETE  = "com.evervolv.widgets.DIALPAD_DELETE";
    public static String DIALPAD_DIAL    = "com.evervolv.widgets.DIALPAD_DIAL";

    public static String mCurrentNumber = "";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate!!!");
        updateWidgets(context, false, null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (DIALPAD_ONE.equals(intent.getAction())){
            updateWidgets(context, true, "1");
        } else if (DIALPAD_TWO.equals(intent.getAction())){
            updateWidgets(context, true, "2");
        } else if (DIALPAD_THREE.equals(intent.getAction())){
            updateWidgets(context, true, "3");
        } else if (DIALPAD_FOUR.equals(intent.getAction())){
            updateWidgets(context, true, "4");
        } else if (DIALPAD_FIVE.equals(intent.getAction())){
            updateWidgets(context, true, "5");
        } else if (DIALPAD_SIX.equals(intent.getAction())){
            updateWidgets(context, true, "6");
        } else if (DIALPAD_SEVEN.equals(intent.getAction())){
            updateWidgets(context, true, "7");
        } else if (DIALPAD_EIGHT.equals(intent.getAction())){
            updateWidgets(context, true, "8");
        } else if (DIALPAD_NINE.equals(intent.getAction())){
            updateWidgets(context, true, "9");
        } else if (DIALPAD_ZERO.equals(intent.getAction())){
            updateWidgets(context, true, "0");
        } else if (DIALPAD_STAR.equals(intent.getAction())){
            updateWidgets(context, true, "*");
        } else if (DIALPAD_HASH.equals(intent.getAction())){
            updateWidgets(context, true, "#");
        } else if (DIALPAD_DELETE.equals(intent.getAction())){
            updateWidgets(context, true, "del");
        } else if (DIALPAD_DIAL.equals(intent.getAction())){
            updateWidgets(context, true, "dial");
        } else if (AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED
                .equals(intent.getAction())) {
            Log.d(TAG, "ACTION_APPWIDGET_OPTIONS_CHANGED");
        }
    }

    private void updateWidgets(Context context, boolean click, String character) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.phone_widget);

        Intent intentOne = new Intent(context, getClass());
        intentOne.setAction(DIALPAD_ONE);
        PendingIntent onePendingIntent = PendingIntent.getBroadcast(context,0,intentOne,0);
        views.setOnClickPendingIntent(R.id.one, onePendingIntent);

        Intent intentTwo = new Intent(context, getClass());
        intentTwo.setAction(DIALPAD_TWO);
        PendingIntent twoPendingIntent = PendingIntent.getBroadcast(context,0,intentTwo,0);
        views.setOnClickPendingIntent(R.id.two, twoPendingIntent);

        Intent intentThree = new Intent(context, getClass());
        intentThree.setAction(DIALPAD_THREE);
        PendingIntent threePendingIntent = PendingIntent.getBroadcast(context,0,intentThree,0);
        views.setOnClickPendingIntent(R.id.three, threePendingIntent);

        Intent intentFour = new Intent(context, getClass());
        intentFour.setAction(DIALPAD_FOUR);
        PendingIntent fourPendingIntent = PendingIntent.getBroadcast(context,0,intentFour,0);
        views.setOnClickPendingIntent(R.id.four, fourPendingIntent);

        Intent intentFive = new Intent(context, getClass());
        intentFive.setAction(DIALPAD_FIVE);
        PendingIntent fivePendingIntent = PendingIntent.getBroadcast(context,0,intentFive,0);
        views.setOnClickPendingIntent(R.id.five, fivePendingIntent);

        Intent intentSix = new Intent(context, getClass());
        intentSix.setAction(DIALPAD_SIX);
        PendingIntent sixPendingIntent = PendingIntent.getBroadcast(context,0,intentSix,0);
        views.setOnClickPendingIntent(R.id.six, sixPendingIntent);

        Intent intentSeven = new Intent(context, getClass());
        intentSeven.setAction(DIALPAD_SEVEN);
        PendingIntent sevenPendingIntent = PendingIntent.getBroadcast(context,0,intentSeven,0);
        views.setOnClickPendingIntent(R.id.seven, sevenPendingIntent);

        Intent intentEight = new Intent(context, getClass());
        intentEight.setAction(DIALPAD_EIGHT);
        PendingIntent eightPendingIntent = PendingIntent.getBroadcast(context,0,intentEight,0);
        views.setOnClickPendingIntent(R.id.eight, eightPendingIntent);

        Intent intentNine = new Intent(context, getClass());
        intentNine.setAction(DIALPAD_NINE);
        PendingIntent ninePendingIntent = PendingIntent.getBroadcast(context,0,intentNine,0);
        views.setOnClickPendingIntent(R.id.nine, ninePendingIntent);

        Intent intentZero = new Intent(context, getClass());
        intentZero.setAction(DIALPAD_ZERO);
        PendingIntent zeroPendingIntent = PendingIntent.getBroadcast(context,0,intentZero,0);
        views.setOnClickPendingIntent(R.id.zero, zeroPendingIntent);

        Intent intentStar = new Intent(context, getClass());
        intentStar.setAction(DIALPAD_STAR);
        PendingIntent starPendingIntent = PendingIntent.getBroadcast(context,0,intentStar,0);
        views.setOnClickPendingIntent(R.id.star, starPendingIntent);

        Intent intentHash = new Intent(context, getClass());
        intentHash.setAction(DIALPAD_HASH);
        PendingIntent hashPendingIntent = PendingIntent.getBroadcast(context,0,intentHash,0);
        views.setOnClickPendingIntent(R.id.hash, hashPendingIntent);

        Intent intentDelete = new Intent(context, getClass());
        intentDelete.setAction(DIALPAD_DELETE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context,0,intentDelete,0);
        views.setOnClickPendingIntent(R.id.delete, deletePendingIntent);

        Intent intentDial = new Intent(context, getClass());
        intentDial.setAction(DIALPAD_DIAL);
        PendingIntent dialPendingIntent = PendingIntent.getBroadcast(context,0,intentDial,0);
        views.setOnClickPendingIntent(R.id.dial, dialPendingIntent);

        if (click) {
            if (mCurrentNumber == "") {
                mCurrentNumber = character;
            } else {
                if (character.equals("del")) {
                    if (mCurrentNumber.length() > 0) {
                        mCurrentNumber = mCurrentNumber.substring(0, mCurrentNumber.length() - 1);
                        if (mCurrentNumber.substring(mCurrentNumber
                                .length()).equals("-")) {
                            mCurrentNumber = mCurrentNumber.substring(0,
                                    mCurrentNumber.length() - 1);
                        }
                    }
                } else if (character.equals("dial")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_CALL,
                                Uri.parse("tel:" + URLEncoder.encode(
                                        mCurrentNumber, "UTF-8")));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (mCurrentNumber.length() == 3 | mCurrentNumber.length() == 7) {
                        mCurrentNumber += "-" + character ;
                    } else {
                        mCurrentNumber += character;
                    }
                }
            }
        } else {
            mCurrentNumber = "";
        }

        views.setTextViewText(R.id.dialpad_text, mCurrentNumber);
        ComponentName cn = new ComponentName(context, getClass());
        AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
    }
}