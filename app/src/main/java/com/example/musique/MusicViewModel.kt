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

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).musicDao()

    var isPlayerReady by mutableStateOf(false)
        private set

    val playlists = dao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val playlistsWithStats = dao.getAllPlaylistsWithStats()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var controller: MediaController? = null
        private set

    private var controllerFuture: ListenableFuture<MediaController>? = null

    // Playback state — persists across navigation
    var isPlaying by mutableStateOf(false)
        private set
    var sliderPosition by mutableFloatStateOf(0f)
        private set
    var songDuration by mutableFloatStateOf(1f)
        private set
    var currentSongIndex by mutableIntStateOf(-1)
        private set
    var isSeeking by mutableStateOf(false)

    var currentSong by mutableStateOf<Song?>(null)
        private set

    var isShuffled by mutableStateOf(false)
        private set

    var isLooping by mutableStateOf(false)
        private set

    private var currentQueue: List<Song> = emptyList()

    var currentPlaylistName by mutableStateOf<String?>(null)
        private set

    var currentPlaylistId by mutableStateOf<Long?>(null)
        private set

    init {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                isPlayerReady = true

                // Handle track ending via event listener — single source of truth
                controller?.addListener(object : Player.Listener {
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

        // Polling loop — runs for the lifetime of the ViewModel
        // Only handles position/index updates; track-end is handled by the listener above
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

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

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

    fun addSong(song: Song) {
        viewModelScope.launch {
            dao.insertSong(song)
        }
    }

    // Shared by SongAdder (home screen) and SongPicker (playlist screen) — both
    // pick a file URI and need the same permission-grant + metadata-extraction +
    // save flow, differing only in which playlistId the song is attached to.
    fun addSongFromUri(context: Context, uri: Uri, playlistId: Long) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)

        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        retriever.release()

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

    private val _currentPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylistSongs: StateFlow<List<Song>> = _currentPlaylistSongs
    private var songLoadJob: Job? = null

    private val _isLoadingPlaylistSongs = MutableStateFlow(false)
    val isLoadingPlaylistSongs: StateFlow<Boolean> = _isLoadingPlaylistSongs

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

    fun getSongCount(playlistId: Long) = dao.getSongCount(playlistId)

    fun getTotalDuration(playlistId: Long) = dao.getTotalDuration(playlistId)

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

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun stopPlayback() {
        controller?.stop()
        isPlaying = false
        currentSongIndex = -1
        currentSong = null
        sliderPosition = 0f
        songDuration = 1f
    }

    fun onSeekStart(position: Float) {
        isSeeking = true
        sliderPosition = position
    }

    fun onSeekFinished() {
        seekTo(sliderPosition.toLong())
        isSeeking = false
    }

    fun getPlaylistById(id: Long): Playlist? {
        return playlists.value.find { it.id == id }
    }

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

    fun toggleShuffle() {
        isShuffled = !isShuffled
        controller?.shuffleModeEnabled = isShuffled
    }

    fun toggleLoop() {
        isLooping = !isLooping
        controller?.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }
}