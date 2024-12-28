package com.andreypmi.musicapp.infrastructure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.app.MainActivity
import com.andreypmi.musicapp.data.entity.TrackEntity
import com.andreypmi.musicapp.data.entity.toDomainModel
import com.andreypmi.musicapp.domain.model.TrackModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayerService() : LifecycleService() {

    private lateinit var mediaPlayer: MediaPlayer
    private var currentTrackIndex = 0
    var isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _currentTrack = MutableStateFlow<TrackModel?>(null)
    val currentTrack: StateFlow<TrackModel?> = _currentTrack.asStateFlow()
    private var tracks: List<TrackModel> = listOf()
    private var trackResources: IntArray = intArrayOf()
    private lateinit var context: Context


    companion object {
        const val CHANNEL_ID = "music_player_service_channel"
        const val NOTIFICATION_ID = 1011
        const val ACTION_PLAY = "PLAY"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_STOP = "STOP"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
        const val ACTION_TOGGLE_PLAY = "ACTION_TOGGLE_PLAY"
        const val ACTION_DISMISS = "ACTION_DISMISS"

        const val TRACK_EXTRA = "track"
    }

    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        context = this
        createNotificationChannel()
        mediaPlayer = MediaPlayer()
        notificationManager = NotificationManagerCompat.from(this)
        setTracks(trackResources)

        mediaPlayer.setOnCompletionListener {
            playNextTrack()
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Music Player Service Channel",
            NotificationManager.IMPORTANCE_HIGH

        ).apply {
            setVibrationPattern(longArrayOf(0))
            setSound(null, null)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            if (it.hasExtra("track_resources")) {
                val resources = it.getIntArrayExtra("track_resources")
                resources?.let { setTracks(it) }
            }
            when (it.action) {
                ACTION_PLAY -> {
                    val track = getTrackFromIntent(it)
                    track?.let {
                        _currentTrack.value = it
                        playTrack(it)
                    }
                }

                ACTION_PAUSE -> {
                    pauseTrack()
                }

                ACTION_DISMISS -> {
                    stopTrack()
                }

                ACTION_STOP -> {
                    stopSelf()
                }

                ACTION_NEXT -> {
                    playNextTrack()
                }

                ACTION_PREVIOUS -> {
                    playPreviousTrack()
                }

                ACTION_TOGGLE_PLAY -> {
                    togglePlayPause()
                }

                else -> null
            }
        }
        return START_STICKY
    }


    private fun getTrackFromIntent(intent: Intent?): TrackModel? {
        return intent?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra(TRACK_EXTRA, TrackModel::class.java)
            } else @Suppress("DEPRECATION") {
                it.getParcelableExtra(TRACK_EXTRA)
            }
        }
    }

    private fun playTrack(track: TrackModel) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, Uri.parse(track.filePath))
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying.value = true
            currentTrackIndex = track.id
            val notification = buildNotification(track.title, this)
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: IllegalStateException) {
            Log.e("playTrack", "Error playing track: ${e.message}", e)
        }
    }

    private fun pauseTrack() {
        mediaPlayer.pause()
        isPlaying.value = false
        currentTrack.value?.let {
            val notification = buildNotification(it.title, this)
            startForeground(NOTIFICATION_ID, notification)
        }

    }

    private fun stopTrack() {
        mediaPlayer.stop()
        isPlaying.value = false
        currentTrack.value?.let {
            val notification = buildNotification(it.title, this)
            startForeground(NOTIFICATION_ID, notification)
        }

    }


    private fun playNextTrack() {
        if (tracks.isEmpty()) return

        val nextIndex = (currentTrackIndex + 1) % tracks.size
        _currentTrack.value = tracks[nextIndex]
        playTrack(tracks[nextIndex])
    }


    private fun playPreviousTrack() {
        if (tracks.isEmpty()) return

        val previousIndex = (currentTrackIndex - 1 + tracks.size) % tracks.size
        _currentTrack.value = tracks[previousIndex]
        playTrack(tracks[previousIndex])

    }


    private fun togglePlayPause() {
        if (isPlaying.value) {
            pauseTrack()
        } else {
            currentTrack.value?.let { playTrack(it) }
        }
    }


    private fun buildNotification(
        trackTitle: String,//TODO Добавить заголовок в уведомление
        context: Context
    ): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val remoteViews = RemoteViews(context.packageName, R.layout.widget)

        val nextIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent =
            PendingIntent.getService(
                context,
                2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val previousIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val previousPendingIntent =
            PendingIntent.getService(
                context,
                3,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        val dismissIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = ACTION_DISMISS
        }
        val dismissPendingIntent =
            PendingIntent.getService(
                context,
                4,
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE
            )


        remoteViews.setOnClickPendingIntent(R.id.btn_previous, previousPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.btn_next, nextPendingIntent)
        val playPauseIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = ACTION_TOGGLE_PLAY
        }

        val playPausePendingIntent =
            PendingIntent.getService(
                context,
                1,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        if (isPlaying.value) {
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_pause)
        } else {
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_play)
        }
        remoteViews.setOnClickPendingIntent(R.id.btn_play_pause, playPausePendingIntent)


        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setSmallIcon(R.drawable.ic_play)
            .setColor(Color.RED)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setVibrate(null)
            .setDeleteIntent(dismissPendingIntent)
            .build()

    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        if (mediaPlayer.isPlaying) {
            stopTrack()
        }
        mediaPlayer.release()
        super.onDestroy()
    }


    fun setTracks(resources: IntArray) {
        if (context != null) {
            trackResources = resources
            tracks = trackResources.mapIndexed { index, resourceId ->
                val fileName = try {
                    context.resources.getResourceEntryName(resourceId)
                } catch (e: Exception) {
                    "Track $index"
                }
                TrackEntity(
                    id = index,
                    title = fileName,
                    fileName = fileName,
                    filePath = "android.resource://${context.packageName}/$resourceId"
                ).toDomainModel()
            }
            if (tracks.isNotEmpty()) {
                _currentTrack.value = tracks.first()
            }
        }
    }


    inner class MusicPlayerServiceBinder : Binder() {
        val service: MusicPlayerService
            get() = this@MusicPlayerService
    }


    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return MusicPlayerServiceBinder()
    }
}