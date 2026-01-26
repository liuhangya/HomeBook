package com.fanda.homebook.data.stock

interface StockRepository {
    suspend fun insert(entity: StockEntity): Long
}

class LocalStockRepository(private val stockDao: StockDao) : StockRepository {
    override suspend fun insert(entity: StockEntity) = stockDao.insert(entity)

}