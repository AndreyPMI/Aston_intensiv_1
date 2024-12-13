package com.andreypmi.musicapp.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andreypmi.musicapp.data.repositoryImpl.MusicRepositoryImpl
import com.andreypmi.musicapp.databinding.ActivityMainBinding
import com.andreypmi.musicapp.domain.repository.MusicRepository
import com.andreypmi.musicapp.presentation.MainViewModel

class MainActivity : AppCompatActivity() {
    private val notificationPermissionRequestCode = 123
    private val musicViewModel: MainViewModel by viewModels {
        MusicViewModelFactory(this)
    }

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
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlayPause.setOnClickListener {
            musicViewModel.play(1)
        }
        binding.btnNext.setOnClickListener {
            musicViewModel.nextTrack()
        }

        binding.btnPrevious.setOnClickListener {
            musicViewModel.previousTrack()
        }
    }

}

class MusicViewModelFactory(private val activity: Activity) :
    ViewModelProvider.Factory { // Передаем Activity
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(MusicRepositoryImpl(activity)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
