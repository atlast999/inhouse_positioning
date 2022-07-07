package com.example.composeapp.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
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
) : Parcelable {

    @IgnoredOnParcel
    var rssi: Int = 0
}

@Entity(tableName = "samples")
data class PositionSample(
    @PrimaryKey @ColumnInfo(name = "uid") val uid: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "ap_uid") val apIds: String,
    @ColumnInfo(name = "ap_name") val apNames: String,
    @ColumnInfo(name = "ap_power") val apPowers: String,
) {
    fun listAp(): List<AccessPoint> {
        val ids = apIds.split(DELIMITER)
        val names = apNames.split(DELIMITER).toTypedArray()
        val powers = apPowers.split(DELIMITER).toTypedArray()
        return ids.mapIndexed { index, id ->
            AccessPoint(
                uid = id,
                name = names[index],
                rssi = powers[index].toInt()
            )
        }
    }

    companion object {
        const val DELIMITER = "-"
        fun createSample(uid: Long, sampleName: String, aps: List<AccessPoint>): PositionSample {
            val ids = aps.joinToString(DELIMITER) { it.uid }
            val names = aps.joinToString(DELIMITER) { it.name }
            val powers = aps.joinToString(DELIMITER) { it.rssi.toString() }
            return PositionSample(
                uid = uid,
                name = sampleName,
                apIds = ids,
                apNames = names,
                apPowers = powers
            )
        }
    }
}