package rom.visio.androidauto

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class MyMediaLibraryService : MediaLibraryService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // 1️⃣ Создаем ExoPlayer
        val player = ExoPlayer.Builder(this).build()

        // 2️⃣ Создаем MediaLibrarySession (вместо MediaSession)
        mediaSession = MediaLibrarySession.Builder(this, player, MyLibraryCallback())
            .build()

        // 3️⃣ Запускаем сервис в фоне (Foreground Service)
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaLibrarySession? {
        return mediaSession as? MediaLibrarySession
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "music_channel"
        private const val NOTIFICATION_ID = 1

        fun startService(context: android.content.Context) {
            val intent = Intent(context, MyMediaLibraryService::class.java)
            context.startForegroundService(intent)
        }
    }
}

// 4️⃣ Callback для MediaLibrarySession (обработка запросов библиотеки)
class MyLibraryCallback : MediaLibraryService.MediaLibrarySession.Callback {
    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        // Возвращаем корневой элемент (например, список жанров/альбомов)
        val rootItem = MediaItem.Builder()
            .setMediaId("root")
            .setUri("androidx://media3-session/root")
            .build()
        return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
    }

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        // Возвращаем список дочерних элементов (например, треки в альбоме)
        val children = mutableListOf<MediaItem>()

        if (parentId == "root") {
            // Пример: список альбомов
            children.add(MediaItem.Builder().setMediaId("album1").setUri("...").build())
            children.add(MediaItem.Builder().setMediaId("album2").setUri("...").build())
        } else if (parentId.startsWith("album")) {
            // Пример: список треков в альбоме
            children.add(MediaItem.Builder().setMediaId("track1").setUri("...").build())
            children.add(MediaItem.Builder().setMediaId("track2").setUri("...").build())
        }

        return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
    }

    // Обработка команды Play
    override fun onPlayRequested(
        session: MediaSession,
        controller: ControllerInfo
    ): ListenableFuture<SessionResult> {
        session.player.play()
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    // Обработка команды Pause
    override fun onPauseRequested(
        session: MediaSession,
        controller: ControllerInfo
    ): ListenableFuture<SessionResult> {
        session.player.pause()
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }



}