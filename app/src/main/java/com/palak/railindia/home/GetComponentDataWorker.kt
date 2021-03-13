package com.palak.railindia.home

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.palak.railindia.di.ComponentDataRef
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
public class GetComponentDataWorker
    @AssistedInject constructor(@Assisted context: Context,
                                @Assisted params: WorkerParameters,
                                @ComponentDataRef var componentDataRef : DatabaseReference)
    : CoroutineWorker(context,params) {

        override suspend fun doWork(): Result {

        componentDataRef.addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEach {
                    data ->
                    println("GetComponentDataWorker dataSnapshot : ${data.value}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return Result.success()
    }
}