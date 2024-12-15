package com.andreypmi.musicapp.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andreypmi.musicapp.domain.model.TrackModel
import com.andreypmi.musicapp.domain.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicViewModel(application: Application, private val musicRepository: MusicRepository) : AndroidViewModel(application) {
    private val _currentTrackPosition = MutableStateFlow<TrackModel?>(null)
    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying
    val currentTrackPosition = _currentTrackPosition.asStateFlow()


    fun play() {
        musicRepository.currentTrack.value?.let {
            musicRepository.play(it)
            _isPlaying.value = true
        }
    }

    fun pause() {
        musicRepository.currentTrack.value?.let {
            musicRepository.pause(it)
            _isPlaying.value = false
        }
    }

    fun next() {
        musicRepository.playNext()
        _currentTrackPosition.value = musicRepository.currentTrack.value
        _isPlaying.value = true
    }

    fun previous() {
        musicRepository.playPrevious()
        _currentTrackPosition.value = musicRepository.currentTrack.value
        _isPlaying.value = true
    }
    fun togglePlay() {
        if (_isPlaying.value == true) {
            pause()
        } else {
            play()
        }
    }
    fun stop(){
        musicRepository
    }
}
