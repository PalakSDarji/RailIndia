package com.palak.railindia.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.model.Component
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val app: Application,
    val componentRepo: ComponentRepo
): AndroidViewModel(app) {

    val componentLiveData : LiveData<List<Component>> = componentRepo.fetchAllFromDb()

    var selectedDate : Date? = null
        set(value) {
            println("date updated : $value")
            field = value
        }

    fun downloadComponentData(){
        val getComponentData = OneTimeWorkRequest.Builder(GetComponentDataWorker::class.java).build()
        WorkManager.getInstance(app).enqueue(getComponentData)
    }

}