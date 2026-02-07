package com.fanda.homebook.data.color

import kotlinx.coroutines.flow.Flow

/**
 * 颜色类型仓库接口
 * 定义颜色类型数据的操作契约，提供数据访问的抽象层
 */
interface ColorTypeRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 插入颜色类型并自动设置排序（放在最后）
    suspend fun insertWithAutoOrder(entity: ColorTypeEntity): Long

    // 获取颜色类型总数
    suspend fun getCount(): Int

    // 重置数据库为默认颜色数据
    suspend fun resetToDefault(list: List<ColorTypeEntity>)

    // 插入颜色类型
    suspend fun insert(entity: ColorTypeEntity): Long

    // 更新颜色类型
    suspend fun update(entity: ColorTypeEntity): Int

    // 删除颜色类型
    suspend fun delete(entity: ColorTypeEntity): Int

    // 根据ID删除颜色类型
    suspend fun deleteById(id: Int): Int

    // 获取所有颜色类型列表（Flow版本，支持实时更新）
    fun getItems(): Flow<List<ColorTypeEntity>>

    // 根据ID获取单个颜色类型（Flow版本，支持实时更新）
    fun getItemById(id: Int): Flow<ColorTypeEntity>

    // 根据名称获取颜色类型
    suspend fun getItemByName(name: String): ColorTypeEntity?

    // 获取最大排序值
    suspend fun getMaxSortOrder(): Int?

    // 批量更新颜色类型排序字段
    suspend fun updateSortOrders(list: List<ColorTypeEntity>): Int
}

/**
 * 本地颜色类型仓库实现类
 * 实现ColorTypeRepository接口，封装对Room数据库的直接访问
 *
 * @property colorTypeDao 颜色类型数据访问对象
 */
class LocalColorTypeRepository(private val colorTypeDao: ColorTypeDao) : ColorTypeRepository {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认颜色数据
     */
    override suspend fun initializeDatabase() {
        if (colorTypeDao.getCount() == 0) {
            resetToDefault(defaultColorData)
        }
    }

    // 以下方法都是直接委托给colorTypeDao对应的方法
    override suspend fun insertWithAutoOrder(entity: ColorTypeEntity) = colorTypeDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = colorTypeDao.getCount()
    override suspend fun resetToDefault(list: List<ColorTypeEntity>) = colorTypeDao.resetToDefault(list)
    override suspend fun insert(entity: ColorTypeEntity) = colorTypeDao.insert(entity)
    override suspend fun update(entity: ColorTypeEntity) = colorTypeDao.update(entity)
    override suspend fun delete(entity: ColorTypeEntity) = colorTypeDao.delete(entity)
    override suspend fun deleteById(id: Int) = colorTypeDao.deleteById(id)
    override fun getItems(): Flow<List<ColorTypeEntity>> = colorTypeDao.getItems()
    override fun getItemById(id: Int): Flow<ColorTypeEntity> = colorTypeDao.getItemById(id)
    override suspend fun getItemByName(name: String) = colorTypeDao.getItemByName(name)
    override suspend fun getMaxSortOrder() = colorTypeDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<ColorTypeEntity>) = colorTypeDao.updateSortOrders(list)
}

/**
 * 默认颜色数据
 * 应用首次安装或数据库重置时使用的默认颜色列表
 * 包含13种常见颜色系列，按sortOrder排序
 */
val defaultColorData = listOf(
    ColorTypeEntity(
        name = "棕色系",
        color = 0xFF6A3D06,  // 深棕色
        sortOrder = 0,
    ), ColorTypeEntity(
        name = "黑色系",
        color = 0xFF000000,   // 纯黑色
        sortOrder = 1,
    ), ColorTypeEntity(
        name = "蓝色系",
        color = 0xFF9CD4EB,   // 浅蓝色
        sortOrder = 2,
    ), ColorTypeEntity(
        name = "绿色系",
        color = 0xFFA4D66B,   // 浅绿色
        sortOrder = 3,
    ), ColorTypeEntity(
        name = "紫色系",
        color = 0xFFB398F1,   // 淡紫色
        sortOrder = 4,
    ), ColorTypeEntity(
        name = "红色系",
        color = 0xFFDA4851,   // 深红色
        sortOrder = 5,
    ), ColorTypeEntity(
        name = "灰色系",
        color = 0xFFDADADA,   // 浅灰色
        sortOrder = 6,
    ), ColorTypeEntity(
        name = "玫红系",
        color = 0xFFE360BE,   // 玫红色
        sortOrder = 7,
    ), ColorTypeEntity(
        name = "橙色系",
        color = 0xFFEC9F4C,   // 橙色
        sortOrder = 8,
    ), ColorTypeEntity(
        name = "金色系",
        color = 0xFFEFDD8B,   // 淡金色
        sortOrder = 9,
    ), ColorTypeEntity(
        name = "裸色系",
        color = 0xFFEFE5CE,   // 裸色/米色
        sortOrder = 10,
    ), ColorTypeEntity(
        name = "黄色系",
        color = 0xFFF8D854,   // 浅黄色
        sortOrder = 11,
    ), ColorTypeEntity(
        name = "白色系",
        color = 0xFFFFFFFF,   // 纯白色
        sortOrder = 12,
    )
)