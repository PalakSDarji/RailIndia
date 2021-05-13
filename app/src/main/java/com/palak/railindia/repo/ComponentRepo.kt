package com.palak.railindia.repo

import androidx.lifecycle.LiveData
import com.palak.railindia.db.ComponentDao
import com.palak.railindia.model.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ComponentRepo @Inject constructor(val componentDao: ComponentDao) : Repo<Component> {

    override fun fetchAllFromDb(): Flow<List<Component>> {
        return componentDao.fetchAllComponent()
    }

    override suspend fun insertIntoDb(component : Component): Long {
        return withContext(Dispatchers.IO){
            componentDao.insertComponent(component)
        }
    }

    override suspend fun updateIntoDb(t: Component) {
        //No need.
    }

    suspend fun getComponentFromId(id : Int) : Component {
        return withContext(Dispatchers.IO){
            componentDao.getComponentFromId(id)
        }
    }
}