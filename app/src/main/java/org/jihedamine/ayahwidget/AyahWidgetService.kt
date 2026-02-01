package org.jihedamine.ayahwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.widget.RemoteViews
import org.jihedamine.ayahwidget.ConfigDefaults.AYAH_NAME_TEXT_RATIO
import org.jihedamine.ayahwidget.ConfigDefaults.TEXT_SIZE_DEFAULT
import org.json.JSONException
import org.json.JSONObject

object AyahWidgetService {

    fun updateWidgetAyah(context: Context, appWidgetId: Int) {
        val ayahRepository = AyahRepository(context)
        val ayah = ayahRepository.getRandomAyah()
        val ayahContent = ayah.toString()
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("widget_ayah_content_$appWidgetId", ayahContent)
            apply()
        }

        updateWidget(context, appWidgetId)
    }

    fun updateWidget(context: Context, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.ayah_widget)
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)

        // Set ayah spannable string
        val textSize = prefs.getFloat("widget_text_size_$appWidgetId", TEXT_SIZE_DEFAULT)
        val ayahContent = prefs.getString("widget_ayah_content_$appWidgetId", null)
        views.setCharSequence(R.id.appwidget_ayah_content, "setText", getAyahSpannableString(ayahContent, textSize.toInt()))

        // Set background based on alpha value
        val alpha = prefs.getInt("widget_alpha_$appWidgetId", R.id.radioOpaque)
        val backgroundResId = getBackgroundResId(alpha)
        views.setInt(R.id.appwidget_layout, "setBackgroundResource", backgroundResId)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getBackgroundResId(alpha: Int): Int {
        return when (alpha) {
            R.id.radioTransparent -> R.color.widget_background_transparent
            else -> R.color.widget_background_opaque
        }
    }

    fun getAyahSpannableString(ayahContent: String?, textSize: Int): SpannableString {
        return try {
            val ayahJson = JSONObject(ayahContent!!)
            val ayahText = ayahJson.getString("ayahContent")
            val souraName = ayahJson.getString("ayahName")
            val ayatNumber = ayahJson.getString("ayahNumbers")

            val ayahAndName = "$ayahText\nسورة $souraName, الآية $ayatNumber"
            val ayahContentString = SpannableString(ayahAndName)
            ayahContentString.setSpan(AbsoluteSizeSpan(textSize), 0, ayahText.length, 0)
            ayahContentString.setSpan(AbsoluteSizeSpan((textSize * AYAH_NAME_TEXT_RATIO).toInt()), ayahText.length, ayahAndName.length, 0)
            ayahContentString
        } catch (_: JSONException) {
            SpannableString("")
        }
    }
}