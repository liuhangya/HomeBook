package com.fanda.homebook.data.season

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao interface SeasonDao {
    @Query("SELECT * FROM season") suspend fun getSeasonTypes(): List<SeasonEntity>

    @Query("SELECT COUNT(*) FROM season") suspend fun getCount(): Int

    @Query("SELECT * FROM season WHERE id = :id") fun getSeasonTypeById(id: Int): Flow<SeasonEntity?>

    @Query("SELECT * FROM season WHERE id IN (:ids)") fun getSeasonsByIdsFlow(ids: List<Int>): Flow<List<SeasonEntity>>

    @Transaction suspend fun resetToDefault(seasonTypes: List<SeasonEntity>) {
        insertAll(seasonTypes)
    }

    // 根据 closetId 删除所有关联
    @Query("DELETE FROM closet_season_relation WHERE closetId = :closetId") suspend fun deleteByClosetId(closetId: Int): Int

    /**
     * 根据 closetId 获取关联的季节ID列表
     * @param closetId 衣柜ID
     * @return 季节ID列表
     */
    @Query("SELECT seasonId FROM closet_season_relation WHERE closetId = :closetId")
    suspend fun getSeasonIdsByClosetId(closetId: Int): List<Int>

    // 事务：更新某个衣柜的季节关联
    @Transaction suspend fun updateSeasonsForCloset(closetId: Int, seasonIds: List<Int>) {
        // 1. 删除旧的关联
        deleteByClosetId(closetId)

        // 2. 创建新的关联
        if (seasonIds.isNotEmpty()) {
            val newRelations = seasonIds.map { seasonId ->
                ClosetSeasonRelation(closetId = closetId, seasonId = seasonId)
            }
            insertSeasonRelationAll(newRelations)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(seasonTypes: List<SeasonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSeasonRelationAll(entities: List<ClosetSeasonRelation>)
}