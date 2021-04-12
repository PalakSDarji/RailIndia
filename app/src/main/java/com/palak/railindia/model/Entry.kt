package com.palak.railindia.model

import androidx.room.*
import com.palak.railindia.utils.Converters
import java.sql.Date

@Entity(tableName = "entry")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @TypeConverters(Converters::class)
    var date: Date? = null,
    @Ignore
    var componentEntry: List<ComponentEntry>? = null
)