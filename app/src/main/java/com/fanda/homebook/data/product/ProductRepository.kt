package com.fanda.homebook.data.product

import kotlinx.coroutines.flow.Flow

/**
 * 产品仓库接口
 * 定义产品数据的操作契约，提供数据访问的抽象层
 */
interface ProductRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 插入产品并自动设置排序（放在最后）
    suspend fun insertWithAutoOrder(entity: ProductEntity): Long

    // 获取产品总数
    suspend fun getCount(): Int

    // 重置数据库为默认产品数据
    suspend fun resetToDefault(list: List<ProductEntity>)

    // 插入产品
    suspend fun insert(entity: ProductEntity): Long

    // 更新产品
    suspend fun update(entity: ProductEntity): Int

    // 删除产品
    suspend fun delete(entity: ProductEntity): Int

    // 根据ID删除产品
    suspend fun deleteById(id: Int): Int

    // 获取所有产品列表（Flow版本，支持实时更新）
    fun getItems(): Flow<List<ProductEntity>>

    // 根据ID获取单个产品（Flow版本，支持实时更新）
    fun getItemById(id: Int): Flow<ProductEntity>

    // 根据名称获取产品
    suspend fun getItemByName(name: String): ProductEntity?

    // 获取最大排序值
    suspend fun getMaxSortOrder(): Int?

    // 批量更新产品排序字段
    suspend fun updateSortOrders(list: List<ProductEntity>): Int
}

/**
 * 本地产品仓库实现类
 * 实现ProductRepository接口，封装对Room数据库的直接访问
 *
 * @property productDao 产品数据访问对象
 */
class LocalProductRepository(private val productDao: ProductDao) : ProductRepository {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认产品数据
     */
    override suspend fun initializeDatabase() {
        if (productDao.getCount() == 0) {
            resetToDefault(defaultProductData)
        }
    }

    // 以下方法都是直接委托给productDao对应的方法
    override suspend fun insertWithAutoOrder(entity: ProductEntity) = productDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = productDao.getCount()
    override suspend fun resetToDefault(list: List<ProductEntity>) = productDao.resetToDefault(list)
    override suspend fun insert(entity: ProductEntity) = productDao.insert(entity)
    override suspend fun update(entity: ProductEntity) = productDao.update(entity)
    override suspend fun delete(entity: ProductEntity) = productDao.delete(entity)
    override suspend fun deleteById(id: Int) = productDao.deleteById(id)
    override fun getItems(): Flow<List<ProductEntity>> = productDao.getItems()
    override fun getItemById(id: Int): Flow<ProductEntity> = productDao.getItemById(id)
    override suspend fun getItemByName(name: String) = productDao.getItemByName(name)
    override suspend fun getMaxSortOrder() = productDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<ProductEntity>) = productDao.updateSortOrders(list)
}

/**
 * 默认产品数据
 * 应用首次安装或数据库重置时使用的默认产品列表
 * 包含6个常见品牌/产品，按sortOrder排序
 */
val defaultProductData = listOf(
    ProductEntity(name = "安踏", sortOrder = 0),   // 安踏品牌
    ProductEntity(name = "阿里", sortOrder = 1),   // 阿里巴巴
    ProductEntity(name = "竿竿", sortOrder = 2),   // 竿竿（可能是一个特定品牌或店铺）
    ProductEntity(name = "耐克", sortOrder = 3),   // 耐克品牌
    ProductEntity(name = "山姆", sortOrder = 4),   // 山姆会员店
    ProductEntity(name = "其他", sortOrder = 5)    // 其他未分类产品
)