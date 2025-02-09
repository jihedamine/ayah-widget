package org.jihedamine.ayahwidget;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AyahRepository {

    private final Context context;

    public AyahRepository(Context context) {
        this.context = context;
    }

    public List<JSONObject> getAyahs() {
        List<JSONObject> ayahs = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("ayahs.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                ayahs.add(jsonArray.getJSONObject(i));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return ayahs;
    }

    public JSONObject getRandomAyah() {
        var ayahs = getAyahs();
        int index = (int) (Math.random() * ayahs.size());
        return ayahs.get(index);
    }
}