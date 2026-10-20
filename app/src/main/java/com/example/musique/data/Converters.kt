package com.example.musique.data

import android.net.Uri
import androidx.room.TypeConverter

/**
 * Room cannot store URI directly. So, it is stored as a string and rebuilt when read back.
 * Registered on AppDatabase with @TypeConverters. Room finds and calls these by their
 * parameters and return types so function names don't matter.
 */
class Converters {

    // Uri to String, called by Room when writing a song or playlist row.
    // Null stays null, which is how "no image" is stored.
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    // String to Uri, called by Room when reading a row. Null stays null.
    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}