package com.example.musique.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.musique.R

val RetroFontFamily = FontFamily(
    Font(R.font.pixel_operator, FontWeight.Normal)
)

val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
    bodyMedium = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    bodySmall = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp),
    titleSmall = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.sp),
    labelLarge = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
    labelMedium = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),
    labelSmall = TextStyle(fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.sp)
)