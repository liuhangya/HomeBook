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

    companion object {
        // 动态查询，根据参数动态生成SQL语句
        fun getStocksByRackAndSubCategory(
            rackId: Int,
            subCategoryId: Int?,
            useStatus: Int?
        ): SimpleSQLiteQuery {
            val query = StringBuilder("SELECT * FROM stock WHERE rackId = ?")
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

            // 添加排序规则：优先按过期时间，其次购买时间，最后创建时间
            query.append(" ORDER BY ")
            query.append(
                """
            CASE 
                WHEN expireDate IS NOT NULL AND expireDate > 0 THEN expireDate
                WHEN buyDate IS NOT NULL AND buyDate > 0 THEN buyDate
                ELSE createDate
            END ASC
        """.trimIndent()
            )

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