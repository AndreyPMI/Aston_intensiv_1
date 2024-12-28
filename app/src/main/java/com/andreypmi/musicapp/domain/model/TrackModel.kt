package com.andreypmi.musicapp.domain.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable


data class TrackModel (
    val id : Int,
    val title: String,
    val fileName: String,
    val filePath: String
):Serializable