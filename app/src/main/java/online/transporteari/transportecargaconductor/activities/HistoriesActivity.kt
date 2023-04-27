package online.transporteari.transportecargaconductor.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import online.transporteari.transportecargaconductor.R
import online.transporteari.transportecargaconductor.adapters.HistoriesAdapter
import online.transporteari.transportecargaconductor.databinding.ActivityHistoriesBinding
import online.transporteari.transportecargaconductor.models.History
import online.transporteari.transportecargaconductor.providers.HistoryProvider

class HistoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoriesBinding
    private var historyProvider = HistoryProvider()
    private var histories: ArrayList<History> = ArrayList()
    private lateinit var adapter: HistoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistories.layoutManager = linearLayoutManager

        getHistories()
    }

    private fun getHistories() {
        histories.clear()
        historyProvider.getHistories().get().addOnSuccessListener {query ->
            if(query != null) {
                if(query.documents.size > 0) {
                    val documents = query.documents
                    for(document in documents) {
                        val history = document.toObject(History::class.java)
                        histories.add(history!!)
                    }

                    adapter = HistoriesAdapter(this, histories)
                    binding.recyclerViewHistories.adapter = adapter
                }
            }
        }
    }
}