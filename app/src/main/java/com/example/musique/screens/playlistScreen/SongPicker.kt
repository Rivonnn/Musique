package com.example.musique.screens.playlistScreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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

    PixelTextButton(
        onClick = { importing.launch(arrayOf("audio/*")) },
//        icon = {
//            Box(
//                Modifier.padding(bottom = 10.dp, start = 6.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Add,
//                    contentDescription = null,
//                    tint = Color(0xff175546),
//                    modifier = Modifier
//                        .size(32.dp)
//                )
//            }
//        }
    ) {
        Text(
            text = "ADD",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xff175546),
            modifier = Modifier.offset(y = (-6).dp)
        )
    }
}