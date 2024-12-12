package com.andreypmi.musicapp.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andreypmi.musicapp.domain.model.TrackModel

class MainViewModel() : ViewModel() {
    private val _tracks = MutableLiveData<List<TrackModel>>(emptyList())
    val tracks: LiveData<List<TrackModel>> = _tracks
}