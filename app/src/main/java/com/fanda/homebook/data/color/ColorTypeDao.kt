package com.fanda.homebook.data.color

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao interface ColorTypeDao {

    // 获取数据总数
    @Query("SELECT COUNT(*) FROM color_type") suspend fun getCount(): Int

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(colorTypes: List<ColorTypeEntity>)

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(colorType: ColorTypeEntity): Long

    @Update suspend fun update(colorType: ColorTypeEntity): Int

    @Delete suspend fun delete(colorType: ColorTypeEntity): Int

    @Query("DELETE FROM color_type WHERE id = :id") suspend fun deleteById(id: Int): Int

    // Flow 版本，支持实时更新，按排序字段升序
    @Query("SELECT * FROM color_type ORDER BY sortOrder ASC") fun getColorTypes(): Flow<List<ColorTypeEntity>>

    @Query("SELECT * FROM color_type WHERE id = :id") fun getColorTypeById(id: Int): Flow<ColorTypeEntity>

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM color_type") suspend fun getMaxSortOrder(): Int?

    // 批量更新排序
    @Update suspend fun updateSortOrders(colorTypes: List<ColorTypeEntity>): Int

    // 插入时自动设置排序（放在最后）
    @Transaction suspend fun insertWithAutoOrder(colorType: ColorTypeEntity): Long {
        val maxOrder = getMaxSortOrder() ?: 0
        colorType.copy(sortOrder = maxOrder + 1)
        return insert(colorType)
    }

    // 重置为默认数据
    @Transaction suspend fun resetToDefault(colorTypes: List<ColorTypeEntity>) {
        insertAll(colorTypes)
    }
}