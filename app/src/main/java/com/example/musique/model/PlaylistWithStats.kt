package com.example.musique.model

import androidx.room.Embedded

/**
 * Not a table. The result shape of MusicDao.getAllPlaylistsWithStats():
 * a playlist plus its song count and total duration, computed in one query.
 * songCount and totalDuration must match the column aliases in that query.
 */
data class PlaylistWithStats(
    @Embedded val playlist: Playlist,
    val songCount: Int,
    val totalDuration: Long
)