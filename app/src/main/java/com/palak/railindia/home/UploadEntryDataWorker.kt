package com.palak.railindia.home

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DatabaseReference
import com.palak.railindia.di.DateSDF
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.di.EntryDataRef
import com.palak.railindia.model.FirebaseComponentEntry
import com.palak.railindia.model.FirebaseEntry
import com.palak.railindia.repo.EntryRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import javax.inject.Inject

@FlowPreview
@HiltWorker
public class UploadEntryDataWorker
@AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    var entryRepo: EntryRepo,
    var componentRepo: ComponentRepo,
    @EntryDataRef var entryDataRef: DatabaseReference
) : CoroutineWorker(context, params) {

    @Inject
    @DateSDF
    lateinit var dateSdf: SimpleDateFormat

    override suspend fun doWork(): Result {

        return try {
            GlobalScope.launch {

                val componentList = componentRepo.fetchAllFromDb().first()

                entryRepo.fetchEntriesToSync().collect { list ->

                    list.forEach { entry ->
                        Timber.d("Fetched entry : $entry")

                        val dateInStr = dateSdf.format(entry.date!!.time)

                        entry.componentEntry = entryRepo.fetchComponentEntry(entry.id)
                        Timber.d("Uploading entry : $entry")

                        //set synced flag to true and update in local.
                        entry.assignedToSync = true
                        entryRepo.updateEntry(entry)

                        val firebaseComponentEntryList = entry.componentEntry!!.map { ce ->
                            FirebaseComponentEntry(ce.id, ce.pass, ce.fail)
                        }

                        val firebaseEntry = FirebaseEntry(
                            entry.id,
                            dateInStr
                        )

                        entryDataRef.child(dateInStr).setValue(firebaseEntry)

                        firebaseComponentEntryList.forEach { firebaseComponentEntry ->

                            firebaseComponentEntry.componentName =
                                componentList.filter { component ->
                                    component.id == firebaseComponentEntry.id
                                }.map {
                                    it.name
                                }.first()

                            Timber.d("Syncing FirebaseComponentEntry : $firebaseComponentEntry")

                            entryDataRef.child(dateInStr).child(firebaseComponentEntry.id.toString())
                                .setValue(firebaseComponentEntry)
                        }

                        Timber.d("Now Set Synced to true")
                        entry.synced = true
                        entryRepo.updateEntry(entry)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}