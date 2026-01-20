package com.fanda.homebook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fanda.homebook.data.closet.ClosetDao
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.data.color.ColorTypeDao
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.owner.OwnerDao
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.product.ProductDao
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.SeasonDao
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeDao
import com.fanda.homebook.data.size.SizeEntity

@Database(entities = [ColorTypeEntity::class, ClosetEntity::class, SeasonEntity::class, ProductEntity::class, SizeEntity::class, OwnerEntity::class], version = 6, exportSchema = false) abstract class HomeBookDatabase() : RoomDatabase() {

    abstract fun colorTypeDao(): ColorTypeDao

    abstract fun closetDao(): ClosetDao

    abstract fun seasonDao(): SeasonDao

    abstract fun productDao(): ProductDao

    abstract fun sizeDao(): SizeDao

    abstract fun ownerDao(): OwnerDao

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