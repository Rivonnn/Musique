package com.example.musique.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
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

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AppNavGraph(
    viewModel: MusicViewModel,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo("splash") { inclusive = true }
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
                    navController.navigate(Screen.Playlist.createRoute(playlist.id))
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
            val playlist = viewModel.getPlaylistById(playlistId) ?: return@composable

            PlaylistScreen(
                viewModel = viewModel,
                playlist = playlist,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}