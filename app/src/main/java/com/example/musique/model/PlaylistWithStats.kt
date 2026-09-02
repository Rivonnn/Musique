package com.example.musique.model

import androidx.room.Embedded

data class PlaylistWithStats(
    @Embedded val playlist: Playlist,
    val songCount: Int,
    val totalDuration: Long
)