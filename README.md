# Musique

## What is Musique?
Musique is a local music player which allows you to make playlists,
reorder your songs and keep a clean structure of your audio files!
It is meant to be a clean, retro-themed music player!

Musique never moves or changes your audio files. Any edits you make
(titles, artists, pictures) are stored inside the app only.

## Features
### Playlists!
Playlists are created with a name, and a custom picture can be added by editing the playlist afterwards.
Playlists display the total song count as well as the total duration of the playlist.
Playlists can be shuffled to play their songs in a random order.

### Songs!
Songs from the user's storage can be added to any playlist.
Title and artist are read from the file's tags (or shown as "Unknown" if missing).
The title, artist and picture are editable.
Songs can be reordered in any playlist using the up and down arrow buttons.
Songs can even be looped with the loop button.

### Playback!
The currently playing song is shown on the home screen with a seek bar.
Music keeps playing in the background, with controls in the notification.

## Install
1. Go to the [Releases](https://github.com/Rivonnn/Musique/releases/tag/v1.0.0) page and download the latest `.apk` file.
2. Open the file on your Android device (minSDK = 24, Android 7.0 or newer).
3. If asked, allow installs from this source, then tap Install.

Musique uses the system file picker to add songs, so it does not need storage permission.

## Tech Stack
Musique was built with Android Studio, Kotlin and the Jetpack Compose framework.

- **Jetpack Compose (Material3):** the user interface
- **Navigation Compose:** moving between the Home and Playlist screens
- **Room:** stores playlists and songs on the device
- **Media3 (ExoPlayer and MediaSession):** audio playback, background playback and notification controls
- **Coil:** loading cover pictures and the animated splash screen