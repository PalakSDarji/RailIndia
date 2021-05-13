package com.palak.railindia.utils

import com.palak.railindia.model.ComponentEntry
import com.palak.railindia.model.Entry
import com.palak.railindia.model.FirebaseComponentEntry
import com.palak.railindia.model.FirebaseEntry
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {

        fun convertFirebaseEntryToEntry(
            firebaseEntry: FirebaseEntry,
            listOfFirebaseComponentEntry: MutableList<FirebaseComponentEntry>,
            dateFormat: SimpleDateFormat
        ): Entry {

            val entry = Entry()
            entry.id = UUID.randomUUID().toString()
            val date = Date(dateFormat.parse(firebaseEntry.date).time)
            entry.date = date
            entry.qty = firebaseEntry.totalQty
            entry.synced = true

            var listOfComponentEntry = mutableListOf<ComponentEntry>()

            listOfFirebaseComponentEntry.forEach {
                val componentEntry = convertFirebaseComponentEntryToComponentEntry(it)
                componentEntry.entryId = entry.id
                listOfComponentEntry.add(componentEntry)
            }
            entry.componentEntry = listOfComponentEntry

            return entry
        }

        private fun convertFirebaseComponentEntryToComponentEntry(
            firebaseComponentEntry: FirebaseComponentEntry): ComponentEntry {

            val componentEntry = ComponentEntry()
            componentEntry.id = firebaseComponentEntry.id
            componentEntry.pass = firebaseComponentEntry.pass
            componentEntry.fail = firebaseComponentEntry.fail
            componentEntry.componentId = firebaseComponentEntry.componentId

            return componentEntry
        }
    }

}