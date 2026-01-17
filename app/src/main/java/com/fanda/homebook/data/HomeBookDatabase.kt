package com.fanda.homebook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fanda.homebook.data.closet.ClosetDao
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.data.color.ColorTypeDao
import com.fanda.homebook.data.color.ColorTypeEntity

@Database(entities = [ColorTypeEntity::class , ClosetEntity::class], version = 2, exportSchema = false) abstract class HomeBookDatabase() : RoomDatabase() {

    abstract fun colorTypeDao(): ColorTypeDao

    abstract fun closetDao(): ClosetDao

    companion object {
        @Volatile private var Instance: HomeBookDatabase? = null

        fun getDatabase(context: Context): HomeBookDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext, HomeBookDatabase::class.java, "HomeBookDatabase"
                ).fallbackToDestructiveMigration().build() // 允许销毁并重建数据库
                    .also { Instance = it }
            }
        }
    }
}