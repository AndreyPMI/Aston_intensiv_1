package com.andreypmi.musicapp.app

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andreypmi.musicapp.R
import com.andreypmi.musicapp.data.repositoryImpl.MusicRepositoryImpl
import com.andreypmi.musicapp.domain.repository.MusicRepository
import com.andreypmi.musicapp.presentation.MusicViewModel

class MusicApplication : Application() {
    lateinit var musicViewModel: MusicViewModel

    override fun onCreate() {
        super.onCreate()
        val trackResources = intArrayOf(R.raw.track1, R.raw.track2, R.raw.track3)
        val repository = MusicRepositoryImpl(applicationContext, trackResources)
        val factory = MusicViewModelFactory(this, repository)
        musicViewModel = factory.create(MusicViewModel::class.java)
    }
}
class MusicViewModelFactory(private val application: Application, private val repository: MusicRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(application,repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}