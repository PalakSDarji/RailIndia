package com.palak.railindia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "component")
data class Component constructor(
    @PrimaryKey
    val id : Int = 0,
    val name : String = "",
    val qty : Int = 0)


