package com.fanda.homebook.data.stock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface StockRepository {
    suspend fun insert(entity: StockEntity): Long

    suspend fun delete(entity: StockEntity): Int

    suspend fun update(entity: StockEntity): Int

    suspend fun deleteById(id: Int): Int

    suspend fun getStockById(id: Int): AddStockEntity

     fun getStockStatusCounts(rackId: Int): Flow<StockStatusCounts?>

    suspend fun getStocksByRackAndSubCategory(rackId: Int, subCategoryId: Int?, useStatus: Int?): Flow<List<AddStockEntity>>
}

class LocalStockRepository(private val stockDao: StockDao) : StockRepository {
    override suspend fun insert(entity: StockEntity) = stockDao.insert(entity)
    override suspend fun delete(entity: StockEntity) = stockDao.delete(entity)

    override suspend fun update(entity: StockEntity) = stockDao.update(entity)

    override suspend fun deleteById(id: Int) = stockDao.deleteById(id)

    override suspend fun getStockById(id: Int) = stockDao.getStockById(id)
    override  fun getStockStatusCounts(rackId: Int): Flow<StockStatusCounts?> = stockDao.getStockStatusCounts(rackId).map { it ?: StockStatusCounts(0, 0, 0, 0) }

    override suspend fun getStocksByRackAndSubCategory(
        rackId: Int, subCategoryId: Int?, useStatus: Int?
    ): Flow<List<AddStockEntity>> {
        val query = StockDao.getStocksByRackAndSubCategory(rackId, subCategoryId, useStatus)
        return stockDao.getStocksByDynamicQuery(query)
    }

}