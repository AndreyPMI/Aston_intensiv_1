package com.andreypmi.musicapp.data.repositoryImpl

import android.content.Context
import android.content.Intent
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import com.andreypmi.musicapp.data.entity.TrackEntity
import com.andreypmi.musicapp.data.entity.toDomainModel
import com.andreypmi.musicapp.domain.model.TrackModel
import com.andreypmi.musicapp.domain.repository.MusicRepository
import com.andreypmi.musicapp.infrastructure.MusicPlayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class MusicRepositoryImpl(
    private val context: Context,
    @RawRes private val trackResources: IntArray
) : MusicRepository {
    private var currentTrackIndex = 0
    private val _currentTrack = MutableStateFlow<TrackModel?>(null)
    override val currentTrack: StateFlow<TrackModel?> = _currentTrack.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    private val musicServiceIntent = Intent(context, MusicPlayerService::class.java)
    private var tracks: List<TrackModel>


    init {
        tracks = trackResources.mapIndexed { index, resourceId ->
            val fileName = context.resources.getResourceEntryName(resourceId)
            TrackEntity(
                id = index,
                title = "Track $index",
                fileName = fileName,
                filePath = "android.resource://${context.packageName}/$resourceId"
            ).toDomainModel()
        }
        if (tracks.isNotEmpty()) {
            _currentTrack.value = tracks.first()
        }
    }


    override fun play(track: TrackModel) {
        musicServiceIntent.action = "PLAY"
        musicServiceIntent.putExtra("track", track)
        ContextCompat.startForegroundService(context, musicServiceIntent)
        _isPlaying.value = true
    }

    override fun pause(track: TrackModel) {
        musicServiceIntent.action = "PAUSE"
        musicServiceIntent.putExtra("track", track)
        context.startService(musicServiceIntent)
        _isPlaying.value = false
    }

    override fun playNext(): TrackModel? {
        if (tracks.isEmpty()) return null
        currentTrackIndex = (currentTrackIndex + 1) % tracks.size
        _currentTrack.value = tracks[currentTrackIndex]
        play(tracks[currentTrackIndex])
        return _currentTrack.value
    }

    override fun playPrevious(): TrackModel? {
        if (tracks.isEmpty()) return null
        currentTrackIndex = (currentTrackIndex - 1 + tracks.size) % tracks.size
        _currentTrack.value = tracks[currentTrackIndex]
        play(tracks[currentTrackIndex])
        return _currentTrack.value
    }

    override fun isPlaying(): Boolean {
        return _isPlaying.value
    }
    override fun getTrack() = tracks

    override fun stop() {
        musicServiceIntent.action = "STOP"
        context.startService(musicServiceIntent)
        _isPlaying.value = false
    }
}