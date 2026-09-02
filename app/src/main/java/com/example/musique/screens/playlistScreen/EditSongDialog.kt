package com.example.musique.screens.playlistScreen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.musique.R
import com.example.musique.ui.components.NinePatchImage
import com.example.musique.ui.components.PixelTextButton
import com.example.musique.ui.components.PixelTextField

@Composable
fun EditSongDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    artist: String,
    onArtistChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddImageClick: () -> Unit,
    onRemoveImageClick: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    imageUri: Uri?
) {
    var imageMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            NinePatchImage(
                drawableRes = R.drawable.orange_tile,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Edit Song!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if(!imageMode) {Color(0xff175546)}
                            else { Color(0xff2b2a56)},
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier.size(80.dp),
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 2.dp,
                                        color = if(!imageMode) {Color(0xff175546)}
                                                else { Color(0xff2b2a56)},
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { imageMode = !imageMode},
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                tint = if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)},
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 0.dp,
                                        color = Color(0x00FFFFFF),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if(!imageMode) {Color(0x70175546)}
                                    else { Color(0x702b2a56)})
                                    .clickable { imageMode = !imageMode }
                                ,
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.edit_pencil),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(all = 4.dp)
                                .align(Alignment.TopStart)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixelTextField(
                            value = title,
                            onValueChange = onTitleChange,
                            label = "Enter Title",
                            backgroundColor = if(!imageMode) {Color(0xff64BBA8)}
                                else { Color(0xff01B3D7)},
                            borderColor = if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)},
                            cursorColour= if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)},
                            textColor = if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)}
                        )
                        PixelTextField(
                            value = artist,
                            onValueChange = onArtistChange,
                            label = "Enter Artist",
                            backgroundColor = if(!imageMode) {Color(0xff64BBA8)}
                                else { Color(0xff01B3D7)},
                            borderColor = if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)},
                            cursorColour= if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)},
                            textColor = if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)}
                        )
                    }
                }

                if (title == "" || artist == "") {
                    Text(
                        "Title/Artist cannot be empty.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        textAlign = TextAlign.Center,
                        color = if(!imageMode) {Color(0xff175546)}
                                else { Color(0xff2b2a56)}
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)
                ) {
                    if (!imageMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PixelTextButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = if(!imageMode) {Color(0xff175546)}
                                            else { Color(0xff2b2a56)},
                                    modifier = Modifier.offset(y = (-6).dp)
                                )
                            }
                            PixelTextButton(
                                onClick = onSave,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = if(!imageMode) {Color(0xff175546)}
                                            else { Color(0xff2b2a56)},
                                    modifier = Modifier.offset(y = (-6).dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PixelTextButton(
                                onClick = {
                                    onRemoveImageClick()
                                    imageMode = false
                                },
                                backgroundRes = R.drawable.blue_tile,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Remove",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xff2b2a56),
                                    modifier = Modifier.offset(y = (-6).dp)
                                )
                            }
                            PixelTextButton(
                                onClick = {
                                    onAddImageClick()
                                    imageMode = false
                                },
                                backgroundRes = R.drawable.blue_tile,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Change",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xff2b2a56),
                                    modifier = Modifier.offset(y = (-6).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}