package com.fanda.homebook.data.owner

import kotlinx.coroutines.flow.Flow

/**
 * 所有者仓库接口
 * 定义所有者数据的操作契约，提供数据访问的抽象层
 */
interface OwnerRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 获取所有所有者列表（挂起函数版本）
    suspend fun getItems(): List<OwnerEntity>

    // 获取所有者总数
    suspend fun getCount(): Int

    // 根据ID获取所有者（Flow版本，支持实时更新）
    fun getItemById(id: Int): Flow<OwnerEntity?>

    // 批量更新所有者数据
    suspend fun updateItems(list: List<OwnerEntity>): Int

    // 获取当前选中的所有者（Flow版本，支持实时更新）
    fun getSelectedItem(): Flow<OwnerEntity?>

    // 更新单个所有者数据
    suspend fun updateItem(item: OwnerEntity): Int
}

/**
 * 本地所有者仓库实现类
 * 实现OwnerRepository接口，封装对Room数据库的直接访问
 *
 * @property ownerDao 所有者数据访问对象
 */
class LocalOwnerRepository(private val ownerDao: OwnerDao) : OwnerRepository {
    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认所有者数据
     */
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            ownerDao.resetToDefault(defaultOwnerData)
        }
    }

    /**
     * 获取所有所有者列表
     *
     * @return 所有者实体列表
     */
    override suspend fun getItems(): List<OwnerEntity> = ownerDao.getItems()

    /**
     * 获取所有者总数
     *
     * @return 所有者总数
     */
    override suspend fun getCount() = ownerDao.getCount()

    /**
     * 根据ID获取所有者
     *
     * @param id 所有者ID
     * @return Flow流，包含对应的所有者实体（可为空）
     */
    override fun getItemById(id: Int) = ownerDao.getItemById(id)

    /**
     * 批量更新所有者数据
     *
     * @param list 所有者实体列表
     * @return 受影响的行数
     */
    override suspend fun updateItems(list: List<OwnerEntity>) = ownerDao.updateItems(list)

    /**
     * 获取当前选中的所有者
     * 用于获取应用当前活跃用户/所有者的信息
     *
     * @return Flow流，包含当前选中的所有者实体（可为空）
     */
    override fun getSelectedItem() = ownerDao.getSelectedItem()

    /**
     * 更新单个所有者数据
     *
     * @param item 所有者实体
     * @return 受影响的行数
     */
    override suspend fun updateItem(item: OwnerEntity) = ownerDao.updateItem(item)
}

/**
 * 默认所有者数据
 * 应用首次安装或数据库重置时使用的默认所有者列表
 * 包含5个预设的所有者账户
 */
val defaultOwnerData = listOf(
    OwnerEntity(name = "番茄", selected = true),  // 默认选中"番茄"作为当前活跃用户
    OwnerEntity(name = "阿凡达"),                  // 其他所有者账户
    OwnerEntity(name = "圆圆"),
    OwnerEntity(name = "家庭"),                    // 可用于家庭公共物品
    OwnerEntity(name = "送人"),                    // 可用于标记将要送人的物品
)