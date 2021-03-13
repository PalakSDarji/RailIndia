package com.palak.railindia.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val app: Application): AndroidViewModel(app) {

    fun downloadComponentData(){
        val getComponentData = OneTimeWorkRequest.Builder(GetComponentDataWorker::class.java).build()
        WorkManager.getInstance(app).enqueue(getComponentData)
    }

}