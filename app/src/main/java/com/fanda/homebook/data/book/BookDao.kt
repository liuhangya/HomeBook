package com.fanda.homebook.data.book

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
 * 账本数据访问对象 (Data Access Object)
 * 定义对BookEntity表的所有数据库操作
 */
@Dao interface BookDao {

    // 获取数据总数
    @Query("SELECT COUNT(*) FROM book") suspend fun getCount(): Int

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<BookEntity>)

    // 单条插入，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: BookEntity): Long

    // 更新数据
    @Update suspend fun update(entity: BookEntity): Int

    // 删除数据
    @Delete suspend fun delete(entity: BookEntity): Int

    // 根据ID删除数据
    @Query("DELETE FROM book WHERE id = :id") suspend fun deleteById(id: Int): Int

    // Flow版本查询，支持实时更新，按排序字段升序排列
    @Query("SELECT * FROM book ORDER BY sortOrder ASC") fun getItems(): Flow<List<BookEntity>>

    // 根据ID查询单个账本（Flow版本）
    @Query("SELECT * FROM book WHERE id = :id") fun getItemById(id: Int): Flow<BookEntity>

    // 根据名称查询账本（挂起函数版本）
    @Query("SELECT * FROM book WHERE name = :name") suspend fun getItemByName(name: String): BookEntity?

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM book") suspend fun getMaxSortOrder(): Int?

    // 批量更新排序字段
    @Update suspend fun updateSortOrders(list: List<BookEntity>): Int

    // 插入时自动设置排序（放在最后）
    @Transaction suspend fun insertWithAutoOrder(entity: BookEntity): Long {
        // 获取当前最大排序值
        val maxOrder = getMaxSortOrder() ?: 0
        LogUtils.i("最大排序值为：$maxOrder")

        // 创建新实体，设置排序值为最大值+1（即放在最后）
        val insertEntity = entity.copy(sortOrder = maxOrder + 1)
        LogUtils.i("插入数据：$insertEntity")

        // 执行插入操作
        return insert(insertEntity)
    }

    // 重置为默认数据（清空表并插入默认数据列表）
    @Transaction suspend fun resetToDefault(list: List<BookEntity>) {
        // 先清空表（通过插入操作自动覆盖，因为使用REPLACE策略）
        insertAll(list)
    }
}