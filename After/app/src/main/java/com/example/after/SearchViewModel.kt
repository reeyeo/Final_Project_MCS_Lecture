package com.example.after

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MusicRepository
    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> = _searchResults

    private val allSongsLiveData = MusicDatabase.getDatabase(application).musicDao().getAllSongs().asLiveData()

    init {
        val dao = MusicDatabase.getDatabase(application).musicDao()
        repository = MusicRepository(dao)
    }

    fun searchSongs(query: String) {
        allSongsLiveData.observeForever { songs ->
            _searchResults.value = songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true)
            }
        }
    }
} 