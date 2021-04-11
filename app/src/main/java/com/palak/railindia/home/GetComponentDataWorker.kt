package com.palak.railindia.home

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.di.ComponentDataRef
import com.palak.railindia.model.Component
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import java.lang.Exception

@HiltWorker
public class GetComponentDataWorker
@AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    var componentRepo: ComponentRepo,
    @ComponentDataRef var componentDataRef: DatabaseReference
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return try {

            componentDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    GlobalScope.launch(Dispatchers.IO) {
                        snapshot.children.forEach { data ->
                            val component: Component? = data.getValue(Component::class.java)
                            component?.let {
                                componentRepo.insertIntoDb(component)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}