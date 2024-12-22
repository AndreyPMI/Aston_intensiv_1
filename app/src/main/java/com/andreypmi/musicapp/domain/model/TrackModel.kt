package com.andreypmi.musicapp.domain.model

import android.os.Parcel
import android.os.Parcelable


data class TrackModel (
    val id : Int,
    val title: String,
    val fileName: String,
    val filePath: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(fileName)
        parcel.writeString(filePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrackModel> {
        override fun createFromParcel(parcel: Parcel): TrackModel {
            return TrackModel(parcel)
        }

        override fun newArray(size: Int): Array<TrackModel?> {
            return arrayOfNulls(size)
        }
    }
}