package com.palak.railindia.repo

import androidx.lifecycle.LiveData

interface Repo<T> {

    fun fetchAllFromDb() : LiveData<List<T>>
    suspend fun insertIntoDb(t : T) : Long
}