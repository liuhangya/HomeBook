package com.fanda.homebook.data.owner

import kotlinx.coroutines.flow.Flow

interface OwnerRepository {
    suspend fun initializeDatabase()

    suspend fun getItems(): List<OwnerEntity>

    suspend fun getCount(): Int

    fun getItemById(id: Int): Flow<OwnerEntity?>

    suspend fun updateItems(list: List<OwnerEntity>): Int

    fun getSelectedItem(): Flow<OwnerEntity?>

    suspend fun updateItem(item: OwnerEntity): Int
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
    override suspend fun updateItems(list: List<OwnerEntity>) = ownerDao.updateItems(list)
    override fun getSelectedItem() = ownerDao.getSelectedItem()
    override suspend fun updateItem(item: OwnerEntity) = ownerDao.updateItem(item)
}

val defaultOwnerData = listOf(
    OwnerEntity(name = "番茄", selected = true),
    OwnerEntity(name = "阿凡达"),
    OwnerEntity(name = "圆圆"),
    OwnerEntity(name = "家庭"),
    OwnerEntity(name = "送人"),
)
