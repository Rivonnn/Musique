package com.example.musique.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat

// Shared across all NinePatchImage instances — decoded once per resource, reused everywhere
private val ninePatchCache = mutableMapOf<Int, Drawable?>()

@Composable
fun NinePatchImage(drawableRes: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawable = ninePatchCache.getOrPut(drawableRes) {
        ResourcesCompat.getDrawable(context.resources, drawableRes, context.theme)
    }

    Canvas(modifier = modifier) {
        drawable?.let {
            it.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawIntoCanvas { canvas -> it.draw(canvas.nativeCanvas) }
        }
    }
}