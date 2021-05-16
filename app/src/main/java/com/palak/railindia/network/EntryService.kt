package com.palak.railindia.network

import com.palak.railindia.model.Entry
import kotlinx.coroutines.flow.Flow

interface EntryService {

    fun searchEntryByDate(date : String) : Flow<Result<Entry>>

    fun searchEntryForMonth(month : String) : Flow<Result<List<Entry>>>
}