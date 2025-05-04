package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.ConfigDefaults.AYAH_REFRESH_INTERVAL_MINS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class WidgetNotification {
    public static final int WIDGET_REQUEST_CODE = 191001;
    public static final long MINUTES_TO_MILLIS = 1000 * 60;

    private static int[] getActiveWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager.getAppWidgetIds(new ComponentName(context, AyahWidgetProvider.class));
    }

    public static void scheduleWidgetUpdate(Context context, int appWidgetId) {
        var prefs =  context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        int updateInterval = prefs.getInt("widget_refresh_interval_" + appWidgetId, AYAH_REFRESH_INTERVAL_MINS);

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
