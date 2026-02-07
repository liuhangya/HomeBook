package com.fanda.homebook.data.color

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
 * 颜色类型数据访问对象 (Data Access Object)
 * 定义对ColorTypeEntity表的所有数据库操作
 */
@Dao interface ColorTypeDao {

    // 获取颜色类型数据总数
    @Query("SELECT COUNT(*) FROM color") suspend fun getCount(): Int

    // 批量插入颜色类型
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<ColorTypeEntity>)

    // 单条插入颜色类型，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: ColorTypeEntity): Long

    // 更新颜色类型
    @Update suspend fun update(entity: ColorTypeEntity): Int

    // 删除颜色类型
    @Delete suspend fun delete(entity: ColorTypeEntity): Int

    // 根据ID删除颜色类型
    @Query("DELETE FROM color WHERE id = :id") suspend fun deleteById(id: Int): Int

    // Flow版本查询，支持实时更新，按排序字段升序排列
    @Query("SELECT * FROM color ORDER BY sortOrder ASC") fun getItems(): Flow<List<ColorTypeEntity>>

    // 根据ID查询单个颜色类型（Flow版本）
    @Query("SELECT * FROM color WHERE id = :id") fun getItemById(id: Int): Flow<ColorTypeEntity>

    // 根据名称查询颜色类型（挂起函数版本）
    @Query("SELECT * FROM color WHERE name = :name") suspend fun getItemByName(name: String): ColorTypeEntity?

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM color") suspend fun getMaxSortOrder(): Int?

    // 批量更新颜色类型排序字段
    @Update suspend fun updateSortOrders(list: List<ColorTypeEntity>): Int

    /**
     * 插入颜色类型时自动设置排序（放在最后）
     * 使用@Transaction确保获取最大排序值和插入操作的原子性
     *
     * @param entity 要插入的颜色类型实体
     * @return 新插入记录的ID
     */
    @Transaction suspend fun insertWithAutoOrder(entity: ColorTypeEntity): Long {
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
     * 重置为默认颜色类型数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param list 默认颜色类型数据列表
     */
    @Transaction suspend fun resetToDefault(list: List<ColorTypeEntity>) {
        // 先清空表（通过插入操作自动覆盖，因为使用REPLACE策略）
        insertAll(list)
    }
}