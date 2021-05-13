package com.palak.railindia.model

import androidx.room.*

@Entity(
    tableName = "component_entry", foreignKeys = [
        ForeignKey(entity = Entry::class, parentColumns = ["id"], childColumns = ["entryId"]),
        ForeignKey(entity = Component::class, parentColumns = ["id"], childColumns = ["componentId"]),
    ]
)
data class ComponentEntry(
    @PrimaryKey
    var id : String = "",
    var entryId: String = "",
    var componentId: Int = 0,
    var pass: Int = 0,
    var fail: Int = 0,
    @Ignore
    var component: Component? = null

)
