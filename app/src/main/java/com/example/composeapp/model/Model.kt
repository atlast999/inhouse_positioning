package com.example.composeapp.model

data class AccessPoint(
    val name: String,
    val mac: String,
    var ro: Int = 0,
    var pro: Int = 0,
    var rssi: Int = 0,
    var xAxis: Double = 0.0,
    var yAxis: Double = 0.0,
    var zAxis: Double = 0.0,
)