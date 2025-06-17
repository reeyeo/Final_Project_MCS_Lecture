package com.example.after

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val repository: MusicRepository

    val allSongs = MusicDatabase.getDatabase(application).musicDao().getAllSongs().asLiveData()
    val allPlaylists = MusicDatabase.getDatabase(application).musicDao().getAllPlaylists().asLiveData()

    init {
        val dao = MusicDatabase.getDatabase(application).musicDao()
        repository = MusicRepository(dao)
    }

    fun addSong(song: Song) = viewModelScope.launch {
        repository.insertSong(song)
    }

    fun deleteSong(song: Song) = viewModelScope.launch {
        repository.deleteSong(song)
    }

    fun addPlaylist(playlist: Playlist) = viewModelScope.launch {
        repository.insertPlaylist(playlist)
    }

    fun deletePlaylist(playlist: Playlist) = viewModelScope.launch {
        repository.deletePlaylist(playlist)
    }
} 