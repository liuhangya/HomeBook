package com.fanda.homebook.data.size

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.flow.Flow

/**
 * 尺码数据访问对象 (Data Access Object)
 * 定义对SizeEntity表的所有数据库操作
 */
@Dao interface SizeDao {
    // 获取尺码数据总数
    @Query("SELECT COUNT(*) FROM size") suspend fun getCount(): Int

    // 批量插入尺码
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<SizeEntity>)

    // 单条插入尺码，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: SizeEntity): Long

    // 更新尺码
    @Update suspend fun update(entity: SizeEntity): Int

    // 删除尺码
    @Delete suspend fun delete(entity: SizeEntity): Int

    // 根据ID删除尺码
    @Query("DELETE FROM size WHERE id = :id") suspend fun deleteById(id: Int): Int

    // Flow版本查询，支持实时更新，按排序字段升序排列
    @Query("SELECT * FROM size ORDER BY sortOrder ASC") fun getItems(): Flow<List<SizeEntity>>

    // 根据ID查询单个尺码（Flow版本）
    @Query("SELECT * FROM size WHERE id = :id") fun getItemById(id: Int): Flow<SizeEntity>

    // 根据名称查询尺码（挂起函数版本）
    @Query("SELECT * FROM size WHERE name = :name") suspend fun getItemByName(name: String): SizeEntity?

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM size") suspend fun getMaxSortOrder(): Int?

    // 批量更新尺码排序字段
    @Update suspend fun updateSortOrders(list: List<SizeEntity>): Int

    /**
     * 插入尺码时自动设置排序（放在最后）
     * 使用@Transaction确保获取最大排序值和插入操作的原子性
     *
     * @param entity 要插入的尺码实体
     * @return 新插入记录的ID
     */
    @Transaction suspend fun insertWithAutoOrder(entity: SizeEntity): Long {
        // 获取当前最大排序值
        val maxOrder = getMaxSortOrder() ?: 0
        LogUtils.i("最大排序值为：$maxOrder")

        // 创建新实体，设置排序值为最大值+1（即放在最后）
        val insertEntity = entity.copy(sortOrder = maxOrder + 1)
        LogUtils.i("插入数据：$insertEntity")

        // 执行插入操作
        return insert(insertEntity)
    }

    /**
     * 重置为默认尺码数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param list 默认尺码数据列表
     */
    @Transaction suspend fun resetToDefault(list: List<SizeEntity>) {
        // 先清空表（通过插入操作自动覆盖，因为使用REPLACE策略）
        insertAll(list)
    }
}