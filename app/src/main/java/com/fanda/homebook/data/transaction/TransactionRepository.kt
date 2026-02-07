package com.fanda.homebook.data.transaction

import com.fanda.homebook.common.entity.TransactionAmountType
import kotlinx.coroutines.flow.Flow

/**
 * 交易分类仓库接口
 * 定义交易分类数据的操作契约，提供数据访问的抽象层
 */
interface TransactionRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 统计相关方法
    suspend fun getItemCount(): Int
    suspend fun getSubItemCount(): Int

    // 批量操作方法
    suspend fun insertAllItems(list: List<TransactionEntity>)
    suspend fun insertAllSubItems(list: List<TransactionSubEntity>)

    // 单条操作方法
    suspend fun insertItem(entity: TransactionEntity): Long
    suspend fun insertSubItem(entity: TransactionSubEntity): Long
    suspend fun updateItem(entity: TransactionEntity): Int
    suspend fun updateSubItem(entity: TransactionSubEntity): Int
    suspend fun deleteItem(entity: TransactionEntity): Int
    suspend fun deleteSubItem(entity: TransactionSubEntity): Int

    // 查询方法（Flow版本，支持实时更新）
    fun getItems(): Flow<List<TransactionEntity>>
    fun getItemById(id: Int): Flow<TransactionEntity>
    fun getSubItems(): Flow<List<TransactionSubEntity>>
    fun getSubItemsById(id: Int): Flow<List<TransactionSubEntity>>
    fun getSubItemById(id: Int): Flow<TransactionSubEntity>

    // 查询方法（挂起函数版本）
    suspend fun getItemByName(name: String): TransactionEntity?
    suspend fun getSubItemByName(name: String, parentId: Int): TransactionSubEntity?

    // 排序相关方法
    suspend fun getSubItemMaxSortOrder(): Int?
    suspend fun updateItemsSortOrders(list: List<TransactionEntity>): Int
    suspend fun updateSubItemsSortOrders(list: List<TransactionSubEntity>): Int

    // 关联查询方法
    fun getAllItemsWithSub(): Flow<List<TransactionWithSubCategories>>

    // 自动排序插入方法
    suspend fun insertSubItemWithAutoOrder(entity: TransactionSubEntity): Long
}

/**
 * 本地交易分类仓库实现类
 * 实现TransactionRepository接口，封装对Room数据库的直接访问
 *
 * @property transactionDao 交易分类数据访问对象
 */
class LocalTransactionRepository(private val transactionDao: TransactionDao) : TransactionRepository {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认的交易分类和子分类数据
     */
    override suspend fun initializeDatabase() {
        if (transactionDao.getItemCount() == 0) {
            // 先批量插入父级分类，让数据库自动生成ID
            transactionDao.resetToDefault(defaultCategoryData)

            // 再查询父级分类，拿到对应的ID用于创建子分类
            val items = transactionDao.getItems()

            // 插入默认的交易子分类数据
            insertAllDefaultSubItems(items)
        }
    }

    // 以下方法都是直接委托给transactionDao对应的方法
    override suspend fun getItemCount() = transactionDao.getItemCount()
    override suspend fun getSubItemCount() = transactionDao.getSubItemCount()
    override suspend fun insertAllItems(list: List<TransactionEntity>) = transactionDao.insertAllItems(list)
    override suspend fun insertAllSubItems(list: List<TransactionSubEntity>) = transactionDao.insertAllSubItems(list)
    override suspend fun insertItem(entity: TransactionEntity) = transactionDao.insertItem(entity)
    override suspend fun insertSubItem(entity: TransactionSubEntity) = transactionDao.insertSubItem(entity)
    override suspend fun updateItem(entity: TransactionEntity) = transactionDao.updateItem(entity)
    override suspend fun updateSubItem(entity: TransactionSubEntity) = transactionDao.updateSubItem(entity)
    override suspend fun deleteItem(entity: TransactionEntity) = transactionDao.deleteItem(entity)
    override suspend fun deleteSubItem(entity: TransactionSubEntity) = transactionDao.deleteSubItem(entity)
    override fun getItems(): Flow<List<TransactionEntity>> = transactionDao.getItemsStream()
    override fun getItemById(id: Int): Flow<TransactionEntity> = transactionDao.getItemById(id)
    override fun getSubItems(): Flow<List<TransactionSubEntity>> = transactionDao.getSubItems()
    override fun getSubItemsById(id: Int): Flow<List<TransactionSubEntity>> = transactionDao.getSubItemsById(id)
    override fun getSubItemById(id: Int) = transactionDao.getSubItemById(id)
    override suspend fun getItemByName(name: String): TransactionEntity? = transactionDao.getItemByName(name)
    override suspend fun getSubItemByName(name: String, parentId: Int): TransactionSubEntity? = transactionDao.getSubItemByName(name, parentId)
    override suspend fun getSubItemMaxSortOrder() = transactionDao.getSubItemMaxSortOrder()
    override suspend fun updateItemsSortOrders(list: List<TransactionEntity>) = transactionDao.updateItemsSortOrders(list)
    override suspend fun updateSubItemsSortOrders(list: List<TransactionSubEntity>) = transactionDao.updateSubItemsSortOrders(list)
    override fun getAllItemsWithSub(): Flow<List<TransactionWithSubCategories>> = transactionDao.getAllItemsWithSub()
    override suspend fun insertSubItemWithAutoOrder(entity: TransactionSubEntity) = transactionDao.insertSubItemWithAutoOrder(entity)

    /**
     * 插入所有默认的交易子分类数据
     * 根据交易分类的类型（支出/收入/不计入收支），为每个分类创建相应的子分类
     *
     * @param items 已插入的交易主分类列表
     */
    private suspend fun insertAllDefaultSubItems(items: List<TransactionEntity>) {
        val subItems = mutableListOf<TransactionSubEntity>()

        // 遍历所有交易主分类，根据分类类型创建对应的子分类
        items.forEach { item ->
            when (item.type) {
                TransactionAmountType.EXPENSE.ordinal -> {
                    // 支出分类的子分类
                    subItems.add(TransactionSubEntity(name = "餐饮", categoryId = item.id, sortOrder = 0, type = TransactionType.DINING.type))
                    subItems.add(TransactionSubEntity(name = "交通", categoryId = item.id, sortOrder = 1, type = TransactionType.TRAFFIC.type))
                    subItems.add(TransactionSubEntity(name = "服饰", categoryId = item.id, sortOrder = 2, type = TransactionType.CLOTHING.type))
                    subItems.add(TransactionSubEntity(name = "护肤", categoryId = item.id, sortOrder = 3, type = TransactionType.SKINCARE.type))
                    subItems.add(TransactionSubEntity(name = "购物", categoryId = item.id, sortOrder = 4, type = TransactionType.SHOPPING.type))
                    subItems.add(TransactionSubEntity(name = "服务", categoryId = item.id, sortOrder = 5, type = TransactionType.SERVICES.type))
                    subItems.add(TransactionSubEntity(name = "医疗", categoryId = item.id, sortOrder = 6, type = TransactionType.HEALTH.type))
                    subItems.add(TransactionSubEntity(name = "娱乐", categoryId = item.id, sortOrder = 7, type = TransactionType.PLAY.type))
                    subItems.add(TransactionSubEntity(name = "生活", categoryId = item.id, sortOrder = 8, type = TransactionType.DAILY.type))
                    subItems.add(TransactionSubEntity(name = "旅行", categoryId = item.id, sortOrder = 9, type = TransactionType.TRAVEL.type))
                    subItems.add(TransactionSubEntity(name = "保险", categoryId = item.id, sortOrder = 10, type = TransactionType.INSURANCE.type))
                    subItems.add(TransactionSubEntity(name = "发红包", categoryId = item.id, sortOrder = 11, type = TransactionType.RED_ENVELOPE.type))
                    subItems.add(TransactionSubEntity(name = "人情", categoryId = item.id, sortOrder = 12, type = TransactionType.SOCIAL.type))
                    subItems.add(TransactionSubEntity(name = "其他", categoryId = item.id, sortOrder = 13, type = TransactionType.OTHERS.type))
                }

                TransactionAmountType.INCOME.ordinal -> {
                    // 收入分类的子分类
                    subItems.add(TransactionSubEntity(name = "工资", categoryId = item.id, sortOrder = 0, type = TransactionType.SALARY.type))
                    subItems.add(TransactionSubEntity(name = "收红包", categoryId = item.id, sortOrder = 1, type = TransactionType.GET_ENVELOPE.type))
                    subItems.add(TransactionSubEntity(name = "人情", categoryId = item.id, sortOrder = 2, type = TransactionType.SOCIAL.type))
                    subItems.add(TransactionSubEntity(name = "奖金", categoryId = item.id, sortOrder = 3, type = TransactionType.BONUS.type))
                    subItems.add(TransactionSubEntity(name = "其他", categoryId = item.id, sortOrder = 4, type = TransactionType.OTHERS.type))
                }

                TransactionAmountType.EXCLUDED.ordinal -> {
                    // 不计入收支分类的子分类
                    subItems.add(TransactionSubEntity(name = "理财", categoryId = item.id, sortOrder = 0, type = TransactionType.FINANCE.type))
                    subItems.add(TransactionSubEntity(name = "借还款", categoryId = item.id, sortOrder = 1, type = TransactionType.DEBTS.type))
                    subItems.add(TransactionSubEntity(name = "其他", categoryId = item.id, sortOrder = 2, type = TransactionType.OTHERS.type))
                }
            }
        }

        // 批量插入所有交易子分类
        transactionDao.insertAllSubItems(subItems)
    }
}

// 交易分类名称常量
const val TRANSACTION_INCOME = "入账"
const val TRANSACTION_EXPENSE = "支出"
const val TRANSACTION_EXCLUDED = "不计入收支"

/**
 * 默认交易分类数据
 * 应用首次安装或数据库重置时使用的默认交易分类列表
 * 包含3个基本交易分类：支出、入账、不计入收支
 */
val defaultCategoryData = listOf(
    TransactionEntity(name = TRANSACTION_EXPENSE, type = TransactionAmountType.EXPENSE.ordinal),
    TransactionEntity(name = TRANSACTION_INCOME, type = TransactionAmountType.INCOME.ordinal),
    TransactionEntity(name = TRANSACTION_EXCLUDED, type = TransactionAmountType.EXCLUDED.ordinal),
)