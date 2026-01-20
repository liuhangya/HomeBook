package com.fanda.homebook.data.owner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.fanda.homebook.data.owner.OwnerEntity
import kotlinx.coroutines.flow.Flow

@Dao interface OwnerDao {
    @Query("SELECT * FROM owner") suspend fun getItems(): List<OwnerEntity>

    @Query("SELECT COUNT(*) FROM owner") suspend fun getCount(): Int

    @Query("SELECT * FROM owner WHERE id = :id") fun getItemById(id: Int): Flow<OwnerEntity?>

    @Transaction suspend fun resetToDefault(list: List<OwnerEntity>) {
        insertAll(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<OwnerEntity>)
}