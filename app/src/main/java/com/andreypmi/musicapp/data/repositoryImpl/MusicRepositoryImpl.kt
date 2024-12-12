package com.andreypmi.musicapp.data.repositoryImpl

import android.content.Context
import androidx.annotation.RawRes
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.data.entity.TrackEntity
import com.andreypmi.musicapp.data.entity.toDomainModel
import com.andreypmi.musicapp.domain.model.TrackModel
import com.andreypmi.musicapp.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RawRes
private val trackResources = intArrayOf(R.raw.track1, R.raw.track2, R.raw.track3)

class MusicRepositoryImpl(private val context: Context) : MusicRepository {
    override suspend fun getTrack(): List<TrackModel> = withContext(Dispatchers.IO) {
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
}