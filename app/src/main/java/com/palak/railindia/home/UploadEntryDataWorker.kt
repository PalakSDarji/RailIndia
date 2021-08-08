package com.palak.railindia.home

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DatabaseReference
import com.palak.railindia.di.DateSDF
import com.palak.railindia.di.EntryDataRef
import com.palak.railindia.model.FirebaseComponentEntry
import com.palak.railindia.model.FirebaseEntry
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.repo.EntryRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
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

                        //Pretty print date format in String to make it human readable even on firebase.
                        val dateInStr = dateSdf.format(entry.date!!.time)

                        //Fetch component list from local db. We will need it later to extract
                        // component name.
                        entry.componentEntry = entryRepo.fetchComponentEntry(entry.id)

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

                        //Set sync flag to true to make them finally synced.
                        entry.synced = true
                        entryRepo.updateIntoDb(entry)
                        //entryRepo.deleteEntry(entry)
                        Timber.d("Synced data for ${entry.date} successfully!")
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