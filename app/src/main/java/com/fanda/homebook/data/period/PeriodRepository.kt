package com.fanda.homebook.data.period

import kotlinx.coroutines.flow.Flow

interface PeriodRepository {
    suspend fun initializeDatabase()

    suspend fun getTypes(): List<PeriodEntity>

    suspend fun getCount(): Int

    fun getTypeById(id: Int): Flow<PeriodEntity?>
}

class LocalPeriodRepository(private val periodDao: PeriodDao) : PeriodRepository {
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            periodDao.resetToDefault(defaultPeriodData)
        }
    }

    override suspend fun getTypes(): List<PeriodEntity> = periodDao.getTypes()

    override suspend fun getCount() = periodDao.getCount()

    override fun getTypeById(id: Int) = periodDao.getTypeById(id)
}

val defaultPeriodData = listOf(
    PeriodEntity(name = "全天"),
    PeriodEntity(name = "日用"),
    PeriodEntity(name = "夜用"),
)