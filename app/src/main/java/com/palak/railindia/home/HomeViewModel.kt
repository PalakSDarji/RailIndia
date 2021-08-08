package com.palak.railindia.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.database.DatabaseReference
import com.palak.railindia.di.DateSDF
import com.palak.railindia.di.EntryDataRef
import com.palak.railindia.model.Component
import com.palak.railindia.model.Entry
import com.palak.railindia.model.FirebaseComponentEntry
import com.palak.railindia.model.FirebaseEntry
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.repo.EntryRepo
import com.palak.railindia.utils.HomeViewStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val app: Application,
    val componentRepo: ComponentRepo, private val entryRepo: EntryRepo,
    @EntryDataRef var entryDataRef: DatabaseReference
) : AndroidViewModel(app) {

    @Inject
    @DateSDF
    lateinit var dateSdf: SimpleDateFormat

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

    suspend fun syncEntryToServer(entry: Entry) {

        try {
            val job = viewModelScope.async {

                val componentList = componentRepo.fetchAllFromDb().first()

                Timber.d("Fetched entry : $entry")

                //Pretty print date format in String to make it human readable even on firebase.
                val dateInStr = dateSdf.format(entry.date!!.time)

                //Fetch component list from local db. We will need it later to extract
                // component name.
                //entry.componentEntry = entryRepo.fetchComponentEntry(entry.id)

                Timber.d("Uploading entry : $entry")

                //Set assignedToSync flag to true and update in local to indicate that this
                // item is already picked for syncing. So in next sync process, we can leave
                // it from syncing again.
                entry.assignedToSync = true
                entryRepo.updateIntoDb(entry)

                //Sync entry first.
                val firebaseEntry = FirebaseEntry(entry.id, dateInStr, entry.month!!, entry.qty)
                entryDataRef.child(dateInStr).setValue(firebaseEntry)

                //Sync entry's component list one by one. but before that, retrieve
                // component name from component list.
                entry.componentEntry?.map { ce ->

                    val compName = componentList.filter { component ->
                        component.id == ce.componentId
                    }.map { component ->
                        component.name
                    }.single()

                    //Wrap inside custom type to sync on firebase and return.
                    FirebaseComponentEntry(ce.id, ce.pass, ce.fail, compName, ce.componentId).also {

                        Timber.d("Syncing FirebaseComponentEntry : $it")

                        entryDataRef.child(dateInStr).child("compEntry")
                            .child(it.id.toString())
                            .setValue(it)
                    }
                }

                Timber.d("Synced data for ${entry.date} successfully!")

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