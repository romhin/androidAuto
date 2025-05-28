package rom.visio.androidauto

import android.os.Bundle
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import androidx.media.MediaBrowserCompat
import androidx.media.session.MediaSessionCompat
import androidx.media.session.PlaybackStateCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSessionConnector

class MusicService : MediaBrowserServiceCompat() {

    private var player: Player? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri("https.www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
        player?.setMediaItem(mediaItem)
        player?.prepare()

        mediaSession = MediaSessionCompat(this, "MusicServiceSession").apply {
            setCallback(MediaSessionCallback())
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                    .build()
            )
            // isActive = true // Will be handled by MediaSessionConnector
        }
        sessionToken = mediaSession.sessionToken

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player)
        mediaSession.isActive = true // Activate session after connector is set
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.isActive = false
        mediaSessionConnector.setPlayer(null)
        player?.release()
        player = null
        mediaSession.release()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("@root@", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        if (parentId == "@root@") {
            val mediaItem = MediaBrowserCompat.MediaItem(
                MediaBrowserCompat.MediaDescriptionCompat.Builder()
                    .setMediaId("sample_track_1")
                    .setTitle("Sample Track 1")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
            mediaItems.add(mediaItem)
            result.sendResult(mediaItems)
        } else {
            result.sendResult(null)
        }
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            player?.play()
            Log.d("MusicService", "onPlay called")
        }

        override fun onPause() {
            super.onPause()
            player?.pause()
            Log.d("MusicService", "onPause called")
        }

        override fun onStop() {
            super.onStop()
            player?.stop()
            // Optionally, update PlaybackStateCompat to STATE_STOPPED if MediaSessionConnector doesn't handle it.
            // For now, relying on MediaSessionConnector.
            Log.d("MusicService", "onStop called")
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            // ExoPlayer's MediaSessionConnector usually handles this if a playlist is set.
            // For a single item, this might not do much without custom playlist logic.
            Log.d("MusicService", "onSkipToNext called")
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            // Similar to onSkipToNext, connector handles this.
            Log.d("MusicService", "onSkipToPrevious called")
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            Log.d("MusicService", "onPlayFromMediaId called with mediaId: $mediaId")
            if (mediaId == "sample_track_1") {
                val item = MediaItem.fromUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                player?.setMediaItem(item)
                player?.prepare()
                player?.play()
            }
        }
    }
}
