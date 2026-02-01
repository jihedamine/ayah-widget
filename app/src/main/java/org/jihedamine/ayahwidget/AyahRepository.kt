package org.jihedamine.ayahwidget

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AyahRepository(private val context: Context) {

    fun getAyahs(): List<JSONObject> {
        val ayahs = mutableListOf<JSONObject>()
        val assetManager = context.assets
        try {
            val inputStream = assetManager.open(AYAHS_ASSET_NAME)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                jsonBuilder.append(line)
            }
            val jsonArray = JSONArray(jsonBuilder.toString())
            for (i in 0 until jsonArray.length()) {
                ayahs.add(jsonArray.getJSONObject(i))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred while processing ayahs", e)
        } catch (e: JSONException) {
            Log.e(TAG, "Error occurred while processing ayahs", e)
        }
        return ayahs
    }

    fun getRandomAyah(): JSONObject {
        val ayahs = getAyahs()
        val index = (Math.random() * ayahs.size).toInt()
        return ayahs[index]
    }

    companion object {
        private const val TAG = "AyahRepository"
        const val AYAHS_ASSET_NAME = "ayahs.json"
    }
}