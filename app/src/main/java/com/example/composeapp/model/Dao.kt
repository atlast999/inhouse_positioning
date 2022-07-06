package com.example.composeapp.model

import androidx.room.*

@Dao
interface ApDao {

    @Query("SELECT * FROM ap_info")
    suspend fun getAll(): List<ApPositionInfo>

    @Query("SELECT * FROM ap_info WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): ApPositionInfo

    @Insert
    suspend fun insertAll(vararg aps: ApPositionInfo)

    @Update
    suspend fun updateAp(vararg aps: ApPositionInfo)

    @Delete
    suspend fun delete(ap: ApPositionInfo)
}