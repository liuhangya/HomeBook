package com.fanda.homebook.data.period

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * 使用期限数据访问对象 (Data Access Object)
 * 定义对PeriodEntity表的所有数据库操作
 * 用于管理物品的使用期限类型，如"开封后"、"6个月"、"12个月"等
 */
@Dao interface PeriodDao {
    /**
     * 获取所有使用期限类型（挂起函数版本）
     *
     * @return 使用期限实体列表
     */
    @Query("SELECT * FROM period") suspend fun getTypes(): List<PeriodEntity>

    /**
     * 获取使用期限类型总数
     *
     * @return 使用期限类型总数
     */
    @Query("SELECT COUNT(*) FROM period") suspend fun getCount(): Int

    /**
     * 根据ID获取使用期限类型（Flow版本，支持实时更新）
     *
     * @param id 使用期限类型ID
     * @return Flow流，包含对应的使用期限实体（可为空）
     */
    @Query("SELECT * FROM period WHERE id = :id") fun getTypeById(id: Int): Flow<PeriodEntity?>

    /**
     * 重置为默认使用期限类型数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param types 默认使用期限类型数据列表
     */
    @Transaction suspend fun resetToDefault(types: List<PeriodEntity>) {
        insertAll(types)
    }

    /**
     * 批量插入使用期限类型数据，在发生冲突时覆盖之前的数据
     *
     * @param types 使用期限实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(types: List<PeriodEntity>)
}