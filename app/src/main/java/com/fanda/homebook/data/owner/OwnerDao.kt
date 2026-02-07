package com.fanda.homebook.data.owner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 所有者数据访问对象 (Data Access Object)
 * 定义对OwnerEntity表的所有数据库操作
 */
@Dao interface OwnerDao {
    /**
     * 获取所有所有者列表（挂起函数版本）
     *
     * @return 所有者实体列表
     */
    @Query("SELECT * FROM owner") suspend fun getItems(): List<OwnerEntity>

    /**
     * 获取所有者数据总数
     *
     * @return 所有者总数
     */
    @Query("SELECT COUNT(*) FROM owner") suspend fun getCount(): Int

    /**
     * 根据ID获取所有者（Flow版本，支持实时更新）
     *
     * @param id 所有者ID
     * @return Flow流，包含对应的所有者实体（可为空）
     */
    @Query("SELECT * FROM owner WHERE id = :id") fun getItemById(id: Int): Flow<OwnerEntity?>

    /**
     * 获取当前选中的所有者（Flow版本，支持实时更新）
     * 用于获取应用当前活跃用户/所有者的信息
     *
     * @return Flow流，包含当前选中的所有者实体（可为空）
     */
    @Query("SELECT * FROM owner WHERE selected = 1") fun getSelectedItem(): Flow<OwnerEntity?>

    /**
     * 重置为默认所有者数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param list 默认所有者数据列表
     */
    @Transaction suspend fun resetToDefault(list: List<OwnerEntity>) {
        insertAll(list)
    }

    /**
     * 批量插入所有者数据，在发生冲突时覆盖之前的数据
     *
     * @param list 所有者实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<OwnerEntity>)

    /**
     * 批量更新所有者数据
     *
     * @param list 所有者实体列表
     * @return 受影响的行数
     */
    @Update suspend fun updateItems(list: List<OwnerEntity>): Int

    /**
     * 更新单个所有者数据
     *
     * @param item 所有者实体
     * @return 受影响的行数
     */
    @Update suspend fun updateItem(item: OwnerEntity): Int
}