package org.jihedamine.ayahwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WidgetConfigActivity extends Activity {

    public static final int COLOR_PICKER_DENSITY = 12;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner textSizePicker;
    private int widgetBackgroundColor;
    private int widgetTextColor;
    private String selectedQuote;

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

        QuoteRepository quoteRepository = new QuoteRepository(this);

        SharedPreferences prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        float widgetTextSize = prefs.getFloat("widget_text_size_" + appWidgetId, 14f); // Default to 14
        selectedQuote = prefs.getString("widget_text_" + appWidgetId, quoteRepository.getRandomQuote()); // Default to a random quote
        widgetBackgroundColor = prefs.getInt("widget_bg_color_" + appWidgetId, getSystemThemeBackgroundColor());
        widgetTextColor = prefs.getInt("widget_text_color_" + appWidgetId, getSystemTextColor());

        textSizePicker = findViewById(R.id.textSizePicker);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getTextSizeValues());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textSizePicker.setAdapter(adapter);
        textSizePicker.setSelection(adapter.getPosition((int)widgetTextSize));

        setupColorPicker(R.id.textColorPickerButton, widgetTextColor, false);
        setupColorPicker(R.id.bgColorPickerButton, widgetBackgroundColor, true);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> savePreferences());
    }

    private void setupColorPicker(int imageViewId, int color, boolean isBackgroundColor) {
        ImageView imageView = findViewById(imageViewId);
        imageView.setColorFilter(color);
        imageView.setBackground(getOvalPaint(color));
        imageView.setOnClickListener(view -> renderColorPicker(imageView, isBackgroundColor));
    }

    @NonNull
    private ShapeDrawable getOvalPaint(int color) {
        var ovalDrawable = new OvalShape();
        var shapeDrawable = new ShapeDrawable(ovalDrawable);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private List<Integer> getTextSizeValues() {
        return IntStream.rangeClosed(12, 24).boxed().collect(Collectors.toList());
    }

    private void savePreferences() {
        // Save the selected preferences
        SharedPreferences prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("widget_bg_color_" + appWidgetId, widgetBackgroundColor);
        editor.putInt("widget_text_color_" + appWidgetId, widgetTextColor);
        editor.putFloat("widget_text_size_" + appWidgetId, ((Integer) textSizePicker.getSelectedItem()).floatValue());
        // put a random quote
        editor.putString("widget_text_" + appWidgetId, selectedQuote);
        editor.apply();

        // Update the widget with the new background and text color
        updateWidget(WidgetConfigActivity.this, appWidgetId);

        // Finish the activity and return RESULT_OK
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private void renderColorPicker(ImageView imageView, boolean isBackgroundColor) {
        ColorPickerDialogBuilder
                .with(WidgetConfigActivity.this)
                .setTitle("Choose color")
                .initialColor(isBackgroundColor ? widgetBackgroundColor : widgetTextColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(COLOR_PICKER_DENSITY)
                .setPositiveButton("ok", (dialog, color, allColors) -> {
                    if (isBackgroundColor) {
                        widgetBackgroundColor = color;
                    } else {
                        widgetTextColor = color;
                    }
                    imageView.setColorFilter(color);
                    imageView.setBackground(getOvalPaint(color));
                })
                .setNegativeButton("cancel", (dialog, which) -> {
                })
                .build()
                .show();
    }

    private int getSystemColor(int attribute) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attribute, typedValue, true);
        return typedValue.data;
    }

    private int getSystemThemeBackgroundColor() {
        return getSystemColor(android.R.attr.colorBackground);
    }

    private int getSystemTextColor() {
//        return getResources().getColor(android.R.color.black, null);
        return getSystemColor(android.R.attr.colorForeground);
    }

    private void updateWidget(Context context, int appWidgetId) {
        QuoteRepository quoteRepository = new QuoteRepository(context);

        // Get the selected background and text color preference
        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        int bgColor = prefs.getInt("widget_bg_color_" + appWidgetId, getSystemThemeBackgroundColor()); // Default to system theme colors
        int textColor = prefs.getInt("widget_text_color_" + appWidgetId, getSystemTextColor()); // Default to system theme colors
        float textSize = prefs.getFloat("widget_text_size_" + appWidgetId, 14f); // Default to 14
        String text = prefs.getString("widget_text_" + appWidgetId, quoteRepository.getRandomQuote()); // Default to a random quote

        // Update the widget's RemoteViews with the new background and text color
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ayah_widget);
        views.setInt(R.id.appwidget_layout, "setBackgroundColor", bgColor);
        views.setInt(R.id.appwidget_text, "setTextColor", textColor);
        views.setFloat(R.id.appwidget_text, "setTextSize", textSize);
        views.setTextViewText(R.id.appwidget_text, text);

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}