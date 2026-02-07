package com.fanda.homebook.data.size

import kotlinx.coroutines.flow.Flow

/**
 * 尺码数据仓库接口
 * 定义尺码数据的持久化操作契约
 */
interface SizeRepository {
    /**
     * 初始化数据库
     * 检查数据库是否为空，为空时插入默认数据
     */
    suspend fun initializeDatabase()

    /**
     * 插入尺码记录并自动设置排序序号
     * @param entity 尺码实体对象
     * @return 插入记录的行ID
     */
    suspend fun insertWithAutoOrder(entity: SizeEntity): Long

    /**
     * 获取尺码记录总数
     * @return 记录总数
     */
    suspend fun getCount(): Int

    /**
     * 重置为默认尺码数据
     * @param list 默认尺码数据列表
     */
    suspend fun resetToDefault(list: List<SizeEntity>)

    /**
     * 插入尺码记录
     * @param entity 尺码实体对象
     * @return 插入记录的行ID
     */
    suspend fun insert(entity: SizeEntity): Long

    /**
     * 更新尺码记录
     * @param entity 尺码实体对象
     * @return 受影响的行数
     */
    suspend fun update(entity: SizeEntity): Int

    /**
     * 删除尺码记录
     * @param entity 尺码实体对象
     * @return 受影响的行数
     */
    suspend fun delete(entity: SizeEntity): Int

    /**
     * 根据ID删除尺码记录
     * @param id 尺码记录ID
     * @return 受影响的行数
     */
    suspend fun deleteById(id: Int): Int

    /**
     * 获取所有尺码记录
     * @return 尺码记录列表的数据流
     */
    fun getItems(): Flow<List<SizeEntity>>

    /**
     * 根据ID获取尺码记录
     * @param id 尺码记录ID
     * @return 尺码实体的数据流
     */
    fun getItemById(id: Int): Flow<SizeEntity>

    /**
     * 根据名称获取尺码记录
     * @param name 尺码名称
     * @return 尺码实体对象，未找到时返回null
     */
    suspend fun getItemByName(name: String): SizeEntity?

    /**
     * 获取最大排序序号
     * @return 最大排序序号，无记录时返回null
     */
    suspend fun getMaxSortOrder(): Int?

    /**
     * 批量更新尺码排序序号
     * @param list 尺码实体列表
     * @return 受影响的行数
     */
    suspend fun updateSortOrders(list: List<SizeEntity>): Int
}

/**
 * 本地尺码数据仓库实现类
 * 使用Room数据库操作尺码数据
 * @property sizeDao 尺码数据访问对象
 */
class LocalSizeRepository(private val sizeDao: SizeDao) : SizeRepository {

    /**
     * 初始化数据库
     * 如果数据库为空，则插入默认尺码数据
     */
    override suspend fun initializeDatabase() {
        if (sizeDao.getCount() == 0) {
            resetToDefault(defaultSizeData)
        }
    }

    override suspend fun insertWithAutoOrder(entity: SizeEntity) = sizeDao.insertWithAutoOrder(entity)

    override suspend fun getCount() = sizeDao.getCount()

    override suspend fun resetToDefault(list: List<SizeEntity>) = sizeDao.resetToDefault(list)

    override suspend fun insert(entity: SizeEntity) = sizeDao.insert(entity)

    override suspend fun update(entity: SizeEntity) = sizeDao.update(entity)

    override suspend fun delete(entity: SizeEntity) = sizeDao.delete(entity)

    override suspend fun deleteById(id: Int) = sizeDao.deleteById(id)

    override fun getItems(): Flow<List<SizeEntity>> = sizeDao.getItems()

    override fun getItemById(id: Int): Flow<SizeEntity> = sizeDao.getItemById(id)

    override suspend fun getItemByName(name: String) = sizeDao.getItemByName(name)

    override suspend fun getMaxSortOrder() = sizeDao.getMaxSortOrder()

    override suspend fun updateSortOrders(list: List<SizeEntity>) = sizeDao.updateSortOrders(list)
}

/**
 * 默认尺码数据
 * 包含常见的服装尺码和鞋码
 */
val defaultSizeData = listOf(
    // 字母尺码系列
    SizeEntity(name = "XS", sortOrder = 0),
    SizeEntity(name = "S", sortOrder = 1),
    SizeEntity(name = "M", sortOrder = 2),
    SizeEntity(name = "L", sortOrder = 3),
    SizeEntity(name = "XL", sortOrder = 4),
    SizeEntity(name = "XXL", sortOrder = 5),
    SizeEntity(name = "XXXL", sortOrder = 6),

    // 鞋码系列（欧洲码）
    SizeEntity(name = "34", sortOrder = 7),
    SizeEntity(name = "35", sortOrder = 8),
    SizeEntity(name = "36", sortOrder = 9),
    SizeEntity(name = "37", sortOrder = 10),
    SizeEntity(name = "38", sortOrder = 11),
    SizeEntity(name = "39", sortOrder = 12),
    SizeEntity(name = "40", sortOrder = 13),
    SizeEntity(name = "41", sortOrder = 14),
    SizeEntity(name = "42", sortOrder = 15),
    SizeEntity(name = "43", sortOrder = 16),
    SizeEntity(name = "44", sortOrder = 17),
    SizeEntity(name = "45", sortOrder = 18),

    // 中文尺码系列
    SizeEntity(name = "小码", sortOrder = 19),
    SizeEntity(name = "中码", sortOrder = 20),
    SizeEntity(name = "大码", sortOrder = 21),
    SizeEntity(name = "超大", sortOrder = 22),
)