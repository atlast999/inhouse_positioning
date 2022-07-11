package com.example.composeapp.model

import androidx.room.*

@Dao
interface ApDao {

    @Query("SELECT * FROM ap_info")
    suspend fun getAll(): List<ApPositionInfo>

    @Query("SELECT * FROM ap_info WHERE uid IN (:uid)")
    suspend fun getByUid(uid: List<String>): List<ApPositionInfo>

    @Insert
    suspend fun insertAll(vararg aps: ApPositionInfo)

    @Update
    suspend fun updateAp(vararg aps: ApPositionInfo)

    @Delete
    suspend fun delete(ap: ApPositionInfo)
}

@Dao
interface SampleDao {

    @Query("SELECT * FROM samples")
    suspend fun getAll(): List<PositionSample>

    @Insert
    suspend fun insertAll(vararg sample: PositionSample)

    @Delete
    suspend fun delete(sample: PositionSample)
}