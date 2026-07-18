package org.jihedamine.ayahwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.ContextThemeWrapper
import android.widget.RemoteViews
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import org.jihedamine.ayahwidget.ConfigDefaults.AYAH_NAME_TEXT_RATIO
import org.jihedamine.ayahwidget.ConfigDefaults.TEXT_SIZE_DEFAULT
import org.json.JSONException
import org.json.JSONObject

object AyahWidgetService {

    private const val TRANSPARENT_ALPHA = 0x85

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

        // Resolve dynamic Material 3 colors from the widget's own theme so the palette
        // is consistent whether the system has dynamic color enabled or falls back to
        // the app's Material theme. Outer background derives from the surface tone,
        // the inner container uses the primary container color, and text uses the
        // on-primary container color.
        val widgetContext = ContextThemeWrapper(context, getDynamicColorThemeResId(context))
        val bgColor = getColorFromAttr(widgetContext, com.google.android.material.R.attr.colorSurfaceContainerHigh, android.R.color.white)
        val innerBgColor = getColorFromAttr(widgetContext, com.google.android.material.R.attr.colorPrimaryContainer, android.R.color.white)
        // Honor user alpha preference. The background is always derived from the dynamic
        // color: transparent mode applies the same dynamic tone with reduced opacity so the
        // home screen shows through while keeping the dynamic palette.
        val alpha = prefs.getInt("widget_alpha_$appWidgetId", R.id.radioOpaque)
        if (alpha == R.id.radioTransparent) {
            val outerBackground = applyAlpha(bgColor, TRANSPARENT_ALPHA)
            views.setInt(R.id.appwidget_layout, "setBackgroundColor", outerBackground)
        } else {
            views.setInt(R.id.appwidget_layout, "setBackgroundResource", R.drawable.outer_container_background)
        }

        if (alpha == R.id.radioTransparent) {
            val innerBackground = applyAlpha(innerBgColor, TRANSPARENT_ALPHA)
            views.setInt(R.id.appwidget_inner_container, "setBackgroundColor", innerBackground)
        } else {
            views.setInt(R.id.appwidget_inner_container, "setBackgroundResource", R.drawable.inner_container_background)
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getDynamicColorThemeResId(context: Context): Int {
        return context.resources.getIdentifier(
            "Theme.AyahWidget.Widget",
            "style",
            context.packageName
        ).takeIf { it != 0 } ?: android.R.style.Theme_DeviceDefault_DayNight
    }

    @ColorInt
    private fun applyAlpha(@ColorInt color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun getColorFromAttr(context: Context, @AttrRes attr: Int, fallback: Int): Int {
        val tv = TypedValue()
        return if (context.theme.resolveAttribute(attr, tv, true)) {
            if (tv.resourceId != 0) ContextCompat.getColor(context, tv.resourceId) else tv.data
        } else {
            fallback
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