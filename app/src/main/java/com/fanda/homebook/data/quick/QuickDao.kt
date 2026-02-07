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
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.flow.Flow

/**
 * 快速记账数据访问对象 (Data Access Object)
 * 定义对QuickEntity表的所有数据库操作
 */
@Dao interface QuickDao {
    // 单条插入快速记账记录，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: QuickEntity): Long

    // 删除快速记账记录
    @Delete suspend fun delete(entity: QuickEntity): Int

    // 更新快速记账记录
    @Update suspend fun update(entity: QuickEntity): Int

    // 根据ID删除快速记账记录
    @Query("DELETE FROM quick WHERE id = :id") suspend fun deleteById(id: Int): Int

    /**
     * 根据ID获取单个快速记账记录的完整信息
     * 使用@Transaction确保关联查询的原子性
     *
     * @param id 快速记账记录ID
     * @return 完整的AddQuickEntity对象
     */
    @Transaction @Query("SELECT * FROM quick WHERE id = :id") suspend fun getQuickById(id: Int): AddQuickEntity

    /**
     * 动态查询快速记账记录列表
     * 使用@RawQuery支持复杂的动态SQL查询
     *
     * @param query 动态构建的SQL查询
     * @return Flow流，包含完整的AddQuickEntity列表
     */
    @Transaction @RawQuery fun getQuickListByDynamicQuery(query: SimpleSQLiteQuery): Flow<List<AddQuickEntity>>

    /**
     * 根据分类条件查询快速记账记录列表
     * 这是一个便捷方法，内部使用QuickQueryBuilder构建动态查询
     *
     * @param bookId 账本ID（可选）
     * @param categoryId 主分类ID（可选）
     * @param subCategoryId 子分类ID（可选）
     * @param categoryType 分类类型（可选）
     * @return Flow流，包含符合条件的AddQuickEntity列表
     */
    fun getQuickListByCategory(
        bookId: Int? = null, categoryId: Int? = null, subCategoryId: Int? = null, categoryType: Int? = null
    ): Flow<List<AddQuickEntity>> {
        // 使用查询构建器创建动态查询
        val queryBuilder = QuickQueryBuilder()

        // 根据参数设置查询条件
        bookId?.let { queryBuilder.withBookId(it) }
        categoryId?.let { queryBuilder.withCategoryId(it) }
        subCategoryId?.let { queryBuilder.withSubCategoryId(it) }
        categoryType?.let { queryBuilder.withCategoryType(it) }

        // 输出调试信息
        LogUtils.d("getQuickListByCategory query: ${queryBuilder.build().sql}")

        // 执行动态查询
        return getQuickListByDynamicQuery(queryBuilder.build())
    }
}