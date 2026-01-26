package com.fanda.homebook.data.stock

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface StockDao {

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StockEntity): Long

}