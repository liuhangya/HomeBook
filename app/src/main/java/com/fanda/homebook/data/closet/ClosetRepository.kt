package com.fanda.homebook.data.closet

import kotlinx.coroutines.flow.Flow

/**
 * 衣橱仓库接口
 * 定义衣橱数据的操作契约，提供数据访问的抽象层
 */
interface ClosetRepository {
    // 获取衣橱物品总数
    suspend fun getCount(): Int

    // 单条操作方法
    suspend fun insert(closet: ClosetEntity): Long
    suspend fun update(closet: ClosetEntity): Int
    suspend fun delete(closet: ClosetEntity): Int

    // 批量操作方法
    suspend fun insertAll(closets: List<ClosetEntity>)
    suspend fun updateAll(closet: List<ClosetEntity>): Int
    suspend fun deleteAll(closet: List<ClosetEntity>): Int

    // 查询方法（Flow版本，支持实时更新）
    fun getClosets(ownerId: Int, moveToTrash: Boolean = false): Flow<List<AddClosetEntity>>
    fun getNoCategoryClosets(ownerId: Int): Flow<List<AddClosetEntity>>
    fun getClosetsByCategory(
        ownerId: Int, categoryId: Int, moveToTrash: Boolean = false
    ): Flow<List<AddClosetEntity>>

    fun getClosetsBySubCategory(
        ownerId: Int, subCategoryId: Int, moveToTrash: Boolean = false
    ): Flow<List<AddClosetEntity>>

    // 查询方法（挂起函数版本）
    suspend fun getClosetById(ownerId: Int, id: Int): AddClosetEntity

    // 业务逻辑方法
    suspend fun hasClosetsWithSubcategory(
        ownerId: Int, categoryId: Int
    ): Boolean
}

/**
 * 本地衣橱仓库实现类
 * 实现ClosetRepository接口，封装对Room数据库的直接访问
 *
 * @property closetDao 衣橱数据访问对象
 */
class LocalClosetRepository(private val closetDao: ClosetDao) : ClosetRepository {
    /**
     * 获取衣橱物品总数
     */
    override suspend fun getCount() = closetDao.getCount()

    /**
     * 插入单条衣橱记录
     *
     * @param closet 衣橱实体
     * @return 新插入记录的ID
     */
    override suspend fun insert(closet: ClosetEntity) = closetDao.insert(closet)

    /**
     * 批量插入衣橱记录
     *
     * @param closets 衣橱实体列表
     */
    override suspend fun insertAll(closets: List<ClosetEntity>) = closetDao.insertAll(closets)

    /**
     * 更新单条衣橱记录
     *
     * @param closet 衣橱实体
     * @return 受影响的行数
     */
    override suspend fun update(closet: ClosetEntity) = closetDao.update(closet)

    /**
     * 删除单条衣橱记录
     *
     * @param closet 衣橱实体
     * @return 受影响的行数
     */
    override suspend fun delete(closet: ClosetEntity) = closetDao.delete(closet)

    /**
     * 批量删除衣橱记录
     *
     * @param closet 衣橱实体列表
     * @return 受影响的行数
     */
    override suspend fun deleteAll(closet: List<ClosetEntity>) = closetDao.deleteAll(closet)

    /**
     * 批量更新衣橱记录
     *
     * @param closet 衣橱实体列表
     * @return 受影响的行数
     */
    override suspend fun updateAll(closet: List<ClosetEntity>) = closetDao.updateAll(closet)

    /**
     * 获取指定所有者的衣橱列表（支持实时更新）
     *
     * @param ownerId 所有者ID
     * @param moveToTrash 是否包含已移入回收站的物品，默认为false
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    override fun getClosets(ownerId: Int, moveToTrash: Boolean): Flow<List<AddClosetEntity>> = closetDao.getClosets(ownerId, moveToTrash)

    /**
     * 获取未分类的衣橱物品列表（支持实时更新）
     * 查询没有设置分类和子分类的衣橱物品
     *
     * @param ownerId 所有者ID
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    override fun getNoCategoryClosets(ownerId: Int): Flow<List<AddClosetEntity>> = closetDao.getNoCategoryClosets(ownerId)

    /**
     * 根据主分类获取衣橱列表（支持实时更新）
     *
     * @param ownerId 所有者ID
     * @param categoryId 主分类ID
     * @param moveToTrash 是否包含已移入回收站的物品，默认为false
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    override fun getClosetsByCategory(
        ownerId: Int, categoryId: Int, moveToTrash: Boolean
    ): Flow<List<AddClosetEntity>> = closetDao.getClosetsByCategory(ownerId, categoryId, moveToTrash)

    /**
     * 根据子分类获取衣橱列表（支持实时更新）
     *
     * @param ownerId 所有者ID
     * @param subCategoryId 子分类ID
     * @param moveToTrash 是否包含已移入回收站的物品，默认为false
     * @return Flow流，包含完整的AddClosetEntity列表
     */
    override fun getClosetsBySubCategory(
        ownerId: Int, subCategoryId: Int, moveToTrash: Boolean
    ): Flow<List<AddClosetEntity>> = closetDao.getClosetsBySubCategory(ownerId, subCategoryId, moveToTrash)

    /**
     * 根据ID获取单个衣橱物品的完整信息
     *
     * @param ownerId 所有者ID
     * @param id 衣橱物品ID
     * @return 完整的AddClosetEntity对象
     */
    override suspend fun getClosetById(ownerId: Int, id: Int): AddClosetEntity = closetDao.getClosetById(ownerId, id)

    /**
     * 判断指定主分类下是否有设置子分类的衣橱物品
     * 用于分类管理中的状态判断，比如是否可以删除分类等
     *
     * @param ownerId 所有者ID
     * @param categoryId 主分类ID
     * @return 布尔值，true表示有设置子分类的衣橱物品
     */
    override suspend fun hasClosetsWithSubcategory(ownerId: Int, categoryId: Int) = closetDao.hasClosetsWithSubcategory(ownerId, categoryId)
}