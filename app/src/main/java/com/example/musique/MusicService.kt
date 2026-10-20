package com.example.musique

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Owns the ExoPlayer and MediaSession, runs as a media service so that music plays in the background.
 * MusicViewModel connects to the service through a MediaController.
 * MediaSessionService posts media notifications.
 */
class MusicService : MediaSessionService() {

    // Session wraps the player and is what the controller connects to.
    // It is nullable so onDestroy can clear it after release.
    private var mediaSession: MediaSession? = null

    // Runs once when the service is first created.
    // Builds the player, then wraps it in the session.
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    // Called whenever a controller (ViewModel, notifications) asks to connect.
    // controllerInfo is never checked, so every controller is accepted.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Called when the user swipes the app out of recents.
    // Stops playback and empties the queue so nothing keeps playing with no UI.
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mediaSession?.player?.stop()
        mediaSession?.player?.clearMediaItems()
    }

    // Releases the player before the session, then drops the reference.
    // Both must be released or the player's resources and the session leak.
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}