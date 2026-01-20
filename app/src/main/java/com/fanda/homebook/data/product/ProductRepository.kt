package com.fanda.homebook.data.product

import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun initializeDatabase()
    suspend fun insertWithAutoOrder(entity: ProductEntity): Long
    suspend fun getCount(): Int
    suspend fun resetToDefault(list: List<ProductEntity>)
    suspend fun insert(entity: ProductEntity): Long
    suspend fun update(entity: ProductEntity): Int
    suspend fun delete(entity: ProductEntity): Int
    suspend fun deleteById(id: Int): Int
    fun getItems(): Flow<List<ProductEntity>>
    fun getItemById(id: Int): Flow<ProductEntity>
    suspend fun getItemByName(name: String): ProductEntity?
    suspend fun getMaxSortOrder(): Int?
    suspend fun updateSortOrders(list: List<ProductEntity>): Int
}

class LocalProductRepository(private val productDao: ProductDao) : ProductRepository {

    override suspend fun initializeDatabase() {
        if (productDao.getCount() == 0) {
            resetToDefault(defaultProductData)
        }
    }

    override suspend fun insertWithAutoOrder(entity: ProductEntity) = productDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = productDao.getCount()

    override suspend fun resetToDefault(list: List<ProductEntity>) = productDao.resetToDefault(list)

    override suspend fun insert(entity: ProductEntity) = productDao.insert(entity)

    override suspend fun update(entity: ProductEntity) = productDao.update(entity)

    override suspend fun delete(entity: ProductEntity) = productDao.delete(entity)

    override suspend fun deleteById(id: Int) = productDao.deleteById(id)

    override fun getItems(): Flow<List<ProductEntity>> = productDao.getItems()

    override fun getItemById(id: Int): Flow<ProductEntity> = productDao.getItemById(id)
    override suspend fun getItemByName(name: String) = productDao.getItemByName(name)
    override suspend fun getMaxSortOrder() = productDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<ProductEntity>) = productDao.updateSortOrders(list)
}

val defaultProductData = listOf(
    ProductEntity(name = "安踏", sortOrder = 0),
    ProductEntity(name = "阿里", sortOrder = 1),
    ProductEntity(name = "竿竿", sortOrder = 2),
    ProductEntity(name = "耐克", sortOrder = 3),
    ProductEntity(name = "山姆", sortOrder = 4),
    ProductEntity(name = "其他", sortOrder = 5)
)
