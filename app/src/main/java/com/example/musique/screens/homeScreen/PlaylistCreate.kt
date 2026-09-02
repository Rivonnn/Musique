package com.example.musique.screens.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.musique.MusicViewModel
import com.example.musique.R
import com.example.musique.ui.components.NinePatchImage
import com.example.musique.ui.components.PixelTextButton
import com.example.musique.ui.components.PixelTextField

@Composable
fun PlaylistCreate(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
) {
     var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        PixelTextButton(
            onClick = {
                showDialog = true
            },
            backgroundRes = R.drawable.blue_tile_dim,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            Text(
                text = "+ PLAYLIST",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xff2b2a56)
            )
        }
    }
    if (showDialog) {
        PlaylistCreateDialog(
            viewModel = viewModel,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun PlaylistCreateDialog(viewModel: MusicViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = {
        name = ""
        onDismiss()
    }) {
        Box(modifier = Modifier.fillMaxWidth()) {
            NinePatchImage(
                drawableRes = R.drawable.orange_tile,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(all = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Create Playlist!",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 10.dp),
                    color = Color(0xff175546)
                )
                PixelTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Playlist Name",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                )

                if (name == "") {
                    Text(
                        "Playlist name cannot be empty.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 8.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xff175546)
                    )
                }
                if (name != "") {
                    Spacer(Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelTextButton(
                        onClick = {
                            name = ""
                            onDismiss()
                        },
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "CANCEL",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xff175546)
                        )
                    }
                    PixelTextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addPlaylist(name)
                                name = ""
                                onDismiss()
                            }
                        },
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "CREATE",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xff175546)
                        )
                    }
                }
            }
        }
    }
}