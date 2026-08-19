package com.example.dashboardapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dashboardapp.data.local.dao.UserDao
import com.example.dashboardapp.data.local.entity.UserEntity

@Database(entities = [UserEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}