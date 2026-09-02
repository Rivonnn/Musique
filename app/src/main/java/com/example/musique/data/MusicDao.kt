package com.example.musique.data

import androidx.room.*
import com.example.musique.model.Playlist
import com.example.musique.model.PlaylistWithStats
import com.example.musique.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Playlist operations
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    // Song operations
    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Update
    suspend fun updateSongs(songs: List<Song>)

    @Query("SELECT COUNT(*) FROM songs WHERE playlistId = :playlistId")
    fun getSongCount(playlistId: Long): Flow<Int>

    @Query("SELECT SUM(duration) FROM songs WHERE playlistId = :playlistId")
    fun getTotalDuration(playlistId: Long): Flow<Long>

    @Query("""
    SELECT playlists.*, 
           COUNT(songs.id) AS songCount, 
           COALESCE(SUM(songs.duration), 0) AS totalDuration
    FROM playlists
    LEFT JOIN songs ON songs.playlistId = playlists.id
    GROUP BY playlists.id
""")
    fun getAllPlaylistsWithStats(): Flow<List<PlaylistWithStats>>
}