package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.ConfigDefaults.AYAH_NAME_TEXT_RATIO;
import static org.jihedamine.ayahwidget.ConfigDefaults.TEXT_SIZE_DEFAULT;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class AyahWidgetService {

    public static void updateWidgetAyah(Context context, int appWidgetId) {
        AyahRepository ayahRepository = new AyahRepository(context);
        JSONObject ayah = ayahRepository.getRandomAyah();
        var ayahContent = ayah.toString();
        var prefs =  context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        var editor = prefs.edit();

        editor.putString("widget_ayah_content_" + appWidgetId, ayahContent);
        editor.apply();

        updateWidget(context, appWidgetId);
    }

    public static void updateWidget(Context context, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ayah_widget);

        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);

        // Set ayah spannable string
        float textSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT);
        String ayahContent = prefs.getString("widget_ayah_content_" + appWidgetId, null);
        views.setCharSequence(R.id.appwidget_ayah_content, "setText", getAyahSpannableString(ayahContent, (int) textSize));

        // Set background based on alpha value
        int alpha = prefs.getInt("widget_alpha_" + appWidgetId, R.id.radioOpaque);
        int backgroundResId = getBackgroundResId(alpha);
        views.setInt(R.id.appwidget_layout, "setBackgroundResource", backgroundResId);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int getBackgroundResId(int alpha) {
        return alpha == R.id.radioTransparent ?
                R.color.widget_background_transparent
                : R.color.widget_background_opaque;
    }

    @NonNull
    public static SpannableString getAyahSpannableString(String ayahContent, int textSize) {
        try {
            JSONObject ayahJson = new JSONObject(ayahContent);
            String ayahText = ayahJson.getString("ayahContent");
            String souraName = ayahJson.getString("ayahName");
            String ayatNumber = ayahJson.getString("ayahNumbers");

            String ayahAndName = ayahText + "\n" + "سورة " + souraName + ", الآية " + ayatNumber;
            SpannableString ayahContentString = new SpannableString(ayahAndName);
            ayahContentString.setSpan(new AbsoluteSizeSpan(textSize), 0, ayahText.length(), 0);
            ayahContentString.setSpan(new AbsoluteSizeSpan((int) (textSize * AYAH_NAME_TEXT_RATIO)), ayahText.length(), ayahAndName.length(), 0);
            return ayahContentString;
        } catch (JSONException e) {
            return new SpannableString("");
        }
    }


}
