package com.example.musique.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.musique.R
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    thumbRes: Int = R.drawable.slider_thumb,
    trackBgRes: Int = R.drawable.slider_bg,
    trackFilledRes: Int = R.drawable.slider_filled,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        modifier = modifier.height(24.dp),
        thumb = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp)
            ) {
                Image(
                    painter = painterResource(thumbRes),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(24.dp)
                )
            }
        },
        track = { sliderState ->
            val fraction = (sliderState.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            Box(
                modifier = Modifier.fillMaxWidth().height(18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(18.dp)) {
                    val totalWidth = maxWidth
                    NinePatchImage(
                        drawableRes = trackBgRes,
                        modifier = Modifier.fillMaxSize()
                    )
                    NinePatchImage(
                        drawableRes = trackFilledRes,
                        modifier = Modifier.width(totalWidth * fraction).height(18.dp)
                    )
                }
            }
        }
    )
}