package com.fanda.homebook.data.quick

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao interface QuickDao {
    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: QuickEntity): Long

    @Delete suspend fun delete(entity: QuickEntity): Int

    @Update suspend fun update(entity: QuickEntity): Int

    @Query("DELETE FROM quick WHERE id = :id") suspend fun deleteById(id: Int): Int

    // 关联查询对象
    @Transaction @Query("SELECT * FROM quick WHERE  id = :id") suspend fun getQuickById(id: Int): AddQuickEntity

}