package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.AyahWidgetProvider.getAyahSpannableString;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WidgetConfigActivity extends Activity {
    public static final float TEXT_SIZE_DEFAULT = 44f;
    public static final int AYAH_REFRESH_INTERVAL_MINS = 30;
//    public static final long MINUTES_TO_MILLIS = 1000 * 60;
    public static final long MINUTES_TO_MILLIS = 100;
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

        AyahRepository ayahRepository = new AyahRepository(this);
        JSONObject ayah = ayahRepository.getRandomAyah();

        SharedPreferences prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        float widgetTextSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT);
        ayahContent = prefs.getString("widget_ayah_content_" + appWidgetId, ayah.toString()); // Default to a random quote

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
        int savedAlpha = prefs.getInt("widget_alpha_" + appWidgetId, R.id.radioOpaque);
        alphaRadioGroup.check(savedAlpha);

        android.widget.Button changeAyahButton = findViewById(R.id.button_change_ayah);
        changeAyahButton.setOnClickListener(v -> updateWidgetWithNewAyah());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> savePreferences());
    }

    private void updateWidgetWithNewAyah() {
        updateWidget(this, appWidgetId);
        Toast.makeText(this, "Widget updated with a new quote", Toast.LENGTH_SHORT).show();
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

        // Update the widget with the new background and text color
        updateWidget(WidgetConfigActivity.this, appWidgetId);

        // Finish the activity and return RESULT_OK
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    public void updateWidget(Context context, int appWidgetId) {
        AyahRepository ayahRepository = new AyahRepository(context);
        JSONObject ayah = ayahRepository.getRandomAyah();

        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        float textSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT);
        String ayahContent = prefs.getString("widget_ayah_content_" + appWidgetId, ayah.toString());
        int alpha = prefs.getInt("widget_alpha_" + appWidgetId, R.id.radioOpaque);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ayah_widget);
        views.setCharSequence(R.id.appwidget_ayah_content, "setText", getAyahSpannableString(ayahContent, (int) textSize));

        // Set background based on alpha value
        int backgroundResId;
//        int selectedId = alphaRadioGroup.getCheckedRadioButtonId();
        if (alpha == R.id.radioTransparent) {
            backgroundResId = R.color.widget_background_transparent;
        } else if(alpha == R.id.radioSemiTransparent) {
            backgroundResId = R.color.widget_background_semi_transparent;
        } else {
            backgroundResId = R.color.widget_background_opaque;
        }

        views.setInt(R.id.appwidget_layout, "setBackgroundResource", backgroundResId);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private List<Integer> getIntervalValues() {
        return IntStream.of(5, 15, 30, 45, 60, 120, 240).boxed().collect(Collectors.toList());
    }
}
