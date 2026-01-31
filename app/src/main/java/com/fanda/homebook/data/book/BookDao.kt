package com.fanda.homebook.data.book

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // 获取数据总数
    @Query("SELECT COUNT(*) FROM book") suspend fun getCount(): Int

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<BookEntity>)

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: BookEntity): Long

    @Update suspend fun update(entity: BookEntity): Int

    @Delete suspend fun delete(entity: BookEntity): Int

    @Query("DELETE FROM book WHERE id = :id") suspend fun deleteById(id: Int): Int

    // Flow 版本，支持实时更新，按排序字段升序
    @Query("SELECT * FROM book ORDER BY sortOrder ASC") fun getItems(): Flow<List<BookEntity>>

    @Query("SELECT * FROM book WHERE id = :id") fun getItemById(id: Int): Flow<BookEntity>

    @Query("SELECT * FROM book WHERE name = :name") suspend fun getItemByName(name: String): BookEntity?

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM book") suspend fun getMaxSortOrder(): Int?

    // 批量更新排序
    @Update suspend fun updateSortOrders(list: List<BookEntity>): Int

    // 插入时自动设置排序（放在最后）
    @Transaction suspend fun insertWithAutoOrder(entity: BookEntity): Long {
        val maxOrder = getMaxSortOrder() ?: 0
        LogUtils.i("最大排序值为：$maxOrder")
        val insertEntity = entity.copy(sortOrder = maxOrder + 1)
        LogUtils.i("插入数据：$insertEntity")
        return insert(insertEntity)
    }

    // 重置为默认数据
    @Transaction suspend fun resetToDefault(list: List<BookEntity>) {
        insertAll(list)
    }
}