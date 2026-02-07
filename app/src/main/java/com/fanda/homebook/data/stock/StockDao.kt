package com.fanda.homebook.data.stock

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow

/**
 * 库存数据访问对象 (Data Access Object)
 * 定义对StockEntity表的所有数据库操作
 */
@Dao interface StockDao {

    // 单条插入库存记录，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: StockEntity): Long

    // 删除库存记录
    @Delete suspend fun delete(entity: StockEntity): Int

    // 更新库存记录
    @Update suspend fun update(entity: StockEntity): Int

    // 根据ID删除库存记录
    @Query("DELETE FROM stock WHERE id = :id") suspend fun deleteById(id: Int): Int

    /**
     * 根据ID获取单个库存记录的完整信息
     * 使用@Transaction确保关联查询的原子性
     *
     * @param id 库存记录ID
     * @return 完整的AddStockEntity对象
     */
    @Transaction @Query("SELECT * FROM stock WHERE id = :id") suspend fun getStockById(id: Int): AddStockEntity

    /**
     * 动态查询库存记录列表
     * 使用@RawQuery支持复杂的动态SQL查询
     *
     * @param query 动态构建的SQL查询
     * @return Flow流，包含完整的AddStockEntity列表
     */
    @Transaction @RawQuery fun getStocksByDynamicQuery(query: SimpleSQLiteQuery): Flow<List<AddStockEntity>>

    /**
     * 库存查询工具类
     * 提供根据货架和子分类查询库存的方法
     *
     * 排序优先级：
     * 1. 如果有 openDate 和 shelfMonth，计算 openDate + shelfMonth 个月
     * 2. 如果有 expireDate，使用 expireDate
     * 3. 否则使用 createDate
     */
    companion object {
        /**
         * 动态查询，根据参数动态生成SQL语句
         *
         * @param rackId 货架ID（必填）
         * @param subCategoryId 子分类ID（可选）
         * @param useStatus 使用状态（可选）
         * @return SimpleSQLiteQuery 包含SQL语句和参数的查询对象
         */
        fun getStocksByRackAndSubCategory(
            rackId: Int, subCategoryId: Int?, useStatus: Int?
        ): SimpleSQLiteQuery {
            // 构建基础查询语句，计算过期时间
            val query = StringBuilder(
                """
            SELECT *,
            CASE 
                WHEN openDate > 0 AND shelfMonth > 0 THEN 
                    CAST(openDate AS INTEGER) + (shelfMonth * 30 * 24 * 60 * 60 * 1000)
                WHEN expireDate > 0 THEN expireDate
                ELSE createDate
            END AS calculatedExpireDate
            FROM stock WHERE rackId = ?
        """.trimIndent()
            )

            // 构建参数列表
            val args = mutableListOf<Any>()
            args.add(rackId)

            // 添加子分类条件
            if (subCategoryId != null) {
                query.append(" AND subCategoryId = ?")
                args.add(subCategoryId)
            }

            // 添加使用状态条件
            if (useStatus != null) {
                if (useStatus == StockUseStatus.ALL.code) {
                    // 查询 useStatus 为 0 或 1 的记录（未使用或使用中）
                    query.append(" AND (useStatus = ? OR useStatus = ?)")
                    args.add(StockUseStatus.NO_USE.code)
                    args.add(StockUseStatus.USING.code)
                } else {
                    // 查询特定的 useStatus
                    query.append(" AND useStatus = ?")
                    args.add(useStatus)
                }
            }

            // 按照计算出的过期时间升序排序（快过期的在前）
            query.append(" ORDER BY calculatedExpireDate ASC")

            return SimpleSQLiteQuery(query.toString(), args.toTypedArray())
        }
    }

    /**
     * 获取库存状态统计信息（Flow版本，支持实时更新）
     * 统计指定货架下库存的各种使用状态数量
     *
     * @param rackId 货架ID
     * @return Flow流，包含StockStatusCounts统计对象
     */
    @Query(
        """
        SELECT 
            COUNT(*) as allCount,                          -- 总数量
            SUM(CASE WHEN useStatus = 0 THEN 1 ELSE 0 END) as noUseCount,    -- 未使用数量
            SUM(CASE WHEN useStatus = 1 THEN 1 ELSE 0 END) as usingCount,    -- 使用中数量
            SUM(CASE WHEN useStatus = 2 THEN 1 ELSE 0 END) as usedCount      -- 已用完数量
        FROM stock
        WHERE rackId = :rackId
    """
    ) fun getStockStatusCounts(rackId: Int): Flow<StockStatusCounts?>
}