package com.fanda.homebook.data.pay

import kotlinx.coroutines.flow.Flow

/**
 * 付款方式仓库接口
 * 定义付款方式数据的操作契约，提供数据访问的抽象层
 */
interface PayWayRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 插入付款方式并自动设置排序（放在最后）
    suspend fun insertWithAutoOrder(entity: PayWayEntity): Long

    // 获取付款方式总数
    suspend fun getCount(): Int

    // 重置数据库为默认付款方式数据
    suspend fun resetToDefault(list: List<PayWayEntity>)

    // 插入付款方式
    suspend fun insert(entity: PayWayEntity): Long

    // 更新付款方式
    suspend fun update(entity: PayWayEntity): Int

    // 删除付款方式
    suspend fun delete(entity: PayWayEntity): Int

    // 根据ID删除付款方式
    suspend fun deleteById(id: Int): Int

    // 获取所有付款方式列表（Flow版本，支持实时更新）
    fun getItems(): Flow<List<PayWayEntity>>

    // 根据ID获取单个付款方式（Flow版本，支持实时更新）
    fun getItemById(id: Int): Flow<PayWayEntity>

    // 根据名称获取付款方式
    suspend fun getItemByName(name: String): PayWayEntity?

    // 获取最大排序值
    suspend fun getMaxSortOrder(): Int?

    // 批量更新付款方式排序字段
    suspend fun updateSortOrders(list: List<PayWayEntity>): Int
}

/**
 * 本地付款方式仓库实现类
 * 实现PayWayRepository接口，封装对Room数据库的直接访问
 *
 * @property payWayDao 付款方式数据访问对象
 */
class LocalPayWayRepository(private val payWayDao: PayWayDao) : PayWayRepository {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认付款方式数据
     */
    override suspend fun initializeDatabase() {
        if (payWayDao.getCount() == 0) {
            resetToDefault(defaultPayWayData)
        }
    }

    // 以下方法都是直接委托给payWayDao对应的方法
    override suspend fun insertWithAutoOrder(entity: PayWayEntity) = payWayDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = payWayDao.getCount()
    override suspend fun resetToDefault(list: List<PayWayEntity>) = payWayDao.resetToDefault(list)
    override suspend fun insert(entity: PayWayEntity) = payWayDao.insert(entity)
    override suspend fun update(entity: PayWayEntity) = payWayDao.update(entity)
    override suspend fun delete(entity: PayWayEntity) = payWayDao.delete(entity)
    override suspend fun deleteById(id: Int) = payWayDao.deleteById(id)
    override fun getItems(): Flow<List<PayWayEntity>> = payWayDao.getItems()
    override fun getItemById(id: Int): Flow<PayWayEntity> = payWayDao.getItemById(id)
    override suspend fun getItemByName(name: String) = payWayDao.getItemByName(name)
    override suspend fun getMaxSortOrder() = payWayDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<PayWayEntity>) = payWayDao.updateSortOrders(list)
}

/**
 * 默认付款方式数据
 * 应用首次安装或数据库重置时使用的默认付款方式列表
 * 包含13种常见支付方式，按sortOrder排序
 */
val defaultPayWayData = listOf(
    PayWayEntity(name = "微信", sortOrder = 0),          // 微信支付
    PayWayEntity(name = "支付宝", sortOrder = 1),        // 支付宝
    PayWayEntity(name = "现金", sortOrder = 2),          // 现金支付
    PayWayEntity(name = "淘宝", sortOrder = 3),          // 淘宝支付
    PayWayEntity(name = "京东", sortOrder = 4),          // 京东支付
    PayWayEntity(name = "唯品会", sortOrder = 5),        // 唯品会支付
    PayWayEntity(name = "阿里", sortOrder = 6),          // 阿里系支付
    PayWayEntity(name = "小红书", sortOrder = 7),        // 小红书支付
    PayWayEntity(name = "拼多多", sortOrder = 8),        // 拼多多支付
    PayWayEntity(name = "云闪付", sortOrder = 9),        // 云闪付
    PayWayEntity(name = "银行卡", sortOrder = 10),       // 银行卡支付
    PayWayEntity(name = "信用卡", sortOrder = 11),       // 信用卡支付
    PayWayEntity(name = "医保", sortOrder = 12),         // 医保支付
)