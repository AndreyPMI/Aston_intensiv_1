package com.andreypmi.musicapp.domain.repository

import com.andreypmi.musicapp.domain.model.TrackModel

interface MusicRepository {
    suspend fun getTrack(): List<TrackModel>
}