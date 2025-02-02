package org.jihedamine.ayahwidget;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class QuoteRepository {

    private final Context context;

    public QuoteRepository(Context context) {
        this.context = context;
    }

    public List<String> getQuotes() {
        List<String> quotes = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("quotes.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                quotes.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quotes;
    }

    public String getRandomQuote() {
        var quotes = getQuotes();
        var index = (int) (Math.random() * quotes.size());
        return quotes.get(index);
    }
}