package org.jihedamine.ayahwidget;

import static org.jihedamine.ayahwidget.AyahWidgetProvider.getSystemTextColor;
import static org.jihedamine.ayahwidget.AyahWidgetProvider.getSystemThemeBackgroundColor;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WidgetConfigActivity extends Activity {
    public static final int COLOR_PICKER_DENSITY = 12;
    public static final float TEXT_SIZE_DEFAULT = 12f;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner textSizePicker;
    private int widgetBackgroundColor;
    private int widgetTextColor;
    private String ayahContent;
    private String ayahName;

    public static final String QUOTES_FILE = "quotes.txt";
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private EditText editText;
    private QuoteRepository quoteRepository;

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

        listView = findViewById(R.id.quotes_list_view);
        editText = findViewById(R.id.edit_text_quote);

        quoteRepository = new QuoteRepository(this);
        if (loadQuotes().isEmpty()) {
            List<String> assetQuotes = quoteRepository.getQuotes();
            saveQuotes(assetQuotes);
        }
        final List<String> quotesList = loadQuotes();
        String[] quote = quoteRepository.getRandomQuote(quotesList).split("-");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quotesList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            editText.setText(quotesList.get(position));
            findViewById(R.id.button_edit).setTag(position);
        });

        findViewById(R.id.button_add).setOnClickListener(v -> addQuote(quotesList));
        findViewById(R.id.button_edit).setOnClickListener(v -> editQuote(quotesList));
        findViewById(R.id.button_delete).setOnClickListener(v -> deleteQuote(quotesList));


        SharedPreferences prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        float widgetTextSize = prefs.getFloat("widget_text_size_" + appWidgetId, TEXT_SIZE_DEFAULT);
        ayahContent = prefs.getString("widget_ayah_content_" + appWidgetId, quote[0]); // Default to a random quote
        ayahName = prefs.getString("widget_ayah_name_" + appWidgetId, quote[1]); // Default to a random quote
        widgetBackgroundColor = prefs.getInt("widget_bg_color_" + appWidgetId, getSystemThemeBackgroundColor(getApplicationContext()));
        widgetTextColor = prefs.getInt("widget_text_color_" + appWidgetId, getSystemTextColor(getApplicationContext()));

        textSizePicker = findViewById(R.id.textSizePicker);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getTextSizeValues());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textSizePicker.setAdapter(adapter);
        textSizePicker.setSelection(adapter.getPosition((int)widgetTextSize));

        setupColorPicker(R.id.textColorPickerButton, widgetTextColor, false);
        setupColorPicker(R.id.bgColorPickerButton, widgetBackgroundColor, true);

        FloatingActionButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> savePreferences());

        setQuoteUpdateAlarm();
        android.widget.Button updateQuoteButton = findViewById(R.id.button_update_quote);
        updateQuoteButton.setOnClickListener(v -> updateWidgetWithNewQuote());
    }

    private void updateWidgetWithNewQuote() {
        updateWidget(this, appWidgetId);
        Toast.makeText(this, "Widget updated with a new quote", Toast.LENGTH_SHORT).show();
    }

    public List<String> loadQuotes() {
        List<String> quotesList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(getFilesDir() + "/" + QUOTES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                quotesList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quotesList;
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
        editor.putString("widget_ayah_content_" + appWidgetId, ayahContent);
        editor.putString("widget_ayah_name_" + appWidgetId, ayahName);
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

    public void updateWidget(Context context, int appWidgetId) {
        final List<String> quotesList = loadQuotes();
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

    public static float getAyahNameTextSize(float textSize) {
        return textSize - 4;
    }

    private void saveQuotes(List<String> quotesList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilesDir() + "/" + QUOTES_FILE))) {
            for (String quote : quotesList) {
                writer.write(quote);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addQuote(List<String> quotesList) {
        String quote = editText.getText().toString();
        if (!quote.isEmpty()) {
            quotesList.add(quote);
            adapter.notifyDataSetChanged();
            saveQuotes(quotesList);
            editText.setText("");
        } else {
            Toast.makeText(this, "Quote cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void editQuote(List<String> quotesList) {
        int position = (int) findViewById(R.id.button_edit).getTag();
        String quote = editText.getText().toString();
        if (!quote.isEmpty()) {
            quotesList.set(position, quote);
            adapter.notifyDataSetChanged();
            saveQuotes(quotesList);
            editText.setText("");
        } else {
            Toast.makeText(this, "Quote cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteQuote(List<String> quotesList) {
        int position = (int) findViewById(R.id.button_edit).getTag();
        quotesList.remove(position);
        adapter.notifyDataSetChanged();
        saveQuotes(quotesList);
        editText.setText("");
    }

    private void setQuoteUpdateAlarm() {
        Intent intent = new Intent(this, QuoteUpdateReceiver.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] appWidgetIds = {appWidgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR * 4, pendingIntent);
        }
    }
}