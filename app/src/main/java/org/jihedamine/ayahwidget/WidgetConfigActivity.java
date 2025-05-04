package org.jihedamine.ayahwidget;


import static org.jihedamine.ayahwidget.ConfigDefaults.AYAH_REFRESH_INTERVAL_MINS;
import static org.jihedamine.ayahwidget.ConfigDefaults.BACKGROUND_ALPHA;
import static org.jihedamine.ayahwidget.ConfigDefaults.TEXT_SIZE_DEFAULT;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WidgetConfigActivity extends Activity {
   public static final String WIDGET_PREFS = "WidgetPrefs";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Slider textSizeSlider;
    private TextView textSizeValue;
    private String ayahContent;
    private Spinner intervalPicker;
    private RadioGroup alphaRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        // Get the App Widget ID from the Intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }


        SharedPreferences prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        float widgetTextSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT);

        ayahContent = prefs.getString("widget_ayah_content_" + appWidgetId, null);
        if (ayahContent == null) {
            AyahRepository ayahRepository = new AyahRepository(this);
            JSONObject ayah = ayahRepository.getRandomAyah();
            ayahContent = ayah.toString();
        }

        textSizeSlider = findViewById(R.id.textSizeSlider);
        textSizeValue = findViewById(R.id.textSizeValue);
        textSizeSlider.setValue(widgetTextSize);
        textSizeValue.setText(String.valueOf((int)widgetTextSize));

        textSizeSlider.addOnChangeListener((slider, value, fromUser) ->
                textSizeValue.setText(String.valueOf((int)value)));

        intervalPicker = findViewById(R.id.intervalPicker);
        ArrayAdapter<Integer> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getIntervalValues());
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalPicker.setAdapter(intervalAdapter);
        int savedInterval = prefs.getInt("widget_refresh_interval_" + appWidgetId, AYAH_REFRESH_INTERVAL_MINS);
        intervalPicker.setSelection(intervalAdapter.getPosition(savedInterval));

        alphaRadioGroup = findViewById(R.id.alphaRadioGroup);
        int savedAlpha = prefs.getInt("widget_alpha_" + appWidgetId, BACKGROUND_ALPHA);
        alphaRadioGroup.check(savedAlpha);

        android.widget.Button changeAyahButton = findViewById(R.id.button_change_ayah);
        changeAyahButton.setOnClickListener(v -> {
            AyahWidgetService.updateWidgetAyah(WidgetConfigActivity.this, appWidgetId);
            Toast.makeText(WidgetConfigActivity.this, "Widget updated with a new quote", Toast.LENGTH_SHORT).show();
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> {
            savePreferences();
            WidgetNotification.scheduleWidgetUpdate(this, appWidgetId);
            AyahWidgetService.updateWidget(WidgetConfigActivity.this, appWidgetId);
        } );
    }

    private void savePreferences() {
        // Save the selected preferences
        SharedPreferences prefs = getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("widget_text_size_" + appWidgetId, textSizeSlider.getValue());
        editor.putInt("widget_refresh_interval_" + appWidgetId, (Integer) intervalPicker.getSelectedItem());

        // Save alpha based on selected radio button
        int selectedId = alphaRadioGroup.getCheckedRadioButtonId();
        editor.putInt("widget_alpha_" + appWidgetId, selectedId);
        editor.putString("widget_ayah_content_" + appWidgetId, ayahContent);
        editor.apply();

        // Finish the activity and return RESULT_OK
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private List<Integer> getIntervalValues() {
        return IntStream.of(  1, 10, 30, 60, 120).boxed().collect(Collectors.toList());
    }
}
