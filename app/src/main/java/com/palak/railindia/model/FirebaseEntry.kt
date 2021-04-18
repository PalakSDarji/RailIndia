package com.palak.railindia.model

import java.sql.Date


data class FirebaseEntry(
    val id : Int,
    val date : String
)

data class FirebaseComponentEntry(
    val id: Int,
    val pass: Int = 0,
    val fail: Int = 0,
    var componentName: String = ""
)