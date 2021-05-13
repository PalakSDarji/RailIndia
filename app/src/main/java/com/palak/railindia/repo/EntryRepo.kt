package com.palak.railindia.repo

import com.palak.railindia.db.EntryDao
import com.palak.railindia.model.ComponentEntry
import com.palak.railindia.model.Entry
import com.palak.railindia.network.EntryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EntryRepo @Inject constructor(val entryDao: EntryDao, val entryService: EntryService) : Repo<Entry> {
    override fun fetchAllFromDb(): Flow<List<Entry>> {
        return entryDao.fetchAllFromDb()
    }

    override suspend fun insertIntoDb(entry: Entry): Long {
        return withContext(Dispatchers.IO){
            entryDao.insertEntry(entry)
        }
    }

    suspend fun insertComponentEntry(componentEntry: ComponentEntry) : Long {
        return withContext(Dispatchers.IO){
            entryDao.insertComponentEntry(componentEntry)
        }
    }

    fun fetchEntriesToSync() : Flow<List<Entry>> = entryDao.fetchEntriesToSync()

    fun fetchComponentEntry(entryId : String) = entryDao.fetchComponentEntryFromEntryId(entryId)

    override suspend fun updateIntoDb(entry: Entry) {
        withContext(Dispatchers.IO){
            entryDao.updateEntry(entry)
        }
    }

    suspend fun searchByDate(date : String) : Flow<Result<Entry>>{
        return withContext(Dispatchers.IO){
            entryService.searchEntryByDate(date)
        }
    }
}