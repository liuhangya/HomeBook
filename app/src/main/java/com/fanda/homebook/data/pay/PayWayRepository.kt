package com.fanda.homebook.data.pay

import kotlinx.coroutines.flow.Flow

interface PayWayRepository {
    suspend fun initializeDatabase()
    suspend fun insertWithAutoOrder(entity: PayWayEntity): Long
    suspend fun getCount(): Int
    suspend fun resetToDefault(list: List<PayWayEntity>)
    suspend fun insert(entity: PayWayEntity): Long
    suspend fun update(entity: PayWayEntity): Int
    suspend fun delete(entity: PayWayEntity): Int
    suspend fun deleteById(id: Int): Int
    fun getItems(): Flow<List<PayWayEntity>>
    fun getItemById(id: Int): Flow<PayWayEntity>
    suspend fun getItemByName(name: String): PayWayEntity?
    suspend fun getMaxSortOrder(): Int?
    suspend fun updateSortOrders(list: List<PayWayEntity>): Int
}

class LocalPayWayRepository(private val payWayDao: PayWayDao) : PayWayRepository {

    override suspend fun initializeDatabase() {
        if (payWayDao.getCount() == 0) {
            resetToDefault(defaultPayWayData)
        }
    }

    override suspend fun insertWithAutoOrder(entity: PayWayEntity) = payWayDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = payWayDao.getCount()

    override suspend fun resetToDefault(list: List<PayWayEntity>) = payWayDao.resetToDefault(list)

    override suspend fun insert(entity: PayWayEntity) = payWayDao.insert(entity)

    override suspend fun update(entity: PayWayEntity) = payWayDao.update(entity)

    override suspend fun delete(entity: PayWayEntity) = payWayDao.delete(entity)

    override suspend fun deleteById(id: Int) = payWayDao.deleteById(id)

    override fun getItems(): Flow<List<PayWayEntity>> = payWayDao.getItems()

    override fun getItemById(id: Int): Flow<PayWayEntity> = payWayDao.getItemById(id)
    override suspend fun getItemByName(name: String) = payWayDao.getItemByName(name)
    override suspend fun getMaxSortOrder() = payWayDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<PayWayEntity>) = payWayDao.updateSortOrders(list)
}

val defaultPayWayData = listOf(
    PayWayEntity(name = "微信", sortOrder = 0),
    PayWayEntity(name = "支付宝", sortOrder = 1),
    PayWayEntity(name = "现金", sortOrder = 2),
    PayWayEntity(name = "淘宝", sortOrder = 3),
    PayWayEntity(name = "京东", sortOrder = 4),
    PayWayEntity(name = "唯品会", sortOrder = 5),
    PayWayEntity(name = "阿里", sortOrder = 6),
    PayWayEntity(name = "小红书", sortOrder = 7),
    PayWayEntity(name = "拼多多", sortOrder = 8),
    PayWayEntity(name = "云闪付", sortOrder = 9),
    PayWayEntity(name = "银行卡", sortOrder = 10),
    PayWayEntity(name = "信用卡", sortOrder = 11),
    PayWayEntity(name = "医保", sortOrder = 12),
)
