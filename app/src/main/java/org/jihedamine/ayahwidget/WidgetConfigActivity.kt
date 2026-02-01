package org.jihedamine.ayahwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.slider.Slider
import org.jihedamine.ayahwidget.ConfigDefaults.AYAH_REFRESH_INTERVAL_MINS
import org.jihedamine.ayahwidget.ConfigDefaults.BACKGROUND_ALPHA
import org.jihedamine.ayahwidget.ConfigDefaults.TEXT_SIZE_DEFAULT
import androidx.core.content.edit

class WidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var textSizeSlider: Slider
    private lateinit var textSizeValue: TextView
    private lateinit var intervalPicker: Spinner
    private lateinit var alphaRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Set the result to CANCELED. It will be changed to OK if the user saves.
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        val prefs = getSharedPreferences(WIDGET_PREFS, MODE_PRIVATE)
        val widgetTextSize = prefs.getFloat("widget_text_size_$appWidgetId", TEXT_SIZE_DEFAULT)

        textSizeSlider = findViewById(R.id.textSizeSlider)
        textSizeValue = findViewById(R.id.textSizeValue)
        textSizeSlider.value = widgetTextSize
        textSizeValue.text = widgetTextSize.toInt().toString()

        textSizeSlider.addOnChangeListener { _, value, _ ->
            textSizeValue.text = value.toInt().toString()
        }

        intervalPicker = findViewById(R.id.intervalPicker)
        val intervalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getIntervalValues())
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        intervalPicker.adapter = intervalAdapter
        val savedInterval = prefs.getInt("widget_refresh_interval_$appWidgetId", AYAH_REFRESH_INTERVAL_MINS)
        intervalPicker.setSelection(intervalAdapter.getPosition(savedInterval))

        alphaRadioGroup = findViewById(R.id.alphaRadioGroup)
        val savedAlpha = prefs.getInt("widget_alpha_$appWidgetId", BACKGROUND_ALPHA)
        alphaRadioGroup.check(savedAlpha)

        findViewById<Button>(R.id.button_change_ayah).setOnClickListener {
            AyahWidgetService.updateWidgetAyah(this, appWidgetId)
            Toast.makeText(this, "Widget updated with a new quote", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.save_button).setOnClickListener {
            savePreferences()
            WidgetNotification.scheduleWidgetUpdate(this, appWidgetId)
            AyahWidgetService.updateWidget(this, appWidgetId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    private fun savePreferences() {
        val prefs = getSharedPreferences(WIDGET_PREFS, MODE_PRIVATE)
        prefs.edit {
            putFloat("widget_text_size_$appWidgetId", textSizeSlider.value)
            putInt("widget_refresh_interval_$appWidgetId", intervalPicker.selectedItem as Int)
            putInt("widget_alpha_$appWidgetId", alphaRadioGroup.checkedRadioButtonId)

            if (!prefs.contains("widget_ayah_content_$appWidgetId")) {
                val ayahRepository = AyahRepository(this@WidgetConfigActivity)
                val ayah = ayahRepository.getRandomAyah()
                putString("widget_ayah_content_$appWidgetId", ayah.toString())
            }
        }
    }

    private fun getIntervalValues(): List<Int> = listOf(1, 10, 30, 60, 120)

    companion object {
        const val WIDGET_PREFS = "WidgetPrefs"
    }
}