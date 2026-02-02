package com.fanda.homebook.data.quick

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.fanda.homebook.data.stock.AddStockEntity
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.flow.Flow

@Dao interface QuickDao {
    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: QuickEntity): Long

    @Delete suspend fun delete(entity: QuickEntity): Int

    @Update suspend fun update(entity: QuickEntity): Int

    @Query("DELETE FROM quick WHERE id = :id") suspend fun deleteById(id: Int): Int

    // 关联查询对象
    @Transaction @Query("SELECT * FROM quick WHERE  id = :id") suspend fun getQuickById(id: Int): AddQuickEntity

    @Transaction @RawQuery fun getQuickListByDynamicQuery(query: SimpleSQLiteQuery): Flow<List<AddQuickEntity>>

    // 根据分类和子分类查询
    fun getQuickListByCategory(
        bookId: Int? = null, categoryId: Int? = null, subCategoryId: Int? = null
    ): Flow<List<AddQuickEntity>> {
        val queryBuilder = QuickQueryBuilder()

        bookId?.let { queryBuilder.withBookId(it) }
        categoryId?.let { queryBuilder.withCategoryId(it) }
        subCategoryId?.let { queryBuilder.withSubCategoryId(it) }

        LogUtils.d("getQuickListByCategory query: ${queryBuilder.build().sql}")
        return getQuickListByDynamicQuery(queryBuilder.build())
    }
}