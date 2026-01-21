package com.fanda.homebook.data.closet

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao interface ClosetDao {

    // 获取数据总数
    @Query("SELECT COUNT(*) FROM closet") suspend fun getCount(): Int

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: ClosetEntity): Long

    @Update suspend fun update(entity: ClosetEntity): Int

    @Delete suspend fun delete(entity: ClosetEntity): Int

    @Query("DELETE FROM closet WHERE id = :id") suspend fun deleteById(id: Int): Int

    @Transaction @Query("SELECT * FROM closet WHERE ownerId = :ownerId") fun getClosets(ownerId: Int): Flow<List<AddClosetEntity>>

    // 关联查询对象
    @Transaction @Query("SELECT * FROM closet WHERE id = :id") fun getClosetById(id: Int): Flow<AddClosetEntity>

}