package com.example.musique.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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