package com.example.composeapp.app

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.composeapp.model.ApDao
import com.example.composeapp.model.ApPositionInfo
import com.example.composeapp.model.PositionSample
import com.example.composeapp.model.SampleDao

@Database(entities = [ApPositionInfo::class, PositionSample::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apDao(): ApDao
    abstract fun sampleDao(): SampleDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "ap_database")
                    .build().also { instance = it }
            }
        }
    }
}

class App : Application()