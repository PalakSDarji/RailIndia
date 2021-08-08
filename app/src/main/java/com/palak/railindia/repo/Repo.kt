package com.palak.railindia.repo

import kotlinx.coroutines.flow.Flow

interface Repo<T> {

    fun fetchAllFromDb() : Flow<List<T>>
    suspend fun insertIntoDb(t : T)
    suspend fun updateIntoDb(t : T)
}