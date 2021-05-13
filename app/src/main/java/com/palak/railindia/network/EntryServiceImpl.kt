package com.palak.railindia.network

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.palak.railindia.di.DateSDF
import com.palak.railindia.di.EntryDataRef
import com.palak.railindia.model.Entry
import com.palak.railindia.model.FirebaseComponentEntry
import com.palak.railindia.model.FirebaseEntry
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.utils.DateNotFoundException
import com.palak.railindia.utils.Utils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import javax.inject.Inject

class EntryServiceImpl @Inject constructor(@EntryDataRef var entryDataRef: DatabaseReference,
    @DateSDF var dateSdf : SimpleDateFormat, val componentRepo: ComponentRepo) : EntryService {

    @ExperimentalCoroutinesApi
    override fun searchEntryByDate(date: String) = callbackFlow<Result<Entry>>{

        val postListener = object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    snapshot.children.forEach {
                        val firebaseEntry = it.getValue(FirebaseEntry::class.java) as FirebaseEntry
                        println("it : $it")
                        println("firebaseEntry : $firebaseEntry")

                        val list = mutableListOf<FirebaseComponentEntry>()
                        it.child("compEntry").children.forEach {
                            val c = it.getValue(FirebaseComponentEntry::class.java) as FirebaseComponentEntry
                            list.add(c)
                            println("FirebaseComponentEntry : $c")
                        }

                        val entry = Utils.convertFirebaseEntryToEntry(firebaseEntry, list ,dateSdf)

                        runBlocking {
                            entry.componentEntry?.forEach {
                                it.component = componentRepo.getComponentFromId(it.componentId)
                            }
                        }

                        this@callbackFlow.sendBlocking(Result.success(entry))
                    }


                }
                else{
                    this@callbackFlow.sendBlocking(Result.failure(DateNotFoundException("Date not found in Database!")))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(Result.failure(DateNotFoundException("Something is wrong while making network request. Please try later!")))
            }
        }
        entryDataRef.orderByChild("date").equalTo(date).limitToFirst(1).addListenerForSingleValueEvent(postListener)

        awaitClose {
            entryDataRef.removeEventListener(postListener)
        }
    }
}