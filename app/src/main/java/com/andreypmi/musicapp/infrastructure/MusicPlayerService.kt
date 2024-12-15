package com.andreypmi.musicapp.infrastructure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.app.MainActivity
import com.andreypmi.musicapp.domain.model.TrackModel


class MusicPlayerService : LifecycleService() {
    private lateinit var mediaPlayer: MediaPlayer
    private var currentTrackIndex = 0
    private var isPlaying: Boolean = false
    private var currentTrack: TrackModel? = null

    companion object {
        const val CHANNEL_ID = "music_player_service_channel"
        const val NOTIFICATION_ID = 101
    }

    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaPlayer = MediaPlayer()
        notificationManager = NotificationManagerCompat.from(this)
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
            when (it.action) {
                "PLAY" -> {
                    val track: TrackModel?
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                        track = it.getParcelableExtra<TrackModel>("track", TrackModel::class.java)
                    } else @Suppress("DEPRECATION") {
                        track = it.getParcelableExtra<TrackModel>("track")
                    }
                    track?.let {
                        if (currentTrack == null || currentTrack != it) {
                            isPlaying = false
                        }else isPlaying = true
                        currentTrack = it
                        playTrack(it)
                    }
                        ?: Log.e("MusicPlayerService", "Track is null")
                }

                "PAUSE" -> {
                    mediaPlayer.pause()
                    val track: TrackModel?
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                        track = it.getParcelableExtra<TrackModel>("track", TrackModel::class.java)
                    } else @Suppress("DEPRECATION") {
                        track = it.getParcelableExtra<TrackModel>("track")
                    }
                    val notification = buildNotification(track!!.title, this, false)
                    startForeground(NOTIFICATION_ID, notification)

                }
                "STOP" ->{
                    this.stopService(intent)
                }
                else -> null
            }
        }
        return START_STICKY
    }

    private fun playTrack(track: TrackModel) {
        try {
            if (!isPlaying) {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(this, Uri.parse(track.filePath))
                mediaPlayer.prepare()
            }
            mediaPlayer.start()
            currentTrackIndex = track.id
            val notification = buildNotification(track.title, this, true)
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("playTrack", "Error playing track: ${e.message}", e)
        }
    }

    private fun buildNotification(
        trackTitle: String,
        context: Context,
        isPlay: Boolean
    ): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val remoteViews = RemoteViews(context.packageName, R.layout.widget)

        val nextIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = "ACTION_NEXT"
        }
        val nextPendingIntent =
            PendingIntent.getBroadcast(
                context,
                2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val previousIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = "ACTION_PREVIOUS"
        }
        val previousPendingIntent =
            PendingIntent.getBroadcast(
                context,
                3,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        val dismissIntent  = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = "ACTION_DISMISS"
        }
        val dismissPendingIntent  =
            PendingIntent.getBroadcast(
                context,
                3,
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE
            )



        remoteViews.setOnClickPendingIntent(R.id.btn_previous, previousPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.btn_next, nextPendingIntent)
        val playPauseIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = "ACTION_TOGGLE_PLAY"
        }

        val playPausePendingIntent =
            PendingIntent.getBroadcast(
                context,
                1,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        if (isPlay) {
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
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setContentTitle("Playing...")
            .setContentText(trackTitle)
            .setVibrate(null)
            .setDeleteIntent(dismissPendingIntent)
            .build()

    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        mediaPlayer.release()
        super.onDestroy()
    }

}