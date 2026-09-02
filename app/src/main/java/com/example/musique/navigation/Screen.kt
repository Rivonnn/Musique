package com.example.musique.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}