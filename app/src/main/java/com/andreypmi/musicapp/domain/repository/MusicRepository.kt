package com.andreypmi.musicapp.domain.repository

import com.andreypmi.musicapp.domain.model.TrackModel

interface MusicRepository {
    suspend fun getTrack(): List<TrackModel>
    suspend fun play(track: TrackModel): Unit
    suspend fun pause(): Unit
    suspend fun nextTrack(): Unit
    suspend fun previousTrack(): Unit
}