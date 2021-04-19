package com.palak.railindia.repo

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

interface Repo<T> {

    fun fetchAllFromDb() : Flow<List<T>>
    suspend fun insertIntoDb(t : T) : Long
    suspend fun updateIntoDb(t : T)
}