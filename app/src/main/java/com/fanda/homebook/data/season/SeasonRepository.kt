package com.fanda.homebook.data.season

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.fanda.homebook.data.closet.ClosetEntity
import kotlinx.coroutines.flow.Flow

interface SeasonRepository {
    suspend fun initializeDatabase()

    suspend fun getSeasons(): List<SeasonEntity>

    suspend fun getCount(): Int

    fun getSeasonById(id: Int): Flow<SeasonEntity?>

    fun getSeasonsByIdsFlow(ids: List<Int>): Flow<List<SeasonEntity>>

    suspend fun insertSeasonRelationAll(entities: List<ClosetSeasonRelation>)

    suspend fun updateSeasonsForCloset(closetId: Int, seasonIds: List<Int>)

    suspend fun getSeasonIdsByClosetId(closetId: Int): List<Int>

}

class LocalSeasonRepository(private val seasonDao: SeasonDao) : SeasonRepository {
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            seasonDao.resetToDefault(defaultSeasonData)
        }
    }

    override suspend fun getSeasons(): List<SeasonEntity> = seasonDao.getSeasonTypes()

    override suspend fun getCount() = seasonDao.getCount()

    override fun getSeasonById(id: Int) = seasonDao.getSeasonTypeById(id)
    override fun getSeasonsByIdsFlow(ids: List<Int>): Flow<List<SeasonEntity>> = seasonDao.getSeasonsByIdsFlow(ids)
    override suspend fun insertSeasonRelationAll(entities: List<ClosetSeasonRelation>) = seasonDao.insertSeasonRelationAll(entities)
    override suspend fun updateSeasonsForCloset(closetId: Int, seasonIds: List<Int>) = seasonDao.updateSeasonsForCloset(closetId, seasonIds)
    override suspend fun getSeasonIdsByClosetId(closetId: Int): List<Int> = seasonDao.getSeasonIdsByClosetId(closetId)
}

val defaultSeasonData = listOf(
    SeasonEntity(name = "春季"),
    SeasonEntity(name = "夏季"),
    SeasonEntity(name = "秋季"),
    SeasonEntity(name = "冬季"),
)