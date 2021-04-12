package com.palak.railindia.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.palak.railindia.model.ComponentEntry
import com.palak.railindia.model.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM entry")
    fun fetchAllFromDb() : Flow<List<Entry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: Entry) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponentEntry(componentEntry: ComponentEntry) : Long
}