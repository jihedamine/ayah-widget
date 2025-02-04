package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.WidgetConfigActivity.TEXT_SIZE_DEFAULT;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class AyahWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        int bgColor = prefs.getInt("widget_bg_color_" + appWidgetId, getSystemThemeBackgroundColor(context)); // Default to white
        int textColor = prefs.getInt("widget_text_color_" + appWidgetId, getSystemTextColor(context)); // Default to black
        float textSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT); // Default to 14
        String ayahContent = prefs.getString("widget_ayah_content_" + appWidgetId, ""); // Default to empty
        String ayahName = prefs.getString("widget_ayah_name_" + appWidgetId, ""); // Default to empty

        // Update the widget's RemoteViews with the new background and text color
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ayah_widget);
        views.setFloat(R.id.appwidget_ayah_content, "setTextSize", textSize);
        views.setFloat(R.id.appwidget_ayah_name, "setTextSize", WidgetConfigActivity.getAyahNameTextSize(textSize));

//        views.setInt(R.id.appwidget_layout, "setBackgroundColor", bgColor);
//        views.setInt(R.id.appwidget_ayah_content, "setTextColor", textColor);
//        views.setInt(R.id.appwidget_ayah_name, "setTextColor", textColor);
        views.setTextViewText(R.id.appwidget_ayah_content, ayahContent);
        views.setTextViewText(R.id.appwidget_ayah_name, ayahName);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static int getSystemTextColor(Context context) {
        return getSystemColor(context, android.R.attr.textColorSecondary);
    }

    public static int getSystemThemeBackgroundColor(Context context) {
        return getSystemColor(context, android.R.attr.colorBackground);
    }

    public static int getSystemColor(Context context, int attribute) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attribute, typedValue, true);
        return typedValue.data;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}