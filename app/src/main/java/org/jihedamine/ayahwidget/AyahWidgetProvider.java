package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.WidgetConfigActivity.TEXT_SIZE_DEFAULT;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of App Widget functionality.
 */
public class AyahWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_AUTO_UPDATE = "org.jihedamine.ayahwidget.AUTO_UPDATE";
    public static final double AYAH_NAME_TEXT_RATIO = 0.65;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        float textSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT);
        float alpha = prefs.getFloat("widget_alpha_" + appWidgetId, WidgetConfigActivity.ALPHA_DEFAULT);

        AyahRepository ayahRepository = new AyahRepository(context);
        JSONObject ayahContent = ayahRepository.getRandomAyah();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ayah_widget);
        views.setCharSequence(R.id.appwidget_ayah_content, "setText", getAyahSpannableString(ayahContent.toString(), (int) textSize));
        views.setFloat(R.id.appwidget_layout, "setAlpha", alpha);

        appWidgetManager.updateAppWidget(appWidgetId, views);
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
            e.printStackTrace();
            return new SpannableString("");
        }
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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent!=null && intent.getAction()!=null && intent.getAction().equals(ACTION_AUTO_UPDATE)){
            onUpdate(context);
        }
    }

    private void onUpdate(Context context) {
        AppWidgetManager appWidgetManager =
                AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(),getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        WidgetNotification.scheduleWidgetUpdate(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        WidgetNotification.clearWidgetUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WidgetNotification.clearWidgetUpdate(context);
    }
}
