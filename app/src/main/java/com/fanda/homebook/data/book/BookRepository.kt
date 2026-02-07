package com.fanda.homebook.data.book

import kotlinx.coroutines.flow.Flow

/**
 * 账本仓库接口
 * 定义账本数据的操作契约，提供数据访问的抽象层
 */
interface BookRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 插入账本并自动设置排序（放在最后）
    suspend fun insertWithAutoOrder(entity: BookEntity): Long

    // 获取账本总数
    suspend fun getCount(): Int

    // 重置数据库为默认数据
    suspend fun resetToDefault(list: List<BookEntity>)

    // 插入账本
    suspend fun insert(entity: BookEntity): Long

    // 更新账本
    suspend fun update(entity: BookEntity): Int

    // 删除账本
    suspend fun delete(entity: BookEntity): Int

    // 根据ID删除账本
    suspend fun deleteById(id: Int): Int

    // 获取所有账本列表（Flow版本，支持实时更新）
    fun getItems(): Flow<List<BookEntity>>

    // 根据ID获取单个账本（Flow版本，支持实时更新）
    fun getItemById(id: Int): Flow<BookEntity>

    // 根据名称获取账本
    suspend fun getItemByName(name: String): BookEntity?

    // 获取最大排序值
    suspend fun getMaxSortOrder(): Int?

    // 批量更新排序字段
    suspend fun updateSortOrders(list: List<BookEntity>): Int
}

/**
 * 本地账本仓库实现类
 * 实现BookRepository接口，封装对Room数据库的直接访问
 *
 * @property bookDao 账本数据访问对象
 */
class LocalBookRepository(private val bookDao: BookDao) : BookRepository {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认数据
     */
    override suspend fun initializeDatabase() {
        if (bookDao.getCount() == 0) {
            resetToDefault(defaultBookData)
        }
    }

    // 以下方法都是直接委托给bookDao对应的方法

    override suspend fun insertWithAutoOrder(entity: BookEntity) = bookDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = bookDao.getCount()
    override suspend fun resetToDefault(list: List<BookEntity>) = bookDao.resetToDefault(list)
    override suspend fun insert(entity: BookEntity) = bookDao.insert(entity)
    override suspend fun update(entity: BookEntity) = bookDao.update(entity)
    override suspend fun delete(entity: BookEntity) = bookDao.delete(entity)
    override suspend fun deleteById(id: Int) = bookDao.deleteById(id)
    override fun getItems(): Flow<List<BookEntity>> = bookDao.getItems()
    override fun getItemById(id: Int): Flow<BookEntity> = bookDao.getItemById(id)
    override suspend fun getItemByName(name: String) = bookDao.getItemByName(name)
    override suspend fun getMaxSortOrder() = bookDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<BookEntity>) = bookDao.updateSortOrders(list)
}

/**
 * 默认账本数据
 * 应用首次安装或数据库重置时使用的默认账本列表
 */
val defaultBookData = listOf(
    BookEntity(name = "居家生活"),  // 默认创建一个名为"居家生活"的账本
)