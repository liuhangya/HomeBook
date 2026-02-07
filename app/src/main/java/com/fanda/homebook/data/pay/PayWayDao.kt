package com.fanda.homebook.data.pay

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
 * 付款方式数据访问对象 (Data Access Object)
 * 定义对PayWayEntity表的所有数据库操作
 */
@Dao interface PayWayDao {
    // 获取付款方式数据总数
    @Query("SELECT COUNT(*) FROM pay_way") suspend fun getCount(): Int

    // 批量插入付款方式
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<PayWayEntity>)

    // 单条插入付款方式，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: PayWayEntity): Long

    // 更新付款方式
    @Update suspend fun update(entity: PayWayEntity): Int

    // 删除付款方式
    @Delete suspend fun delete(entity: PayWayEntity): Int

    // 根据ID删除付款方式
    @Query("DELETE FROM pay_way WHERE id = :id") suspend fun deleteById(id: Int): Int

    // Flow版本查询，支持实时更新，按排序字段升序排列
    @Query("SELECT * FROM pay_way ORDER BY sortOrder ASC") fun getItems(): Flow<List<PayWayEntity>>

    // 根据ID查询单个付款方式（Flow版本）
    @Query("SELECT * FROM pay_way WHERE id = :id") fun getItemById(id: Int): Flow<PayWayEntity>

    // 根据名称查询付款方式（挂起函数版本）
    @Query("SELECT * FROM pay_way WHERE name = :name") suspend fun getItemByName(name: String): PayWayEntity?

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM pay_way") suspend fun getMaxSortOrder(): Int?

    // 批量更新付款方式排序字段
    @Update suspend fun updateSortOrders(list: List<PayWayEntity>): Int

    /**
     * 插入付款方式时自动设置排序（放在最后）
     * 使用@Transaction确保获取最大排序值和插入操作的原子性
     *
     * @param entity 要插入的付款方式实体
     * @return 新插入记录的ID
     */
    @Transaction suspend fun insertWithAutoOrder(entity: PayWayEntity): Long {
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
     * 重置为默认付款方式数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param list 默认付款方式数据列表
     */
    @Transaction suspend fun resetToDefault(list: List<PayWayEntity>) {
        // 先清空表（通过插入操作自动覆盖，因为使用REPLACE策略）
        insertAll(list)
    }
}