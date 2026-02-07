package com.fanda.homebook.data.stock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 库存仓库接口
 * 定义库存数据的操作契约，提供数据访问的抽象层
 */
interface StockRepository {
    // 插入库存记录
    suspend fun insert(entity: StockEntity): Long

    // 删除库存记录
    suspend fun delete(entity: StockEntity): Int

    // 更新库存记录
    suspend fun update(entity: StockEntity): Int

    // 根据ID删除库存记录
    suspend fun deleteById(id: Int): Int

    // 根据ID获取单个库存记录的完整信息
    suspend fun getStockById(id: Int): AddStockEntity

    /**
     * 获取库存状态统计信息
     *
     * @param rackId 货架ID
     * @return Flow流，包含StockStatusCounts统计对象（当统计为null时返回默认值）
     */
    fun getStockStatusCounts(rackId: Int): Flow<StockStatusCounts?>

    /**
     * 根据货架和子分类查询库存记录列表
     *
     * @param rackId 货架ID（必填）
     * @param subCategoryId 子分类ID（可选）
     * @param useStatus 使用状态（可选）
     * @return Flow流，包含符合条件的AddStockEntity列表
     */
    suspend fun getStocksByRackAndSubCategory(
        rackId: Int, subCategoryId: Int?, useStatus: Int?
    ): Flow<List<AddStockEntity>>
}

/**
 * 本地库存仓库实现类
 * 实现StockRepository接口，封装对Room数据库的直接访问
 *
 * @property stockDao 库存数据访问对象
 */
class LocalStockRepository(private val stockDao: StockDao) : StockRepository {
    /**
     * 插入库存记录
     *
     * @param entity 库存实体
     * @return 新插入记录的ID
     */
    override suspend fun insert(entity: StockEntity) = stockDao.insert(entity)

    /**
     * 删除库存记录
     *
     * @param entity 库存实体
     * @return 受影响的行数
     */
    override suspend fun delete(entity: StockEntity) = stockDao.delete(entity)

    /**
     * 更新库存记录
     *
     * @param entity 库存实体
     * @return 受影响的行数
     */
    override suspend fun update(entity: StockEntity) = stockDao.update(entity)

    /**
     * 根据ID删除库存记录
     *
     * @param id 库存记录ID
     * @return 受影响的行数
     */
    override suspend fun deleteById(id: Int) = stockDao.deleteById(id)

    /**
     * 根据ID获取单个库存记录的完整信息
     *
     * @param id 库存记录ID
     * @return 完整的AddStockEntity对象
     */
    override suspend fun getStockById(id: Int) = stockDao.getStockById(id)

    /**
     * 获取库存状态统计信息
     * 使用map操作符确保返回的Flow不为null，当查询结果为null时返回默认统计值
     *
     * @param rackId 货架ID
     * @return Flow流，包含StockStatusCounts统计对象
     */
    override fun getStockStatusCounts(rackId: Int): Flow<StockStatusCounts?> = stockDao.getStockStatusCounts(rackId).map { it ?: StockStatusCounts(0, 0, 0, 0) } // 处理null情况，返回默认统计值

    /**
     * 根据货架和子分类查询库存记录列表
     *
     * @param rackId 货架ID（必填）
     * @param subCategoryId 子分类ID（可选）
     * @param useStatus 使用状态（可选）
     * @return Flow流，包含符合条件的AddStockEntity列表
     */
    override suspend fun getStocksByRackAndSubCategory(
        rackId: Int, subCategoryId: Int?, useStatus: Int?
    ): Flow<List<AddStockEntity>> {
        // 使用StockDao的伴生对象方法构建动态查询
        val query = StockDao.getStocksByRackAndSubCategory(rackId, subCategoryId, useStatus)

        // 执行动态查询
        return stockDao.getStocksByDynamicQuery(query)
    }
}