package com.example.musique.screens.homeScreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.musique.MusicViewModel
import com.example.musique.R
import com.example.musique.ui.components.NinePatchImage
import com.example.musique.ui.components.PixelTextButton

@Composable
fun SongAdder(viewModel: MusicViewModel, modifier: Modifier = Modifier) {
    var dialog by remember { mutableStateOf(false) }
    var selectedPlaylistId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val playlistList by viewModel.playlists.collectAsState()

    val importing = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            dialog = false
            selectedPlaylistId?.let { id ->
                viewModel.addSongFromUri(context, uri, id)
            }
            selectedPlaylistId = null
        }
    }

    Box(modifier = modifier) {
        PixelTextButton(
            onClick = { dialog = true },
            backgroundRes = R.drawable.bg_blue_tile,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            Text(
                text = "+ SONG",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xff2b2a56),
                modifier = Modifier.offset(x = (-8).dp)
            )
        }
    }

    if (dialog) {
        Dialog(onDismissRequest = { dialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                NinePatchImage(
                    drawableRes = R.drawable.bg_orange_tile,
                    modifier = Modifier.matchParentSize()
                )
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Add Songs!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xff175546)
                    )
                    PixelTextButton(
                        onClick = {
                            showCreatePlaylist = true
                            dialog = false
                        },
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "CREATE PLAYLIST!",
                            color = Color(0xff175546),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (playlistList.isEmpty()) {
                        Text(
                            "Create a playlist first",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xff175546),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(playlistList, key = { it.id }) { playlist ->
                                TextButton(
                                    onClick = {
                                        selectedPlaylistId = playlist.id
                                        importing.launch(arrayOf("audio/*"))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = Color(0xff64BBA8),
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(2.dp, Color(0xff175546)),
                                ) {
                                    Text(
                                        playlist.name,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color(0xff175546),
                                    )
                                }
                            }
                        }
                        Text(
                            "Select a playlist",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xff175546),
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showCreatePlaylist) {
        PlaylistCreateDialog(
            viewModel = viewModel,
            onDismiss = { showCreatePlaylist = false }
        )
    }
}