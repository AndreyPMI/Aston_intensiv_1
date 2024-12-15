package com.andreypmi.musicapp.app

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.databinding.ActivityMainBinding
import com.andreypmi.musicapp.presentation.MusicViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val notificationPermissionRequestCode = 123
    private lateinit var musicViewModel: MusicViewModel

    private fun areNotificationsEnabled(): Boolean =
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).areNotificationsEnabled()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !areNotificationsEnabled()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                notificationPermissionRequestCode
            )
        }
        super.onCreate(savedInstanceState)
        val app = application as MusicApplication
        musicViewModel = app.musicViewModel
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnPlayPause.setOnClickListener {
            musicViewModel.togglePlay()
        }
        musicViewModel.isPlaying.observe(this){
            if (it){
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            } else{
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            }
        }
        lifecycleScope.launch {
            musicViewModel.currentTrackPosition.collectLatest{ track ->
                track?.let{
                    binding.textTrackTitle.text = it.title
                }
            }
        }
        binding.btnNext.setOnClickListener {
            musicViewModel.next()
        }

        binding.btnPrevious.setOnClickListener {
            musicViewModel.previous()
        }
    }

}
