package com.example.majiraapp.ui.data

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    var temperature: Int = 0,
    var time: String = "",
    var isSelected: Boolean = false,
    var isSaved: Boolean = false
)