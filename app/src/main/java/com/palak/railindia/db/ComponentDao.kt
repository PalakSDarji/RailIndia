package com.palak.railindia.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.palak.railindia.model.Component
import kotlinx.coroutines.flow.Flow

/**
 * Handler method for db operations.
 */
@Dao
interface ComponentDao {

    //No need to use suspend, Reason: LiveData will update in async manner.
    @Query("SELECT * from component")
    fun fetchAllComponent() : Flow<List<Component>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component : Component) : Long
/*
    @Query("DELETE FROM cityLocation WHERE id = :id")
    suspend fun deleteCityLocation(id: Int)

    @Query("DELETE FROM cityLocation")
    suspend fun deleteAllCityLocations()*/
}