package com.fanda.homebook.data.owner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.owner.OwnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owner")
    suspend fun getItems(): List<OwnerEntity>

    @Query("SELECT COUNT(*) FROM owner")
    suspend fun getCount(): Int

    @Query("SELECT * FROM owner WHERE id = :id")
    fun getItemById(id: Int): Flow<OwnerEntity?>

    @Query("SELECT * FROM owner WHERE selected = 1")
    fun getSelectedItem(): Flow<OwnerEntity?>

    @Transaction
    suspend fun resetToDefault(list: List<OwnerEntity>) {
        insertAll(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<OwnerEntity>)

    // 批量更新排序
    @Update
    suspend fun updateItems(list: List<OwnerEntity>): Int

    @Update
    suspend fun updateItem(item: OwnerEntity): Int
}