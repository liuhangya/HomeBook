package com.fanda.homebook.data.product

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
 * 产品数据访问对象 (Data Access Object)
 * 定义对ProductEntity表的所有数据库操作
 */
@Dao interface ProductDao {
    // 获取产品数据总数
    @Query("SELECT COUNT(*) FROM product") suspend fun getCount(): Int

    // 批量插入产品
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<ProductEntity>)

    // 单条插入产品，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: ProductEntity): Long

    // 更新产品
    @Update suspend fun update(entity: ProductEntity): Int

    // 删除产品
    @Delete suspend fun delete(entity: ProductEntity): Int

    // 根据ID删除产品
    @Query("DELETE FROM product WHERE id = :id") suspend fun deleteById(id: Int): Int

    // Flow版本查询，支持实时更新，按排序字段升序排列
    @Query("SELECT * FROM product ORDER BY sortOrder ASC") fun getItems(): Flow<List<ProductEntity>>

    // 根据ID查询单个产品（Flow版本）
    @Query("SELECT * FROM product WHERE id = :id") fun getItemById(id: Int): Flow<ProductEntity>

    // 根据名称查询产品（挂起函数版本）
    @Query("SELECT * FROM product WHERE name = :name") suspend fun getItemByName(name: String): ProductEntity?

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM product") suspend fun getMaxSortOrder(): Int?

    // 批量更新产品排序字段
    @Update suspend fun updateSortOrders(list: List<ProductEntity>): Int

    /**
     * 插入产品时自动设置排序（放在最后）
     * 使用@Transaction确保获取最大排序值和插入操作的原子性
     *
     * @param entity 要插入的产品实体
     * @return 新插入记录的ID
     */
    @Transaction suspend fun insertWithAutoOrder(entity: ProductEntity): Long {
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
     * 重置为默认产品数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param list 默认产品数据列表
     */
    @Transaction suspend fun resetToDefault(list: List<ProductEntity>) {
        // 先清空表（通过插入操作自动覆盖，因为使用REPLACE策略）
        insertAll(list)
    }
}