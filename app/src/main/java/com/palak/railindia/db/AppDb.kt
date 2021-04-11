package com.palak.railindia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.palak.railindia.model.Component

@Database(entities = [Component::class],
            version = 1,
            exportSchema = false)
abstract class AppDb : RoomDatabase(){

    abstract fun componentDao() : ComponentDao

    companion object {

        @Volatile
        private var INSTANCE: AppDb? = null

        fun getDb(context: Context): AppDb {

            val tempInstance =
                INSTANCE
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