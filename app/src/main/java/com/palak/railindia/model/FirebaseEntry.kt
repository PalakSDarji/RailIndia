package com.palak.railindia.model

data class FirebaseEntry(
    val id : String = "",
    val date : String = "",
    val month : String = "",
    val totalQty : Int = 0
)

data class FirebaseComponentEntry(
    val id: String = "",
    val pass: Int = 0,
    val fail: Int = 0,
    var componentName: String = "",
    var componentId: Int = 0
)