package com.palak.railindia.db

import androidx.room.*
import com.palak.railindia.model.ComponentEntry
import com.palak.railindia.model.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM entry")
    fun fetchAllFromDb() : Flow<List<Entry>>

    @Query("SELECT * FROM entry WHERE assignedToSync = 0 ORDER BY date ASC")
    fun fetchEntriesToSync() : Flow<List<Entry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: Entry) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponentEntry(componentEntry: ComponentEntry) : Long

    @Query("SELECT * FROM component_entry WHERE entryId = :entryId ORDER BY id")
    fun fetchComponentEntryFromEntryId(entryId : String) : List<ComponentEntry>

    @Update
    fun updateEntry(entry: Entry)
}