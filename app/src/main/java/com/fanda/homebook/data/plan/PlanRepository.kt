package com.fanda.homebook.data.plan

import kotlinx.coroutines.flow.Flow

interface PlanRepository {

    fun getPlanByYearAndMonth(year: Int, month: Int, bookId: Int): Flow<PlanEntity?>

    suspend fun insert(item: PlanEntity)

    suspend fun update(item: PlanEntity): Int
}

class LocalPlanRepository(private val planDao: PlanDao) : PlanRepository {
    override fun getPlanByYearAndMonth(year: Int, month: Int, bookId: Int) = planDao.getPlanByYearAndMonth(year, month, bookId)

    override suspend fun insert(item: PlanEntity) {
        planDao.insert(item)
    }

    override suspend fun update(item: PlanEntity) = planDao.update(item)
}