package com.palak.railindia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "component")
data class Component constructor(
    @PrimaryKey
    var id: Int = 0,
    var name: String = "",
    var qty: Int = 0
)


