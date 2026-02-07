package com.fanda.homebook.data.closet

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 衣橱数据访问对象 (Data Access Object)
 * 定义对ClosetEntity表的所有数据库操作
 */
@Dao interface ClosetDao {

    // 获取数据总数
    @Query("SELECT COUNT(*) FROM closet") suspend fun getCount(): Int

    // 单条插入，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: ClosetEntity): Long

    // 批量插入，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<ClosetEntity>)

    // 更新单条数据
    @Update suspend fun update(entity: ClosetEntity): Int

    // 删除单条数据
    @Delete suspend fun delete(entity: ClosetEntity): Int

    // 批量删除数据
    @Delete suspend fun deleteAll(closet: List<ClosetEntity>): Int

    // 批量更新数据
    @Update suspend fun updateAll(closet: List<ClosetEntity>): Int

    // 根据ID删除数据
    @Query("DELETE FROM closet WHERE id = :id") suspend fun deleteById(id: Int): Int

    /**
     * 获取指定所有者的衣橱列表（支持实时更新）
     * 使用@Transaction确保关联查询的原子性
     *
     * @param ownerId 所有者ID
     * @param moveToTrash 是否已移入回收站，默认为false（获取未删除的衣橱）
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    @Transaction @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND moveToTrash = :moveToTrash") fun getClosets(ownerId: Int, moveToTrash: Boolean = false): Flow<List<AddClosetEntity>>

    /**
     * 获取未分类的衣橱列表（支持实时更新）
     * 查询没有设置分类和子分类的衣橱物品
     *
     * @param ownerId 所有者ID
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    @Transaction @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND categoryId IS NULL AND subCategoryId IS NULL") fun getNoCategoryClosets(ownerId: Int): Flow<List<AddClosetEntity>>

    /**
     * 根据主分类获取衣橱列表（支持实时更新）
     *
     * @param ownerId 所有者ID
     * @param categoryId 主分类ID
     * @param moveToTrash 是否已移入回收站，默认为false
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    @Transaction @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND categoryId = :categoryId AND moveToTrash = :moveToTrash") fun getClosetsByCategory(
        ownerId: Int,
        categoryId: Int,
        moveToTrash: Boolean = false
    ): Flow<List<AddClosetEntity>>

    /**
     * 根据子分类获取衣橱列表（支持实时更新）
     *
     * @param ownerId 所有者ID
     * @param subCategoryId 子分类ID
     * @param moveToTrash 是否已移入回收站，默认为false
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    @Transaction @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND subCategoryId = :subCategoryId AND moveToTrash = :moveToTrash") fun getClosetsBySubCategory(
        ownerId: Int,
        subCategoryId: Int,
        moveToTrash: Boolean = false
    ): Flow<List<AddClosetEntity>>

    /**
     * 判断当前主分类下是否有设置子分类的衣橱物品
     * 用于分类管理中的状态判断
     *
     * @param ownerId 所有者ID
     * @param categoryId 主分类ID
     * @param moveToTrash 是否已移入回收站，默认为false
     * @return 布尔值，true表示有设置子分类的衣橱物品
     */
    @Transaction @Query("SELECT COUNT(*) > 0 FROM closet WHERE ownerId = :ownerId AND categoryId = :categoryId AND subCategoryId IS NOT NULL AND moveToTrash = :moveToTrash")
    suspend fun hasClosetsWithSubcategory(
        ownerId: Int, categoryId: Int, moveToTrash: Boolean = false
    ): Boolean

    /**
     * 根据ID获取单个衣橱物品（完整关联对象）
     * 使用@Transaction确保关联查询的原子性
     *
     * @param ownerId 所有者ID
     * @param id 衣橱物品ID
     * @return 完整的AddClosetEntity对象
     */
    @Transaction @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND id = :id") suspend fun getClosetById(ownerId: Int, id: Int): AddClosetEntity
}