package com.andreypmi.musicapp.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreypmi.musicapp.domain.model.TrackModel
import com.andreypmi.musicapp.domain.repository.MusicRepository
import kotlinx.coroutines.launch

class MainViewModel(val musicRepository : MusicRepository) : ViewModel() {
    private var _tracks = MutableLiveData<List<TrackModel>>(emptyList())
    val tracks: LiveData<List<TrackModel>> = _tracks
    val isPlaying: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        viewModelScope.launch {
            _tracks.value = musicRepository.getTrack()
            Log.d("AAA1",_tracks.value.toString())
            Log.d("AAA2",tracks.value.toString())
        }
    }

    fun play(trackIndex: Int) {
        viewModelScope.launch {
            Log.d("AAA","$trackIndex")
            val track = tracks.value?.getOrNull(trackIndex)
            Log.d("AAA",tracks.value.toString())
            if (track != null) {
                musicRepository.play(track)
                isPlaying.postValue(true)
            }
        }
    }

    fun pause() {
        viewModelScope.launch {
            musicRepository.pause()
            isPlaying.postValue(false)
        }
    }

    fun nextTrack() {
        viewModelScope.launch {
            musicRepository.nextTrack()
            isPlaying.postValue(true)
        }
    }

    fun previousTrack() {
        viewModelScope.launch {
            musicRepository.previousTrack()
            isPlaying.postValue(true)
        }
    }

}