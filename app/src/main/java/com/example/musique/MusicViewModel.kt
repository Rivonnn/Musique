package com.example.musique

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musique.data.AppDatabase
import com.example.musique.model.Playlist
import com.example.musique.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * MusicViewModel is shared by all the screens, so that playback state can survive across
 * navigation between 'Home' and 'Playlist'.
 *
 * Purpose of view model:
 * 1. Persistence: Songs and playlists live in Room (accessed from dao).
 * 2. Playback: The ExoPlayer lives inside MusicService, this class talks to it through
 *    controller and mirrors the player's state into Compose state so that the
 *    UI can observe it.
 */
class MusicViewModel(application: Application) : AndroidViewModel(application) {

    // Entry point to all Room queries.
    private val dao = AppDatabase.getDatabase(application).musicDao()

    // The MediaController which connects MusicService with MusicViewModel.
    // It is set to null initially as it takes some time to load at startup.
    var controller: MediaController? = null
        private set

    // Kept so that onCleared() can release the connection
    private var controllerFuture: ListenableFuture<MediaController>? = null

    // All playlists, collected as soon as the ViewModel exists so the list
    // is ready when screens open.
    val playlists = dao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Set to true once the controller connects to MusicService.
    var isPlayerReady by mutableStateOf(false)
        private set

    // For the UI to know if music is currently playing or paused.
    var isPlaying by mutableStateOf(false)
        private set

    // Playback position in ms (Uses float as the slider uses float values.)
    var sliderPosition by mutableFloatStateOf(0f)
        private set

    // Track the duration of song, default to 1f so the slider doesn't fetch an empty 0..0 range.
    var songDuration by mutableFloatStateOf(1f)
        private set

    // Index of the song which is currently in play in currentQueue. (-1 means nothing is playing)
    var currentSongIndex by mutableIntStateOf(-1)
        private set

    // Is true when the user is dragging the slider
    // Needed so that the polling loop doesn't fight the manual dragging of the slider.
    var isSeeking by mutableStateOf(false)

    // Song which is shown on the home screen as the currentlyPlaying block.
    var currentSong by mutableStateOf<Song?>(null)
        private set

    // Mirrors shuffle mode, so that SHUFFLE button can change states.
    var isShuffled by mutableStateOf(false)
        private set

    // Mirrors loop mode, so that the LOOP button can change states.
    // Loop refers to looping of one song (REPEAT_MODE_ONE), not the entire queue of songs.
    var isLooping by mutableStateOf(false)
        private set

    // The songs passed to playSongs(), kept so the player's index can be mapped
    // back to a Song even if the on-screen list changes later.
    private var currentQueue: List<Song> = emptyList()

    // To determine the playlist of the song in currentlyPlaying block. (Used for the label)
    var currentPlaylistName by mutableStateOf<String?>(null)
        private set
    var currentPlaylistId by mutableStateOf<Long?>(null)
        private set

    /**
     * Runs once when MusicViewModel is created.
     *
     *  1. Connects to MusicService asynchronously and stores the controller.
     *  2. Registers a listener which stops playback when the last track ends.
     *  3. Starts the slider polling loop which copies the position, duration and index from
     *     the controller into Compose twice a second (500ms).
     *
     *  Polling loop is used as it's a simple way of moving the slider forward.
     */
    init {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()

        // Connects to MusicService and then stores the controller.
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                isPlayerReady = true

                controller?.addListener(object : Player.Listener {
                    // Stops playback when the last item ends.
                    // Otherwise, does nothing, the player advances itself.
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED && controller?.hasNextMediaItem() == false) {
                            stopPlayback()
                        }
                    }
                })

            } catch (_: Exception) {
                isPlayerReady = false
                controller = null
            }
        }, MoreExecutors.directExecutor())

        // Polling loop which lasts the lifetime of MusicViewModel.
        // Skipped while paused or when user is manually dragging the slider.
        viewModelScope.launch {
            while (isActive) {
                if (isPlaying && !isSeeking) {
                    val c = controller ?: run {
                        delay(500.milliseconds)
                        continue
                    }
                    val pos = c.currentPosition.toFloat()
                    val dur = c.duration.takeIf { it > 0 }?.toFloat() ?: 1f
                    sliderPosition = pos
                    songDuration = dur
                    val newIndex = c.currentMediaItemIndex
                    // Catches track changes we didn't trigger (auto-advance, notification next/previous).
                    // Update currentlyPlaying and reset slider position with duration.
                    if (newIndex >= 0 && newIndex != currentSongIndex) {
                        currentSongIndex = newIndex
                        currentSong = currentQueue.getOrNull(newIndex)
                        sliderPosition = 0f
                        songDuration = 1f
                    }
                }
                delay(500.milliseconds)
            }
        }
    }

    // Releases the connection to MusicService.
    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    // Playlist CRUD. Each launches in viewModelScope because the DAO functions are
    // suspend functions, which need a coroutine to run in.
    fun addPlaylist(name: String) {
        viewModelScope.launch {
            dao.insertPlaylist(Playlist(name = name))
        }
    }
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            dao.deletePlaylist(playlist)
        }
    }
    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            dao.updatePlaylist(playlist)
        }
    }

    // Simple insert, not related to file URI based insertion.
    fun addSong(song: Song) {
        viewModelScope.launch {
            dao.insertSong(song)
        }
    }

    /**
     * Adds an audio file to a playlist. Shared by SongAdder (Home) and SongPicker
     * (Playlist screen), which differ only in which playlistId they pass.
     *
     * Steps: keep read access to the file across app restarts, read the embedded
     * tags (title/artist/duration), fall back to "Unknown", then save.
     *
     * NOTE: runs on the calling (main) thread and doesn't catch errors from a
     * corrupt file. TODO: move to Dispatchers.IO and wrap in try/catch.
     */
    fun addSongFromUri(context: Context, uri: Uri, playlistId: Long) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        // Opens the file to read its embedded tags.
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)

        // Stores the metadata related to the audio file.
        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        retriever.release()

        // Add the song after required information is received.
        addSong(
            Song(
                playlistId = playlistId,
                title = title ?: "Unknown",
                artist = artist ?: "Unknown",
                duration = durationMs,
                uri = uri
            )
        )
    }
    fun deleteSong(song: Song) {
        viewModelScope.launch {
            dao.deleteSong(song)
        }
    }
    fun updateSong(song: Song) {
        viewModelScope.launch {
            dao.updateSong(song)
        }
    }

    // A mutable copy of the current playlist which is displayed.
    // Used for swapSongs() so that it can reorder instantly before the database write finishes.
    private val _currentPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylistSongs: StateFlow<List<Song>> = _currentPlaylistSongs

    // Holds the active collector so opening a different playlist cancels the old one.
    // Otherwise, two would run and overwrite each other's list.
    private var songLoadJob: Job? = null

    // True from loadSongsForPlaylist() until the first emission.
    // NOTE: not read by any screen as of now, and the previous playlist's songs
    // stay in _currentPlaylistSongs meanwhile.
    private val _isLoadingPlaylistSongs = MutableStateFlow(false)
    val isLoadingPlaylistSongs: StateFlow<Boolean> = _isLoadingPlaylistSongs

    // Loads the songs of the current playlist for display.
    // Called from nav graph before navigating.
    fun loadSongsForPlaylist(playlistId: Long) {
        songLoadJob?.cancel()
        _isLoadingPlaylistSongs.value = true
        songLoadJob = viewModelScope.launch {
            dao.getSongsForPlaylist(playlistId).collect {
                _currentPlaylistSongs.value = it
                _isLoadingPlaylistSongs.value = false
            }
        }
    }

    // Forward straight to the DAO because screens only to the ViewModel.
    // PlaylistGrid calls both once per playlist card (2 live queries per card).
    fun getSongCount(playlistId: Long) = dao.getSongCount(playlistId)
    fun getTotalDuration(playlistId: Long) = dao.getTotalDuration(playlistId)

    /**
     * Starts playing songs from startIndex and records which playlist they came from.
     *
     * Songs with no uri can't be played, so they're left out of what the player gets.
     * That shifts positions, so adjustedStart converts the tapped song's position
     * in the full list into its position in the filtered list.
     *
     * NOTE: currentQueue and currentSongIndex use the FULL list, but the player's
     * queue is the filtered one. They only agree when every song has a uri.
     * TODO: store the filtered list so the indices always match.
     * NOTE: UI state is updated even if the controller isn't connected yet.
     */
    fun playSongs(songs: List<Song>, startIndex: Int, playlistName: String, playlistId: Long) {
        val indexed = songs.mapIndexedNotNull { i, s ->
            s.uri?.let { uri -> Pair(i, MediaItem.fromUri(uri)) }
        }
        if (indexed.isEmpty()) return

        val mediaItems = indexed.map { it.second }
        val adjustedStart = indexed.indexOfFirst { it.first == startIndex }.takeIf { it >= 0 } ?: 0

        controller?.run {
            setMediaItems(mediaItems, adjustedStart, 0L)
            prepare()
            play()
        }
        currentQueue = songs
        currentSong = songs.getOrNull(startIndex)
        currentSongIndex = startIndex
        sliderPosition = 0f
        songDuration = 1f
        isPlaying = true
        currentPlaylistName = playlistName
        currentPlaylistId = playlistId
    }

    // A toggle to keep track of play and pause states.
    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
            } else {
                it.play()
                isPlaying = true
            }
        }
    }

    // Seeks the player to this position (ms). Called by onSeekFinished.
    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    // Stops the player and clears the now-playing song, index and slider.
    // Playlist name/id and currentQueue are left as they were.
    // Called on long-press of the currently-playing card or deleting/reordering songs.
    fun stopPlayback() {
        controller?.stop()
        isPlaying = false
        currentSongIndex = -1
        currentSong = null
        sliderPosition = 0f
        songDuration = 1f
    }

    // onSeekStart updates the slider's position
    // onSeekFinished actually sets the position to the value
    /* The split is made so that the song continues as normal when the user is dragging
       the slider and change to the position only occurs when slider is released. */
    fun onSeekStart(position: Float) {
        isSeeking = true
        sliderPosition = position
    }
    fun onSeekFinished() {
        seekTo(sliderPosition.toLong())
        isSeeking = false
    }

    // Looks up a Playlist by ID from the loaded snapshot.
    // Null before playlists has emitted or after deletion
    // (NavGraph then shows a blank screen).
    fun getPlaylistById(id: Long): Playlist? {
        return playlists.value.find { it.id == id }
    }

    /**
     * Swaps two adjacent songs and stores the new order.
     *
     * The on-screen list is updated first then each song's sortOrder is rewritten in
     * the database.
     *
     * Stops playback if the playing song is swapped,
     * because the highlight is index-based and would land on the wrong row
     * NOTE: swapping other songs leaves the player's upcoming order unchanged
     * NOTE: it isn't ideal UX.
     */
    fun swapSongs(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val current = _currentPlaylistSongs.value.toMutableList()
        val temp = current[fromIndex]
        current[fromIndex] = current[toIndex]
        current[toIndex] = temp
        _currentPlaylistSongs.value = current

        if (currentPlaylistId == playlistId &&
            (currentSongIndex == fromIndex || currentSongIndex == toIndex)) {
            stopPlayback()
        }

        viewModelScope.launch {
            val updatedSongs = current.mapIndexed { index, song ->
                song.copy(sortOrder = index)
            }
            dao.updateSongs(updatedSongs)
        }
    }

    // On/Off toggle for shuffle, mirrors it for the SHUFFLE button state.
    fun toggleShuffle() {
        isShuffled = !isShuffled
        controller?.shuffleModeEnabled = isShuffled
    }

    // On/Off toggle for loop, mirrors it for the loop button state.
    // With REPEAT_MODE_ONE the player never reaches STATE_ENDED,
    // so the end of queue listener in init never fires while looping.
    fun toggleLoop() {
        isLooping = !isLooping
        controller?.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }
}