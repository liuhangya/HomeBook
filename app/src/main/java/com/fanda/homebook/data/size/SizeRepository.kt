package com.fanda.homebook.data.size

import com.fanda.homebook.data.product.ProductDao
import kotlinx.coroutines.flow.Flow

interface SizeRepository {
    suspend fun initializeDatabase()
    suspend fun insertWithAutoOrder(entity: SizeEntity): Long
    suspend fun getCount(): Int
    suspend fun resetToDefault(list: List<SizeEntity>)
    suspend fun insert(entity: SizeEntity): Long
    suspend fun update(entity: SizeEntity): Int
    suspend fun delete(entity: SizeEntity): Int
    suspend fun deleteById(id: Int): Int
    fun getItems(): Flow<List<SizeEntity>>
    fun getItemById(id: Int): Flow<SizeEntity>
    suspend fun getItemByName(name: String): SizeEntity?
    suspend fun getMaxSortOrder(): Int?
    suspend fun updateSortOrders(list: List<SizeEntity>): Int
}

class LocalSizeRepository(private val sizeDao: SizeDao) : SizeRepository {

    override suspend fun initializeDatabase() {
        if (sizeDao.getCount() == 0) {
            resetToDefault(defaultSizeData)
        }
    }

    override suspend fun insertWithAutoOrder(entity: SizeEntity) = sizeDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = sizeDao.getCount()

    override suspend fun resetToDefault(list: List<SizeEntity>) = sizeDao.resetToDefault(list)

    override suspend fun insert(entity: SizeEntity) = sizeDao.insert(entity)

    override suspend fun update(entity: SizeEntity) = sizeDao.update(entity)

    override suspend fun delete(entity: SizeEntity) = sizeDao.delete(entity)

    override suspend fun deleteById(id: Int) = sizeDao.deleteById(id)

    override fun getItems(): Flow<List<SizeEntity>> = sizeDao.getItems()

    override fun getItemById(id: Int): Flow<SizeEntity> = sizeDao.getItemById(id)
    override suspend fun getItemByName(name: String) = sizeDao.getItemByName(name)
    override suspend fun getMaxSortOrder() = sizeDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<SizeEntity>) = sizeDao.updateSortOrders(list)
}

val defaultSizeData = listOf(
    SizeEntity(name = "XS", sortOrder = 0),
    SizeEntity(name = "S", sortOrder = 1),
    SizeEntity(name = "M", sortOrder = 2),
    SizeEntity(name = "L", sortOrder = 3),
    SizeEntity(name = "XL", sortOrder = 4),
    SizeEntity(name = "XXL", sortOrder = 5),
    SizeEntity(name = "XXXL", sortOrder = 6),
    SizeEntity(name = "34", sortOrder = 7),
    SizeEntity(name = "35", sortOrder = 8),
    SizeEntity(name = "36", sortOrder = 9),
    SizeEntity(name = "37", sortOrder = 10),
    SizeEntity(name = "38", sortOrder = 11),
    SizeEntity(name = "39", sortOrder = 12),
    SizeEntity(name = "40", sortOrder = 13),
    SizeEntity(name = "41", sortOrder = 14),
    SizeEntity(name = "42", sortOrder = 15),
    SizeEntity(name = "43", sortOrder = 16),
    SizeEntity(name = "44", sortOrder = 17),
    SizeEntity(name = "45", sortOrder = 18),
    SizeEntity(name = "小码", sortOrder = 19),
    SizeEntity(name = "中码", sortOrder = 20),
    SizeEntity(name = "大码", sortOrder = 21),
    SizeEntity(name = "超大", sortOrder = 22),
)
