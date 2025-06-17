package com.example.after

import kotlinx.coroutines.flow.Flow

class MusicRepository(private val dao: MusicDao) {
    // Songs
    fun getAllSongs(): Flow<List<Song>> = dao.getAllSongs()
    suspend fun insertSong(song: Song) = dao.insertSong(song)
    suspend fun deleteSong(song: Song) = dao.deleteSong(song)

    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>> = dao.getAllPlaylists()
    suspend fun insertPlaylist(playlist: Playlist) = dao.insertPlaylist(playlist)
    suspend fun deletePlaylist(playlist: Playlist) = dao.deletePlaylist(playlist)

    // Playlist-Song CrossRef
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) =
        dao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, songId))
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        dao.deletePlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, songId))

    // Get songs in a playlist
    fun getPlaylistWithSongs(playlistId: Long) = dao.getPlaylistWithSongs(playlistId)
} 