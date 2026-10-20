package com.example.musique.data

import androidx.room.*
import com.example.musique.model.Playlist
import com.example.musique.model.PlaylistWithStats
import com.example.musique.model.Song
import kotlinx.coroutines.flow.Flow
/**
 * The app's database operations. This is an interface: Room generates the
 * implementation at build time (MusicDao_Impl). @Insert/@Update/@Delete get their
 * SQL from the entity passed in; @Query uses the SQL written here, checked at build time.
 *
 * suspend functions are one-shot writes (call from a coroutine). Flow functions are
 * live reads: they emit now and again whenever the tables they read change.
 */
@Dao
interface MusicDao {

    // Playlists

    // Live list of all playlists. Row order isn't specified.
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    // REPLACE: if a row with the same id exists, it is deleted and re-inserted.
    // Safe now because ids are auto-generated and never collide.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    // Matches the row by primary key. Songs in it are removed too (CASCADE on Song).
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    // Overwrites the row with the same id using the passed values (name, imageUri).
    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    // Songs

    // Live list of one playlist's songs in manual order.
    // NOTE: songs tied on sortOrder (all start at 0) come back in arbitrary order;
    // TODO: add ", id ASC" as a tiebreaker.
    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY sortOrder ASC, id ASC")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>

    // Next free sortOrder in a playlist (0 if empty). Used to append new songs at the end.
    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM songs WHERE playlistId = :playlistId")
    suspend fun getNextSortOrder(playlistId: Long): Int

    // Same REPLACE behavior as insertPlaylist. The new song's sortOrder is whatever
    // the Song carries (default 0), nothing here assigns the next position.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    // Overwrites the song row with the same id (title, artist, imageUri, sortOrder...).
    @Update
    suspend fun updateSong(song: Song)

    // Batch update, used by swapSongs to rewrite sortOrder for a whole playlist.
    @Update
    suspend fun updateSongs(songs: List<Song>)

    // Playlist stats

    // Live song count for one playlist.
    // NOTE: replaced by getAllPlaylistsWithStats; remove once PlaylistGrid is switched.
    @Query("SELECT COUNT(*) FROM songs WHERE playlistId = :playlistId")
    fun getSongCount(playlistId: Long): Flow<Int>

    // Live total duration in ms for one playlist.
    // NOTE: SUM is NULL for an empty playlist; also replaced by the stats query below.
    @Query("SELECT SUM(duration) FROM songs WHERE playlistId = :playlistId")
    fun getTotalDuration(playlistId: Long): Flow<Long>

    // All playlists plus each one's song count and total duration in ONE query,
    // replacing a count flow and a duration flow per playlist.
    // LEFT JOIN keeps empty playlists; COALESCE turns their NULL sum into 0.
    // Aliases must match PlaylistWithStats's field names.
    // TODO: add "ORDER BY playlists.id" so row order is stable.
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