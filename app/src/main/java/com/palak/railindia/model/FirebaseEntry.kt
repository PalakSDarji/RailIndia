package com.palak.railindia.model

data class FirebaseEntry(
    val id : Int,
    val date : String,
    val totalQty : Int
)

data class FirebaseComponentEntry(
    val id: Int,
    val pass: Int = 0,
    val fail: Int = 0,
    var componentName: String = ""
)