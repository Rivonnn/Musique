package com.example.musique.screens.playlistScreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.musique.MusicViewModel
import com.example.musique.ui.components.PixelTextButton

@Composable
fun SongPicker(
    viewModel: MusicViewModel,
    playlistId: Long,
) {
    val context = LocalContext.current

    val importing = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addSongFromUri(context, uri, playlistId)
        }
    }

    PixelTextButton(onClick = { importing.launch(arrayOf("audio/*")) }) {
        Text(
            text = "ADD",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xff175546),
            modifier = Modifier.offset(y = (-6).dp)
        )
    }
}