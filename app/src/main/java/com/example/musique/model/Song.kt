package com.example.musique.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One song row, belonging to exactly one playlist. Deleting a playlist deletes
 * its songs (CASCADE). `uri` is the audio file; `imageUri` is optional cover art.
 * `sortOrder` is the position within the playlist. The composite index speeds up
 * the "songs for playlist, in order" query and covers the playlistId foreign key.
 * NOTE: sortOrder defaults to 0, so new songs tie at 0 until the first swap.
 */
@Entity(
    tableName = "songs",
    foreignKeys = [ForeignKey(
        entity = Playlist::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["playlistId", "sortOrder"])]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long = 0,
    var title: String,
    var artist: String,
    val duration: Long = 0,
    val uri: Uri? = null,
    val imageUri: Uri? = null,
    val sortOrder: Int = 0
)