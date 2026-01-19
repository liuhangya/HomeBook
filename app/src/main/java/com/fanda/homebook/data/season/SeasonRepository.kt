package com.fanda.homebook.data.season

interface SeasonRepository {
    suspend fun initializeDatabase()

    suspend fun getSeasons(): List<SeasonEntity>

    suspend fun getCount(): Int

    suspend fun getSeasonById(id: Int): SeasonEntity?
}

class LocalSeasonRepository(private val seasonDao: SeasonDao) : SeasonRepository {
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            seasonDao.resetToDefault(defaultSeasonData)
        }
    }

    override suspend fun getSeasons(): List<SeasonEntity> = seasonDao.getSeasonTypes()

    override suspend fun getCount() = seasonDao.getCount()

    override suspend fun getSeasonById(id: Int) = seasonDao.getSeasonTypeById(id)
}

val defaultSeasonData = listOf(
    SeasonEntity(name = "春季"),
    SeasonEntity(name = "夏季"),
    SeasonEntity(name = "秋季"),
    SeasonEntity(name = "冬季"),
)