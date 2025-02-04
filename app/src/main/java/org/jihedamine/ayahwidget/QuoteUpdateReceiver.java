package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.AyahWidgetProvider.getSystemTextColor;
import static org.jihedamine.ayahwidget.AyahWidgetProvider.getSystemThemeBackgroundColor;
import static org.jihedamine.ayahwidget.WidgetConfigActivity.QUOTES_FILE;
import static org.jihedamine.ayahwidget.WidgetConfigActivity.TEXT_SIZE_DEFAULT;
import static org.jihedamine.ayahwidget.WidgetConfigActivity.getAyahNameTextSize;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuoteUpdateReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null) {
                for (int appWidgetId : appWidgetIds) {
                    updateWidget(context, appWidgetId);
                }
            }
        }
    }

    public void updateWidget(Context context, int appWidgetId) {
        final List<String> quotesList = loadQuotes(context);
        QuoteRepository quoteRepository = new QuoteRepository(context);
        String[] quote = quoteRepository.getRandomQuote(quotesList).split("-");
        // Get the selected background and text color preference
        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        int bgColor = prefs.getInt("widget_bg_color_" + appWidgetId, getSystemThemeBackgroundColor(context)); // Default to system theme colors
        int textColor = prefs.getInt("widget_text_color_" + appWidgetId, getSystemTextColor(context)); // Default to system theme colors
        float textSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT);
        String ayahContent = prefs.getString("widget_ayah_content" + appWidgetId, quote[0].trim()); // Default to a random quote
        String ayahName = prefs.getString("widget_ayah_name" + appWidgetId, quote[1].trim()); // Default to a random quote

        // Update the widget's RemoteViews with the new background and text color
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ayah_widget);
//        views.setInt(R.id.appwidget_layout, "setBackgroundColor", getSystemThemeBackgroundColor(context));
//        views.setInt(R.id.appwidget_ayah_content, "setTextColor", getSystemTextColor(context));
//        views.setInt(R.id.appwidget_ayah_name, "setTextColor", textColor);
        views.setFloat(R.id.appwidget_ayah_content, "setTextSize", textSize);
        views.setFloat(R.id.appwidget_ayah_name, "setTextSize", getAyahNameTextSize(textSize));
        views.setTextViewText(R.id.appwidget_ayah_content, ayahContent);
        views.setTextViewText(R.id.appwidget_ayah_name, ayahName);

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public List<String> loadQuotes(Context context) {
        List<String> quotesList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(context.getFilesDir() + "/" + QUOTES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                quotesList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quotesList;
    }

}
