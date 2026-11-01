package com.example.musique.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.musique.MusicViewModel
import com.example.musique.screens.homeScreen.HomeScreen
import com.example.musique.screens.playlistScreen.PlaylistScreen
import com.example.musique.screens.SplashScreen

private const val TRANSITION_DURATION = 600

@Composable
fun AppNavGraph(
    viewModel: MusicViewModel,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            },
        ) {
            HomeScreen(
                viewModel = viewModel,
                onPlaylistClick = { playlist ->
                    viewModel.loadSongsForPlaylist(playlist.id)
                    navController.navigate(Screen.Playlist.createRoute(playlist.id)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.Playlist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            }
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable

            // Safety net: loads the songs if this screen was restored without a tap
            // (for example after the app process was killed). No-op when already loaded.
            LaunchedEffect(playlistId) { viewModel.loadSongsForPlaylist(playlistId) }

            // Collected, so the screen appears as soon as the playlists arrive.
            val playlists by viewModel.playlists.collectAsState()
            val playlist = playlists.find { it.id == playlistId } ?: return@composable

            PlaylistScreen(
                viewModel = viewModel,
                playlist = playlist,
                onBack = { navController.popBackStack() }
            )
        }
    }
}