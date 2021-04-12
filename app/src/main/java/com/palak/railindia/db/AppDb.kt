package com.palak.railindia.db

import android.content.Context
import androidx.room.*
import com.palak.railindia.model.Component
import com.palak.railindia.model.ComponentEntry
import com.palak.railindia.model.Entry
import com.palak.railindia.utils.Converters

@Database(
    entities = [Component::class, Entry::class, ComponentEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {

    abstract fun componentDao(): ComponentDao
    abstract fun entryDao(): EntryDao

    companion object {

        @Volatile
        private var INSTANCE: AppDb? = null

        fun getDb(context: Context): AppDb {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    "rail_india_db"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }

}