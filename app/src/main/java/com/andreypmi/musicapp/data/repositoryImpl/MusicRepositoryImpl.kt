package com.andreypmi.musicapp.data.repositoryImpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.data.entity.TrackEntity
import com.andreypmi.musicapp.data.entity.toDomainModel
import com.andreypmi.musicapp.domain.model.TrackModel
import com.andreypmi.musicapp.domain.repository.MusicRepository
import com.andreypmi.musicapp.infrastructure.MusicPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MusicRepositoryImpl(private val context: Activity) : MusicRepository {
    @RawRes
    private val trackResources = intArrayOf(R.raw.track1, R.raw.track2, R.raw.track3)

    override suspend fun getTrack(): List<TrackModel> = withContext(Dispatchers.IO) {
        Log.d("AAA", "give list")
        trackResources.mapIndexed { index, resourceId ->
            val fileName = context.resources.getResourceEntryName(resourceId)
            TrackEntity(
                id = index,
                title = "Track $index",
                fileName = fileName,
                filePath = "android.resource://${context.packageName}/$resourceId"
            ).toDomainModel()
        }
    }

    override suspend fun play(track: TrackModel) {
        withContext(Dispatchers.Main) {
            Log.d("AAA", "1")
            val intent = Intent(context, MusicPlayerService::class.java).apply {
                action = "PLAY"
                putExtra("track", track)
            }
            Log.d("AAAС", context.toString())
            ContextCompat.startForegroundService(context, intent)
        }
    }

    override suspend fun pause() {
        val intent = Intent(context, MusicPlayerService::class.java).apply {
            action = "PAUSE"
        }
        context.startService(intent)
    }

    override suspend fun nextTrack() {
        val intent = Intent(context, MusicPlayerService::class.java).apply {
            action = "NEXT"
        }
        context.startService(intent)
    }

    override suspend fun previousTrack() {
        val intent = Intent(context, MusicPlayerService::class.java).apply {
            action = "PREVIOUS"
        }
        context.startService(intent)
    }
}