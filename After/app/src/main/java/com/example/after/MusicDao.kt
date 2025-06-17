package com.example.after

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Song operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    // Playlist operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    // CrossRef operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Delete
    suspend fun deletePlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    // Get songs in a playlist
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs>
}

// Data class for relation
data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
) 