package com.example.musique.screens.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.musique.R

@Composable
fun MusicAppHeader() {
    Image(
        painter = painterResource(id = R.drawable.img_musique_title),
        contentDescription = "Musique",
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 16.dp, bottom = 4.dp),
        contentScale = ContentScale.Crop,
    )
}