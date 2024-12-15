package com.andreypmi.musicapp.infrastructure

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.andreypmi.musicapp.app.MusicApplication
import com.andreypmi.musicapp.presentation.MusicViewModel


class NotificationDismissReceiver : BroadcastReceiver() {
    private lateinit var musicViewModel: MusicViewModel

    override fun onReceive(context: Context, intent: Intent) {
        if (!::musicViewModel.isInitialized) {
            val application = context.applicationContext as MusicApplication
            musicViewModel = application.musicViewModel
        }
        when (intent.action) {
            "ACTION_TOGGLE_PLAY" -> musicViewModel.togglePlay()
            "ACTION_NEXT" -> musicViewModel.next()
            "ACTION_PREVIOUS" -> musicViewModel.previous()
            "ACTION_DISMISS" -> musicViewModel.stop()
        }
    }
}