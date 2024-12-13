package com.andreypmi.musicapp.infrastructure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.domain.model.TrackModel
import kotlin.math.log

//
//class MusicPlayerService : Service() {
//    private lateinit var mediaPlayer: MediaPlayer
//    private var currentTrackIndex = 0
//     private val trackResources = intArrayOf(R.raw.track1, R.raw.track2, R.raw.track3)
//
//    companion object {
//        const val CHANNEL_ID = "music_player_service_channel"
//        const val NOTIFICATION_ID = 101
//    }
//
//    private lateinit var notificationManager: NotificationManagerCompat
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("AAAC","create")
//        createNotificationChannel()
//        mediaPlayer = MediaPlayer.create(this, trackResources[currentTrackIndex])
//        notificationManager = NotificationManagerCompat.from(this)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                "Music Player Service Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(serviceChannel)
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d("AAA", "2")
//        intent?.let {
//            when (it.action) {
//                "PLAY" -> {
//                    var track: TrackModel? = null
//                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
//                        track = it.getParcelableExtra<TrackModel>("track", TrackModel::class.java)
//                    } else @Suppress("DEPRECATION") {
//                        track = it.getParcelableExtra<TrackModel>("track")
//                    }
//                    if (track != null) {
//                        val notification = buildNotification(track?.title ?: "", applicationContext)
//                        try {
//                            startForeground(NOTIFICATION_ID, notification)
//                            Log.d("AAA11", "+")
//                            // showNotification()
//                        } catch (
//                            e: Exception
//                        ) {
//                            Log.d("AAA3", e.message.toString())
//                        }
//                        playTrack(track = track)
//                    }
//                    return START_STICKY
//                }
//
//                "PAUSE" -> {
//                    if (mediaPlayer.isPlaying) {
//                        mediaPlayer.pause()
//                    }
//                }
//
//                "NEXT" -> {
//                    nextTrack()
//                }
//
//                "PREVIOUS" -> {
//                    previousTrack()
//                }
//            }
//        }
//        return START_STICKY
//    }
//
//    private fun buildNotification(trackTitle: String, context: Context): Notification {
//        val remoteViews = RemoteViews(context.packageName, R.layout.widget)
//
//        return NotificationCompat.Builder(context, CHANNEL_ID)
//            .setCustomContentView(remoteViews)
//            .setCustomBigContentView(remoteViews)
//            .setSmallIcon(R.drawable.ic_play)
//            .setColor(Color.RED)
//            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
//            .setOngoing(true)
//            .setAutoCancel(false)
//            .setContentTitle("Playing...")
//            .setContentText(trackTitle)
//            .build()
//    }
//
//    override fun onDestroy() {
//        Log.d("AAA12", "destroy")
//        stopForeground(Service.STOP_FOREGROUND_REMOVE)
//        super.onDestroy()
//        mediaPlayer.stop()
//        mediaPlayer.release()
//
//    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//    private fun playTrack(track: TrackModel) {
//        Log.d("AAA", "${track.toString()}")
//        if (currentTrackIndex != track.id) {
//            mediaPlayer.reset()
//            mediaPlayer.setDataSource(
//                this,
//                Uri.parse(track.filePath)
//            )
//            mediaPlayer.prepare()
//        }
//
//        mediaPlayer.start()
//        currentTrackIndex = track.id
//    }
//
//    private fun nextTrack() {
////        currentTrackIndex = (currentTrackIndex + 1) % trackResources.size
////        playTrack(currentTrackIndex)
//    }
//
//    private fun previousTrack() {
////        currentTrackIndex = (currentTrackIndex - 1 + trackResources.size) % trackResources.size
////        playTrack(currentTrackIndex)
//    }
//}
class MusicPlayerService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private var currentTrackIndex = 0

    companion object {
        const val CHANNEL_ID = "music_player_service_channel"
        const val NOTIFICATION_ID = 101
    }

    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaPlayer = MediaPlayer() // Инициализируем MediaPlayer без трека
        notificationManager = NotificationManagerCompat.from(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("AAA","Chanel created")
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "PLAY" -> {
                    var track: TrackModel? = null
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                        track = it.getParcelableExtra<TrackModel>("track", TrackModel::class.java)
                    } else @Suppress("DEPRECATION") {
                        track = it.getParcelableExtra<TrackModel>("track")
                    }
                    track?.let { playTrack(it) } ?: Log.e("MusicPlayerService", "Track is null")
                }

                "PAUSE" -> mediaPlayer.pause()
                "NEXT" -> playNextTrack()
                "PREVIOUS" -> playPreviousTrack()
                else -> null
            }
        }
        return START_STICKY
    }

    private fun playTrack(track: TrackModel) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, Uri.parse(track.filePath))
            mediaPlayer.prepare()
            mediaPlayer.start()
            currentTrackIndex = track.id
            val notification = buildNotification(track.title, this)
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("AAAQQQ", "Error playing track: ${e.message}", e)
        }
    }


    private fun playNextTrack() {
        // TODO(): Добавить логику переключения на следующий трек
    }

    private fun playPreviousTrack() {
        // TODO() Добавить логику переключения на предыдущий трек
    }

    private fun buildNotification(trackTitle: String, context: Context): Notification {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget)
        return NotificationCompat.Builder(context, CHANNEL_ID)
        //    .setCustomContentView(remoteViews)
        //    .setCustomBigContentView(remoteViews)
            .setSmallIcon(R.drawable.ic_play)
            .setColor(Color.RED)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentTitle("Playing...")
            .setContentText(trackTitle)
            .build()
    }

    override fun onDestroy() {
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}