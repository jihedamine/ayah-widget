package org.jihedamine.ayahwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class AyahWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        int bgColor = prefs.getInt("widget_bg_color_" + appWidgetId, 0xFFFFFFFF); // Default to white
        int textColor = prefs.getInt("widget_text_color_" + appWidgetId, 0xFF000000); // Default to black
        float textSize = prefs.getFloat("widget_text_size_" + appWidgetId, 14f); // Default to 14
        String text = prefs.getString("widget_text_" + appWidgetId, ""); // Default to empty

        // Update the widget's RemoteViews with the new background and text color
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ayah_widget);
        views.setInt(R.id.appwidget_layout, "setBackgroundColor", bgColor);
        views.setInt(R.id.appwidget_text, "setTextColor", textColor);
        views.setFloat(R.id.appwidget_text, "setTextSize", textSize);
        views.setTextViewText(R.id.appwidget_text, text);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}