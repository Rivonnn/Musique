package com.example.musique.screens.playlistScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.musique.MusicViewModel
import com.example.musique.R
import com.example.musique.screens.homeScreen.formatSongDuration
import com.example.musique.model.Playlist
import com.example.musique.model.Song
import com.example.musique.ui.components.NinePatchImage
import com.example.musique.ui.components.PixelSlider
import com.example.musique.ui.components.PixelTextButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    viewModel: MusicViewModel,
    playlist: Playlist,
    song: Song,
    songs: List<Song>,
    index: Int,
    isLast: Boolean,
    isCurrent: Boolean,
    onLongClick: () -> Unit,
) {
    // Connector between song cards
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(horizontal = 38.dp)
    ) {
        NinePatchImage(
            drawableRes = R.drawable.connectors,
            modifier = Modifier.matchParentSize()
        )
    }

    // Song card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .combinedClickable(
                onClick = {
                    val c = viewModel.controller
                    if (viewModel.currentSongIndex != index ||
                        viewModel.currentPlaylistId != playlist.id ||
                        (c?.isPlaying == false && c.playbackState == Player.STATE_ENDED)
                    ) {
                        viewModel.playSongs(songs, index, playlist.name, playlist.id)
                    } else {
                        viewModel.togglePlayPause()
                    }
                },
                onLongClick = onLongClick
            )
    ) {
        NinePatchImage(
            drawableRes = R.drawable.teal_button,
            modifier = Modifier.matchParentSize()
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
                    .offset(y = (-3).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (song.imageUri != null) {
                    AsyncImage(
                        model = song.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .border(width = 1.dp, color = Color(0xff175546), shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        tint = Color(0xff175546),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(all = 2.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xff175546),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xff2d6e5e),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    if (isCurrent) {
                        Image(
                            painter = painterResource(
                                if (viewModel.isLooping) R.drawable.loop_active else R.drawable.loop
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { viewModel.toggleLoop() }
                        )
                    }
                    Text(
                        text = formatSongDuration(song.duration),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xff175546),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    modifier = Modifier.height(52.dp)
                ) {
                    if (index > 0) {
                        PixelTextButton(
                            modifier = Modifier.size(22.dp),
                            backgroundRes = R.drawable.arrow_up,
                            onClick = { viewModel.swapSongs(playlist.id, index, index - 1) }
                        )
                    }
                    if (!isLast) {
                        PixelTextButton(
                            modifier = Modifier.size(22.dp),
                            backgroundRes = R.drawable.arrow_down,
                            onClick = { viewModel.swapSongs(playlist.id, index, index + 1) }
                        )
                    }
                }
            }
            if (isCurrent) {
                val sliderPosition = viewModel.sliderPosition   // read locally, scoped to this composable only
                val songDuration = viewModel.songDuration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    PixelSlider(
                        value = sliderPosition,
                        onValueChange = { viewModel.onSeekStart(it) },
                        onValueChangeFinished = { viewModel.onSeekFinished() },
                        valueRange = 0f..songDuration,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = (-8).dp)
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}