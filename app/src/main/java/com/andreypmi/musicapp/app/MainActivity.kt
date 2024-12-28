package com.andreypmi.musicapp.app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.databinding.ActivityMainBinding
import com.andreypmi.musicapp.infrastructure.MusicPlayerService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var musicService: MusicPlayerService? = null
    private var isBound = false
    private val notificationPermissionRequestCode = 123


    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var trackTitle: TextView
    private lateinit var binding: ActivityMainBinding

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicService = (service as MusicPlayerService.MusicPlayerServiceBinder).service
            isBound = true
            setupObservers()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    private fun areNotificationsEnabled(): Boolean =
        (getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).areNotificationsEnabled()


    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !areNotificationsEnabled()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                notificationPermissionRequestCode
            )
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playPauseButton = binding.btnPlayPause
        nextButton = binding.btnNext
        previousButton = binding.btnPrevious
        trackTitle = binding.textTrackTitle

        val trackResources = intArrayOf(R.raw.track1, R.raw.track2, R.raw.track3)
        val serviceIntent = Intent(this, MusicPlayerService::class.java)


        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        val setTracksIntent = Intent(this, MusicPlayerService::class.java)
        setTracksIntent.putExtra("track_resources", trackResources)
        startService(setTracksIntent)


        playPauseButton.setOnClickListener {
            lifecycleScope.launch {
                musicService?.isPlaying?.collect { isPlaying ->
                    if (isPlaying) {
                        playPauseButton.setImageResource(R.drawable.ic_pause)
                    } else {
                        playPauseButton.setImageResource(R.drawable.ic_play)
                    }
                }
            }
            sendActionToService(MusicPlayerService.ACTION_TOGGLE_PLAY)
        }

        nextButton.setOnClickListener {
            sendActionToService(MusicPlayerService.ACTION_NEXT)
        }

        previousButton.setOnClickListener {
            sendActionToService(MusicPlayerService.ACTION_PREVIOUS)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            musicService?.currentTrack?.collect { track ->
                track?.let {
                    trackTitle.text = it.title
                }
            }
        }
    }

    private fun sendActionToService(action: String) {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            this.action = action
            musicService?.currentTrack?.value?.let {
                putExtra(MusicPlayerService.TRACK_EXTRA, it)
            }
        }
        startService(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}