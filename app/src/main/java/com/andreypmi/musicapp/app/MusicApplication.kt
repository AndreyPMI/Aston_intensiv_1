package com.andreypmi.musicapp.app

import android.app.Application
import android.content.Intent
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.infrastructure.MusicPlayerService

    class MusicApplication : Application() {

        override fun onCreate() {
            super.onCreate()
            val trackResources = intArrayOf(R.raw.track1, R.raw.track2, R.raw.track3)
            val serviceIntent = Intent(this, MusicPlayerService::class.java)
            serviceIntent.putExtra("track_resources",trackResources)
            startService(serviceIntent)
        }
    }