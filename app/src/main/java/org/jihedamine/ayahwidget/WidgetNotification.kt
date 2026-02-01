package org.jihedamine.ayahwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import org.jihedamine.ayahwidget.ConfigDefaults.AYAH_REFRESH_INTERVAL_MINS
import java.util.Calendar

object WidgetNotification {
    const val WIDGET_REQUEST_CODE = 191001
    private const val MINUTES_TO_MILLIS = 60000

    private fun getActiveWidgetIds(context: Context): IntArray {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return appWidgetManager.getAppWidgetIds(ComponentName(context, AyahWidgetProvider::class.java))
    }

    fun scheduleWidgetUpdate(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val updateInterval = prefs.getInt("widget_refresh_interval_$appWidgetId", AYAH_REFRESH_INTERVAL_MINS)

        val activeWidgetIds = getActiveWidgetIds(context)
        if (activeWidgetIds.isNotEmpty()) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = getWidgetAlarmIntent(context)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
            }

            am.setInexactRepeating(AlarmManager.RTC, calendar.timeInMillis, updateInterval.toLong() * MINUTES_TO_MILLIS, pi)
        }
    }

    private fun getWidgetAlarmIntent(context: Context): PendingIntent {
        val intent = Intent(context, AyahWidgetProvider::class.java).apply {
            action = AyahWidgetProvider.ACTION_AUTO_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getActiveWidgetIds(context))
        }
        return PendingIntent.getBroadcast(context, WIDGET_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    fun clearWidgetUpdate(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(getWidgetAlarmIntent(context))
    }
}