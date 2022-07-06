package com.example.composeapp.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccessPoint(
    val uid: String,
    val name: String,
    var rssi: Int,
) : Parcelable

@Parcelize
@Entity(tableName = "ap_info")
data class ApPositionInfo(
    @PrimaryKey @ColumnInfo(name = "uid") val uid: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "ro") var ro: Int,
    @ColumnInfo(name = "pro") var pro: Int,
    @ColumnInfo(name = "xAxis") var xAxis: Double,
    @ColumnInfo(name = "yAxis") var yAxis: Double,
    @ColumnInfo(name = "zAxis") var zAxis: Double,
) : Parcelable