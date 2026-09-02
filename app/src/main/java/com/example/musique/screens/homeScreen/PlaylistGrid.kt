package com.example.musique.screens.homeScreen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musique.MusicViewModel
import com.example.musique.model.Playlist
import androidx.compose.foundation.border
import com.example.musique.R
import com.example.musique.ui.components.NinePatchImage

fun formatTotalDuration(totalMs: Long): String {
    val totalMinutes = totalMs / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatSongDuration(totalMs: Long): String {
    val totalSeconds = totalMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistGrid(viewModel: MusicViewModel, onPlaylistClick: (Playlist) -> Unit) {
    val context = LocalContext.current
    val playlistList by viewModel.playlists.collectAsState()

    var editPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var newName by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingEditPlaylist by remember { mutableStateOf<Playlist?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            pendingEditPlaylist?.let {
                viewModel.updatePlaylist(it.copy(name = newName, imageUri = uri))
            }
            pendingEditPlaylist = null
        }
    }

    if (playlistList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "As Empty as the Abyss...",
                fontSize = 24.sp,
                lineHeight = 32.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 64.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(playlistList) { playlist ->
            val songCountFlow = remember(playlist.id) { viewModel.getSongCount(playlist.id) }
            val totalDurationFlow = remember(playlist.id) { viewModel.getTotalDuration(playlist.id) }
            val songCount by songCountFlow.collectAsState(initial = 0)
            val totalDuration by totalDurationFlow.collectAsState(initial = 0L)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onPlaylistClick(playlist) },
                        onLongClick = {
                            editPlaylist = playlist
                            newName = playlist.name
                            newImageUri = playlist.imageUri
                        }
                    )
            ) {
                NinePatchImage(
                    drawableRes = R.drawable.blue_tile,
                    modifier = Modifier.matchParentSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(top = 8.dp, bottom = 4.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (playlist.imageUri != null) {
                            AsyncImage(
                                model = playlist.imageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .border(width = 2.dp, color = Color(0xff2b2a56), shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp),
                                tint = Color(0xff2b2a56)
                            )
                        }
                    }
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xff2b2a56),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$songCount songs",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xff2b2a56),
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = formatTotalDuration(totalMs = totalDuration),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xff2b2a56),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    editPlaylist?.let { playlist ->
        EditPlaylistDialog(
            name = newName,
            onNameChange = { newName = it },
            onDismiss = { editPlaylist = null },
            onAddImageClick = {
                pendingEditPlaylist = playlist
                editPlaylist = null
                imagePickerLauncher.launch("image/*")
            },
            onRemoveImageClick = {
                viewModel.updatePlaylist(playlist.copy(imageUri = null))
                newImageUri = null
                editPlaylist = null
            },
            onDelete = {
                viewModel.deletePlaylist(playlist)
                editPlaylist = null
            },
            onSave = {
                if (newName.isNotBlank()) {
                    viewModel.updatePlaylist(playlist.copy(name = newName, imageUri = newImageUri))
                    editPlaylist = null
                }
            },
            imageUri = newImageUri
        )
    }
}