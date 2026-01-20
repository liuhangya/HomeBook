package com.fanda.homebook.data.season

import kotlinx.coroutines.flow.Flow

interface SeasonRepository {
    suspend fun initializeDatabase()

    suspend fun getSeasons(): List<SeasonEntity>

    suspend fun getCount(): Int

     fun getSeasonById(id: Int): Flow<SeasonEntity?>
}

class LocalSeasonRepository(private val seasonDao: SeasonDao) : SeasonRepository {
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            seasonDao.resetToDefault(defaultSeasonData)
        }
    }

    override suspend fun getSeasons(): List<SeasonEntity> = seasonDao.getSeasonTypes()

    override suspend fun getCount() = seasonDao.getCount()

    override  fun getSeasonById(id: Int) = seasonDao.getSeasonTypeById(id)
}

val defaultSeasonData = listOf(
    SeasonEntity(name = "春季"),
    SeasonEntity(name = "夏季"),
    SeasonEntity(name = "秋季"),
    SeasonEntity(name = "冬季"),
)