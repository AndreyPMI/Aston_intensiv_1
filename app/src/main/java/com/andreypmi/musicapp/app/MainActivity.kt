package com.andreypmi.musicapp.app

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.data.repositoryImpl.MusicRepositoryImpl
import com.andreypmi.musicapp.databinding.ActivityMainBinding
import com.andreypmi.musicapp.domain.repository.MusicRepository

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
           val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mediaPlayer: MediaPlayer = MediaPlayer.create(this, Uri.parse("android.resource://${this.packageName}/${R.raw.track1}"))
        binding.btnPlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
        }
    }
}