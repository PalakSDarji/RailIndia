package com.palak.railindia.repo

import androidx.lifecycle.LiveData
import com.palak.railindia.db.ComponentDao
import com.palak.railindia.model.Component
import javax.inject.Inject

class ComponentRepo @Inject constructor(val componentDao: ComponentDao) : Repo<Component> {

    override fun fetchAllFromDb(): LiveData<List<Component>> {
        return componentDao.fetchAllComponent()
    }

    override suspend fun insertIntoDb(component : Component): Long {
        return componentDao.insertComponent(component)
    }
}