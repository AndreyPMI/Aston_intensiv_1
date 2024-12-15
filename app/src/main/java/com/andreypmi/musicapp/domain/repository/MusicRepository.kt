package com.andreypmi.musicapp.domain.repository

import com.andreypmi.musicapp.domain.model.TrackModel
import kotlinx.coroutines.flow.StateFlow

interface MusicRepository {
    val currentTrack: StateFlow<TrackModel?>
    val isPlaying: StateFlow<Boolean>
    fun play(track: TrackModel)
    fun pause(track: TrackModel)
    fun playNext(): TrackModel?
    fun playPrevious(): TrackModel?
    fun getTrack(): List<TrackModel>
    fun isPlaying(): Boolean
    fun stop()
}
