package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.WidgetConfigActivity.AYAH_REFRESH_INTERVAL_MINS;
import static org.jihedamine.ayahwidget.WidgetConfigActivity.MINUTES_TO_MILLIS;
import static org.jihedamine.ayahwidget.WidgetConfigActivity.WIDGET_PREFS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class WidgetNotification {
    public static final int WIDGET_REQUEST_CODE = 191001;

    private static int[] getActiveWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager.getAppWidgetIds(new ComponentName(context, AyahWidgetProvider.class));
    }

    public static void scheduleWidgetUpdate(Context context) {
        var prefs = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE);
        int updateInterval = prefs.getAll()
                .keySet()
                .stream()
                .filter(key -> key.startsWith("widget_refresh_interval_"))
                .findFirst()
                .map(key -> prefs.getInt(key, AYAH_REFRESH_INTERVAL_MINS))
                .orElse(AYAH_REFRESH_INTERVAL_MINS);

        if (getActiveWidgetIds(context) != null && getActiveWidgetIds(context).length > 0) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = getWidgetAlarmIntent(context);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            am.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), updateInterval * MINUTES_TO_MILLIS, pi);
        }
    }

    private static PendingIntent getWidgetAlarmIntent(Context context) {
        Intent intent = new Intent(context, AyahWidgetProvider.class)
                .setAction(AyahWidgetProvider.ACTION_AUTO_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getActiveWidgetIds(context));
        return PendingIntent.getBroadcast(context, WIDGET_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    public static void clearWidgetUpdate(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getWidgetAlarmIntent(context));
    }
}
