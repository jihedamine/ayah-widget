package org.jihedamine.ayahwidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject

class AyahListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayah_list)

        val recyclerView = findViewById<RecyclerView>(R.id.ayahRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val ayahRepository = AyahRepository(this)
        val ayahs = ayahRepository.getAyahs()

        recyclerView.adapter = AyahAdapter(ayahs)

        findViewById<FloatingActionButton>(R.id.fab_back).setOnClickListener {
            finish()
        }
    }

    private class AyahAdapter(private val ayahs: List<JSONObject>) :
        RecyclerView.Adapter<AyahAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ayahTextView: TextView = view.findViewById(R.id.ayahTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ayah, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val ayahJson = ayahs[position]
            val ayahContent = ayahJson.toString()
            // Reducing size by 1/4 (70 * 0.75 = 52.5, rounded to 52)
            holder.ayahTextView.text = AyahWidgetService.getAyahSpannableString(ayahContent, 52)
        }

        override fun getItemCount() = ayahs.size
    }
}