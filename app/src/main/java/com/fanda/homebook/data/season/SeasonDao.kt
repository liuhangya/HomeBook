package com.fanda.homebook.data.season

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * 季节数据访问对象 (Data Access Object)
 * 定义对SeasonEntity和ClosetSeasonRelation表的所有数据库操作
 */
@Dao interface SeasonDao {
    /**
     * 获取所有季节类型（挂起函数版本）
     *
     * @return 季节实体列表
     */
    @Query("SELECT * FROM season") suspend fun getSeasonTypes(): List<SeasonEntity>

    /**
     * 获取季节类型总数
     *
     * @return 季节类型总数
     */
    @Query("SELECT COUNT(*) FROM season") suspend fun getCount(): Int

    /**
     * 根据ID获取季节类型（Flow版本，支持实时更新）
     *
     * @param id 季节类型ID
     * @return Flow流，包含对应的季节实体（可为空）
     */
    @Query("SELECT * FROM season WHERE id = :id") fun getSeasonTypeById(id: Int): Flow<SeasonEntity?>

    /**
     * 根据ID列表获取季节类型（Flow版本，支持实时更新）
     *
     * @param ids 季节类型ID列表
     * @return Flow流，包含对应的季节实体列表
     */
    @Query("SELECT * FROM season WHERE id IN (:ids)") fun getSeasonsByIdsFlow(ids: List<Int>): Flow<List<SeasonEntity>>

    /**
     * 重置为默认季节类型数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param seasonTypes 默认季节类型数据列表
     */
    @Transaction suspend fun resetToDefault(seasonTypes: List<SeasonEntity>) {
        insertAll(seasonTypes)
    }

    /**
     * 根据衣柜ID删除所有季节关联
     *
     * @param closetId 衣柜ID
     * @return 删除的行数
     */
    @Query("DELETE FROM closet_season_relation WHERE closetId = :closetId") suspend fun deleteByClosetId(closetId: Int): Int

    /**
     * 根据衣柜ID获取关联的季节ID列表
     *
     * @param closetId 衣柜ID
     * @return 季节ID列表
     */
    @Query("SELECT seasonId FROM closet_season_relation WHERE closetId = :closetId") suspend fun getSeasonIdsByClosetId(closetId: Int): List<Int>

    /**
     * 事务：更新某个衣柜的季节关联
     * 使用@Transaction确保删除旧关联和插入新关联的原子性
     *
     * @param closetId 衣柜ID
     * @param seasonIds 新的季节ID列表
     */
    @Transaction suspend fun updateSeasonsForCloset(closetId: Int, seasonIds: List<Int>) {
        // 1. 删除旧的关联
        deleteByClosetId(closetId)

        // 2. 创建新的关联（如果新列表不为空）
        if (seasonIds.isNotEmpty()) {
            val newRelations = seasonIds.map { seasonId ->
                ClosetSeasonRelation(closetId = closetId, seasonId = seasonId)
            }
            insertSeasonRelationAll(newRelations)
        }
    }

    /**
     * 批量插入季节类型数据，在发生冲突时覆盖之前的数据
     *
     * @param seasonTypes 季节实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(seasonTypes: List<SeasonEntity>)

    /**
     * 批量插入衣柜季节关联数据，在发生冲突时覆盖之前的数据
     *
     * @param entities 衣柜季节关联实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSeasonRelationAll(entities: List<ClosetSeasonRelation>)
}