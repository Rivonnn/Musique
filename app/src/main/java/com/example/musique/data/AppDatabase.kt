package com.example.musique.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.musique.model.Playlist
import com.example.musique.model.Song

/**
 * The Room database which stores the Song and Playlist tables.
 * It also stores the migrations which help update older installs upgrade to current schema.
 *
 */
@Database(entities = [Playlist::class, Song::class], version = 6)
// Room cannot store URI directly, converters help it store them as string.
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Entry point to all queries, MusicViewModel gets its dao from here.
    abstract fun musicDao(): MusicDao

    companion object {

        // @Volatile makes a newly created instance immediately visible to every thread.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1 -> v2: songs get an optional cover image.
        // v2 -> v3: playlists get an optional cover image.

        // v3 -> v4: rebuilds songs with a playlistId foreign key (CASCADE delete) and a
        // duration column. SQLite can't add a foreign key to an existing table, so the
        // table is rebuilt: create songs_new, copy rows (duration set to 0), drop the
        // old table, rename.

        // v4 -> v5: adds sortOrder for manual ordering. Existing songs all start at 0.

        // v5 -> v6: adds the (playlistId, sortOrder) index declared on Song.
        // The index name must match the one Room generates.

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN imageUri TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlists ADD COLUMN imageUri TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE songs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        playlistId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        artist TEXT NOT NULL,
                        duration INTEGER NOT NULL DEFAULT 0,
                        uri TEXT,
                        imageUri TEXT,
                        FOREIGN KEY (playlistId) REFERENCES playlists(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    INSERT INTO songs_new (id, playlistId, title, artist, duration, uri, imageUri)
                    SELECT id, playlistId, title, artist, 0, uri, imageUri FROM songs
                """)
                db.execSQL("DROP TABLE songs")
                db.execSQL("ALTER TABLE songs_new RENAME TO songs")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_songs_playlistId_sortOrder ON songs (playlistId, sortOrder)"
                )
            }
        }

        // Returns the one shared database. Double-checked locking: the first check skips
        // the lock when the instance exists; the second, inside synchronized, stops two
        // threads from both creating it.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}