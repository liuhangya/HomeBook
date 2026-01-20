package com.fanda.homebook.data.owner

import kotlinx.coroutines.flow.Flow

interface OwnerRepository {
    suspend fun initializeDatabase()

    suspend fun getItems(): List<OwnerEntity>

    suspend fun getCount(): Int

    fun getItemById(id: Int): Flow<OwnerEntity?>
}

class LocalOwnerRepository(private val ownerDao: OwnerDao) : OwnerRepository {
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            ownerDao.resetToDefault(defaultOwnerData)
        }
    }

    override suspend fun getItems(): List<OwnerEntity> = ownerDao.getItems()

    override suspend fun getCount() = ownerDao.getCount()

    override fun getItemById(id: Int) = ownerDao.getItemById(id)
}

val defaultOwnerData = listOf(
    OwnerEntity(name = "番茄"),
    OwnerEntity(name = "阿凡达"),
    OwnerEntity(name = "圆圆"),
    OwnerEntity(name = "家庭"),
    OwnerEntity(name = "送人"),
)
