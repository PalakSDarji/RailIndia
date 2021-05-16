package com.palak.railindia.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.palak.railindia.model.Component
import com.palak.railindia.model.Entry
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.repo.EntryRepo
import com.palak.railindia.utils.HomeViewStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val app: Application,
    componentRepo: ComponentRepo, private val entryRepo: EntryRepo
) : AndroidViewModel(app) {

    private val homeViewStatusMutableFlow = MutableStateFlow<HomeViewStatus>(HomeViewStatus.Empty)
    val homeViewStatusFlow : StateFlow<HomeViewStatus> = homeViewStatusMutableFlow

    val componentLiveData: Flow<List<Component>> = componentRepo.fetchAllFromDb()

    var selectedDate: Date? = null
        set(value) {
            println("date updated : $value")
            field = value
        }

    fun downloadComponentData() {
        val getComponentData = OneTimeWorkRequest.Builder(GetComponentDataWorker::class.java)
            .build()
        WorkManager.getInstance(app).enqueue(getComponentData)
    }

    suspend fun saveEntry(entry: Entry) {

        try {
            val job = viewModelScope.async {
                entryRepo.insertIntoDb(entry)

                entry.componentEntry?.forEach {
                    it.entryId = entry.id
                    entryRepo.insertComponentEntry(it)
                }

                uploadEntries()
            }

            job.await()
        } catch (e: Exception) {
            throw e
        }
    }

    fun uploadEntries() {

        val uploadEntryDataWorker =
            OneTimeWorkRequest.Builder(UploadEntryDataWorker::class.java).build()
        WorkManager.getInstance(app).enqueue(uploadEntryDataWorker)
    }

    suspend fun searchByDate(dateInStr: String): Flow<Result<Entry>> {

        return entryRepo.searchByDate(dateInStr)
    }

    suspend fun searchByMonth(monthInStr: String) : Flow<Result<List<Entry>>>{

        return entryRepo.searchByMonth(monthInStr)
    }

    fun setHomeViewStatus(homeViewStatus: HomeViewStatus){
        homeViewStatusMutableFlow.value = homeViewStatus
    }
}