package com.palak.railindia.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.model.Component
import com.palak.railindia.model.Entry
import com.palak.railindia.repo.EntryRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val app: Application,
                                        componentRepo: ComponentRepo, private val entryRepo: EntryRepo
): AndroidViewModel(app) {

    val componentLiveData : Flow<List<Component>> = componentRepo.fetchAllFromDb()

    var selectedDate : Date? = null
        set(value) {
            println("date updated : $value")
            field = value
        }

    fun downloadComponentData(){
        val getComponentData = OneTimeWorkRequest.Builder(GetComponentDataWorker::class.java).build()
        WorkManager.getInstance(app).enqueue(getComponentData)
    }

    fun saveEntry(entry : Entry){
        viewModelScope.launch {
            val entryId = entryRepo.insertIntoDb(entry)

            entry.componentEntry?.forEach {
                it.entryId = entryId.toInt()
                entryRepo.insertComponentEntry(it)
            }
        }
    }
}