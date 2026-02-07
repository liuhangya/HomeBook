package com.fanda.homebook.data.quick

import kotlinx.coroutines.flow.Flow

/**
 * 快速记账仓库接口
 * 定义快速记账数据的操作契约，提供数据访问的抽象层
 */
interface QuickRepository {
    // 插入快速记账记录
    suspend fun insert(entity: QuickEntity): Long

    // 删除快速记账记录
    suspend fun delete(entity: QuickEntity): Int

    // 更新快速记账记录
    suspend fun update(entity: QuickEntity): Int

    // 根据ID删除快速记账记录
    suspend fun deleteById(id: Int): Int

    // 根据ID获取单个快速记账记录的完整信息
    suspend fun getQuickById(id: Int): AddQuickEntity

    /**
     * 根据分类条件查询快速记账记录列表
     *
     * @param bookId 账本ID（可选）
     * @param categoryId 主分类ID（可选）
     * @param subCategoryId 子分类ID（可选）
     * @param categoryType 分类类型（可选）
     * @return Flow流，包含符合条件的AddQuickEntity列表
     */
    fun getQuickListByCategory(
        bookId: Int? = null, categoryId: Int? = null, subCategoryId: Int? = null, categoryType: Int? = null
    ): Flow<List<AddQuickEntity>>
}

/**
 * 本地快速记账仓库实现类
 * 实现QuickRepository接口，封装对Room数据库的直接访问
 *
 * @property quickDao 快速记账数据访问对象
 */
class LocalQuickRepository(private val quickDao: QuickDao) : QuickRepository {
    /**
     * 插入快速记账记录
     *
     * @param entity 快速记账实体
     * @return 新插入记录的ID
     */
    override suspend fun insert(entity: QuickEntity) = quickDao.insert(entity)

    /**
     * 删除快速记账记录
     *
     * @param entity 快速记账实体
     * @return 受影响的行数
     */
    override suspend fun delete(entity: QuickEntity) = quickDao.delete(entity)

    /**
     * 更新快速记账记录
     *
     * @param entity 快速记账实体
     * @return 受影响的行数
     */
    override suspend fun update(entity: QuickEntity) = quickDao.update(entity)

    /**
     * 根据ID删除快速记账记录
     *
     * @param id 快速记账记录ID
     * @return 受影响的行数
     */
    override suspend fun deleteById(id: Int) = quickDao.deleteById(id)

    /**
     * 根据ID获取单个快速记账记录的完整信息
     *
     * @param id 快速记账记录ID
     * @return 完整的AddQuickEntity对象
     */
    override suspend fun getQuickById(id: Int) = quickDao.getQuickById(id)

    /**
     * 根据分类条件查询快速记账记录列表
     *
     * @param bookId 账本ID（可选）
     * @param categoryId 主分类ID（可选）
     * @param subCategoryId 子分类ID（可选）
     * @param categoryType 分类类型（可选）
     * @return Flow流，包含符合条件的AddQuickEntity列表
     */
    override fun getQuickListByCategory(
        bookId: Int?, categoryId: Int?, subCategoryId: Int?, categoryType: Int?
    ): Flow<List<AddQuickEntity>> = quickDao.getQuickListByCategory(
        bookId, categoryId, subCategoryId, categoryType
    )
}