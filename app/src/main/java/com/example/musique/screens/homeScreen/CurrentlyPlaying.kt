package com.example.musique.screens.homeScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musique.MusicViewModel
import com.example.musique.R
import com.example.musique.ui.components.NinePatchImage
import com.example.musique.ui.components.PixelSlider

@Composable
fun CurrentlyPlaying(viewModel: MusicViewModel) {

    val song = viewModel.currentSong ?: return
    viewModel.isPlaying
    val playlistName = viewModel.currentPlaylistName

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .combinedClickable(
                onClick = { viewModel.togglePlayPause() },
                onLongClick = { viewModel.stopPlayback() }
            )
    ) {
        NinePatchImage(
            drawableRes = R.drawable.blue_tile,
            modifier = Modifier.matchParentSize()
        )
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 18.dp, end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (song.imageUri != null) {
                    AsyncImage(
                        model = song.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .border(width = 2.dp, color = Color(0xff2b2a56), shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
                else {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        tint = Color(0xff2b2a56),
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xff2b2a56),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Artist: ${song.artist}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xff223963),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Playlist: $playlistName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xff223963),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formatSongDuration(song.duration),
                    color = Color(0xff2b2a56),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            PixelSlider(
                value = viewModel.sliderPosition,
                onValueChange = { viewModel.onSeekStart(it) },
                onValueChangeFinished = { viewModel.onSeekFinished() },
                valueRange = 0f..viewModel.songDuration,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 22.dp, start = 18.dp, end = 18.dp),
                thumbRes = R.drawable.slider_thumb_active,
                trackBgRes = R.drawable.slider_bg_active,
                trackFilledRes = R.drawable.slider_filled_active,
            )
        }
    }
}