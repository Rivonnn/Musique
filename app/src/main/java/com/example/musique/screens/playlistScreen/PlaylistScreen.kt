package com.example.musique.screens.playlistScreen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musique.MusicViewModel
import com.example.musique.model.Playlist
import com.example.musique.ui.components.PixelTextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.musique.ui.components.NinePatchImage
import com.example.musique.R
import com.example.musique.model.Song


@Composable
fun PlaylistScreen(
    viewModel: MusicViewModel,
    playlist: Playlist,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val songs by viewModel.currentPlaylistSongs.collectAsState()
    val isLoading by viewModel.isLoadingPlaylistSongs.collectAsState()

    var newTitle by remember { mutableStateOf("") }
    var newArtist by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var dialogEditId by remember { mutableStateOf<Long?>(null) }
    var pendingEditSong by remember { mutableStateOf<Song?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            pendingEditSong?.let { song ->
                viewModel.updateSong(
                    song.copy(
                        title = newTitle.ifBlank { song.title },
                        artist = newArtist.ifBlank { song.artist },
                        imageUri = uri
                    )
                )
            }
        }
        pendingEditSong = null
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.main_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with back button and song picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PixelTextButton(
                    onClick = { onBack() },
                ) {
                    Text(
                        text = "BACK",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xff175546),
                        modifier = Modifier.offset(y = (-6).dp)
                    )
                }
                PixelTextButton(
                    onClick = { viewModel.toggleShuffle() },
                    backgroundRes = if (viewModel.isShuffled) { R.drawable.blue_tile}
                        else { R.drawable.teal_button}
                ) {
                    Text(
                        text = "SHUFFLE",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (viewModel.isShuffled) Color(0xff2b2a56) else Color(0xff175546),
                        modifier = Modifier.offset(y = (-6).dp)
                    )
                }
                SongPicker(viewModel = viewModel, playlistId = playlist.id)
            }

            // Playlist name banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                NinePatchImage(
                    drawableRes = R.drawable.teal_button,
                    modifier = Modifier.matchParentSize()
                )
                Text(
                    text = playlist.name,
                    color = Color(0xff175546),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                )
            }

            when {
                // Nothing has loaded yet.
                isLoading -> { }

                // List is empty, display this.
                songs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "As Empty As The Void...",
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }

                // List has some items, render them.
                else -> {
                    LazyColumn (
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                            SongListItem(
                                viewModel = viewModel,
                                playlist = playlist,
                                song = song,
                                songs = songs,
                                index = index,
                                isLast = index == songs.lastIndex,
                                isCurrent = song.id == viewModel.currentSong?.id,
                                onLongClick = {
                                    dialogEditId = song.id
                                    newTitle = song.title
                                    newArtist = song.artist
                                    newImageUri = song.imageUri
                                }
                            )
                        }
                    }
                }
            }
        }

        // Song edit dialog
        dialogEditId?.let { id ->
            val song = songs.find { it.id == id }
            if (song == null) {
                dialogEditId = null
            } else {
                EditSongDialog(
                    title = newTitle,
                    onTitleChange = { newTitle = it },
                    artist = newArtist,
                    onArtistChange = { newArtist = it },
                    onDismiss = { dialogEditId = null },
                    onAddImageClick = {
                        pendingEditSong = song
                        dialogEditId = null
                        imagePickerLauncher.launch(arrayOf("image/*"))
                    },
                    onRemoveImageClick = {
                        viewModel.updateSong(
                            song.copy(
                                title = newTitle.ifBlank { song.title },
                                artist = newArtist.ifBlank { song.artist },
                                imageUri = null
                            )
                        )
                        newImageUri = null
                        dialogEditId = null
                    },
                    onDelete = {
                        if (viewModel.currentSong?.id == song.id) {
                            viewModel.stopPlayback()
                        }
                        viewModel.deleteSong(song)
                        dialogEditId = null
                    },
                    onSave = {
                        if (newTitle.isNotBlank() && newArtist.isNotBlank()) {
                            viewModel.updateSong(song.copy(title = newTitle, artist = newArtist))
                            dialogEditId = null
                        }
                    },
                    imageUri = newImageUri
                )
            }
        }
    }
}