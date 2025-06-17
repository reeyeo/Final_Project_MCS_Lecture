package com.example.after

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    val allPlaylists = MusicDatabase.getDatabase(application).musicDao().getAllPlaylists().asLiveData()

    fun deletePlaylist(playlist: Playlist) = viewModelScope.launch {
        MusicDatabase.getDatabase(getApplication()).musicDao().deletePlaylist(playlist)
    }
} 