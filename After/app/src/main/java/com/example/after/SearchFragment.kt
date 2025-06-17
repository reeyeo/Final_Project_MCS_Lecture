package com.example.after

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchBar: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var emptySearchText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchBar = view.findViewById(R.id.searchBar)
        recyclerView = view.findViewById(R.id.searchResultsRecyclerView)
        adapter = SongAdapter(emptyList()) { song ->
            // Play the selected song and show the playbar
            val activity = requireActivity()
            val musicBar = activity.findViewById<View>(R.id.music_bar_fragment)
            musicBar?.visibility = View.VISIBLE
            MusicBarFragment.playSongList(listOf(song))
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        emptySearchText = view.findViewById(R.id.emptySearchText)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchSongs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            adapter.updateData(results ?: emptyList())
            emptySearchText.visibility = if (results.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
        return view
    }
} 