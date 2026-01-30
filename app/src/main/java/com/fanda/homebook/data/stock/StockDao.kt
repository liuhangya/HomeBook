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

@Dao
interface StockDao {

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StockEntity): Long

    @Delete
    suspend fun delete(entity: StockEntity): Int

    @Update
    suspend fun update(entity: StockEntity): Int

    @Query("DELETE FROM stock WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    // 关联查询对象
    @Transaction
    @Query("SELECT * FROM stock WHERE  id = :id")
    suspend fun getStockById(id: Int): AddStockEntity

    @Transaction
    @RawQuery
    fun getStocksByDynamicQuery(query: SimpleSQLiteQuery): Flow<List<AddStockEntity>>


    /**
     * 根据过期时间排序查询库存
     * 排序优先级：
     * 1. 如果有 openDate 和 shelfMonth，计算 openDate + shelfMonth 个月
     * 2. 如果有 expireDate，使用 expireDate
     * 3. 否则使用 createDate
     */
    companion object {
        // 动态查询，根据参数动态生成SQL语句
        fun getStocksByRackAndSubCategory(
            rackId: Int,
            subCategoryId: Int?,
            useStatus: Int?
        ): SimpleSQLiteQuery {
            val query = StringBuilder("""
            SELECT *,
            CASE 
                WHEN openDate > 0 AND shelfMonth > 0 THEN 
                    CAST(openDate AS INTEGER) + (shelfMonth * 30 * 24 * 60 * 60 * 1000)
                WHEN expireDate > 0 THEN expireDate
                ELSE createDate
            END AS calculatedExpireDate
            FROM stock WHERE rackId = ?
        """.trimIndent())

            val args = mutableListOf<Any>()
            args.add(rackId)

            if (subCategoryId != null) {
                query.append(" AND subCategoryId = ?")
                args.add(subCategoryId)
            }

            if (useStatus != null) {
                if (useStatus == StockUseStatus.ALL.code) {
                    // 查询 useStatus 为 0 或 1 的记录
                    query.append(" AND (useStatus = ? OR useStatus = ?)")
                    args.add(StockUseStatus.NO_USE.code)
                    args.add(StockUseStatus.USING.code)
                } else {
                    // 查询特定的 useStatus
                    query.append(" AND useStatus = ?")
                    args.add(useStatus)
                }
            }

            // 按照计算出的过期时间排序
            query.append(" ORDER BY calculatedExpireDate ASC")

            return SimpleSQLiteQuery(query.toString(), args.toTypedArray())
        }
    }

    // 如果需要观察实时变化，可以使用Flow版本
    @Query(
        """
        SELECT 
            COUNT(*) as allCount,
            SUM(CASE WHEN useStatus = 0 THEN 1 ELSE 0 END) as noUseCount,
            SUM(CASE WHEN useStatus = 1 THEN 1 ELSE 0 END) as usingCount,
            SUM(CASE WHEN useStatus = 2 THEN 1 ELSE 0 END) as usedCount
        FROM stock
        WHERE rackId = :rackId
    """
    )
    fun getStockStatusCounts(rackId: Int): Flow<StockStatusCounts?>
}