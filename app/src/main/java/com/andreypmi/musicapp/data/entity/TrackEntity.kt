package com.andreypmi.musicapp.data.entity

import com.andreypmi.musicapp.domain.model.TrackModel

data class TrackEntity (
    val id : Int,
    val title: String,
    val fileName: String,
    val filePath: String
)

fun TrackEntity.toDomainModel() : TrackModel{
    return TrackModel(
        id = this.id,
        title = this.title,
        fileName = this.fileName,
        filePath = this.filePath
    )
}