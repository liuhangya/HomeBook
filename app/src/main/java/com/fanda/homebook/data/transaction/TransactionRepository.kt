package com.fanda.homebook.data.transaction

import com.fanda.homebook.data.category.CategoryDao
import com.fanda.homebook.data.category.CategoryWithSubCategories
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun initializeDatabase()
    suspend fun getItemCount(): Int
    suspend fun getSubItemCount(): Int
    suspend fun insertAllItems(list: List<TransactionEntity>)
    suspend fun insertAllSubItems(list: List<TransactionSubEntity>)
    suspend fun insertItem(entity: TransactionEntity): Long
    suspend fun insertSubItem(entity: TransactionSubEntity): Long
    suspend fun updateItem(entity: TransactionEntity): Int
    suspend fun updateSubItem(entity: TransactionSubEntity): Int
    suspend fun deleteItem(entity: TransactionEntity): Int
    suspend fun deleteSubItem(entity: TransactionSubEntity): Int
    fun getItems(): Flow<List<TransactionEntity>>
    fun getItemById(id: Int): Flow<TransactionEntity>
    fun getSubItems(): Flow<List<TransactionSubEntity>>
    fun getSubItemsById(id: Int): Flow<List<TransactionSubEntity>>
    fun getSubItemById(id: Int): Flow<TransactionSubEntity>
    suspend fun getItemByName(name: String): TransactionEntity?
    suspend fun getSubItemByName(name: String): TransactionSubEntity?
    suspend fun getSubItemMaxSortOrder(): Int?
    suspend fun updateItemsSortOrders(list: List<TransactionEntity>): Int
    suspend fun updateSubItemsSortOrders(list: List<TransactionSubEntity>): Int
    fun getAllItemsWithSub(): Flow<List<TransactionWithSubCategories>>
}

class LocalTransactionRepository(private val transactionDao: TransactionDao) : TransactionRepository {
    override suspend fun initializeDatabase() {
        if (transactionDao.getItemCount() == 0) {
            // 先批量插入父级分类，先自动生成ID
            transactionDao.resetToDefault(defaultCategoryData)
            // 再查询父级分类，拿到对应ID进行子分类的处理
            val items = transactionDao.getItems()
            insertAllDefaultSubItems(items)
        }
    }

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

    override suspend fun getSubItemByName(name: String): TransactionSubEntity? = transactionDao.getSubItemByName(name)

    override suspend fun getSubItemMaxSortOrder() = transactionDao.getSubItemMaxSortOrder()

    override suspend fun updateItemsSortOrders(list: List<TransactionEntity>) = transactionDao.updateItemsSortOrders(list)

    override suspend fun updateSubItemsSortOrders(list: List<TransactionSubEntity>) = transactionDao.updateSubItemsSortOrders(list)

    override fun getAllItemsWithSub(): Flow<List<TransactionWithSubCategories>> = transactionDao.getAllItemsWithSub()

    private suspend fun insertAllDefaultSubItems(items: List<TransactionEntity>) {
        val subItems = mutableListOf<TransactionSubEntity>()
        items.forEach { item ->
            when (item.name) {
                "支出" -> {
                    subItems.add(TransactionSubEntity(name ="餐饮", categoryId = item.id, sortOrder = 0, type = TransactionType.DINING.type))
                    subItems.add(TransactionSubEntity(name ="交通", categoryId = item.id, sortOrder = 1, type = TransactionType.TRAFFIC.type ))
                    subItems.add(TransactionSubEntity(name ="服饰", categoryId = item.id, sortOrder = 2, type = TransactionType.CLOTHING.type))
                    subItems.add(TransactionSubEntity(name ="护肤", categoryId = item.id, sortOrder = 3, type = TransactionType.SKINCARE.type))
                    subItems.add(TransactionSubEntity(name ="购物", categoryId = item.id, sortOrder = 4, type = TransactionType.SHOPPING.type))
                    subItems.add(TransactionSubEntity(name ="服务", categoryId = item.id, sortOrder = 5, type = TransactionType.SERVICES.type))
                    subItems.add(TransactionSubEntity(name ="医疗", categoryId = item.id, sortOrder = 6, type = TransactionType.HEALTH.type))
                    subItems.add(TransactionSubEntity(name ="娱乐",  categoryId = item.id, sortOrder = 7, type = TransactionType.PLAY.type))
                    subItems.add(TransactionSubEntity(name ="生活",  categoryId = item.id, sortOrder = 8, type = TransactionType.DAILY.type))
                    subItems.add(TransactionSubEntity(name ="旅行",  categoryId = item.id, sortOrder = 9, type = TransactionType.TRAVEL.type))
                    subItems.add(TransactionSubEntity(name ="保险",  categoryId = item.id, sortOrder = 10, type = TransactionType.INSURANCE.type))
                    subItems.add(TransactionSubEntity(name ="发红包", categoryId = item.id, sortOrder = 11, type = TransactionType.RED_ENVELOPE.type))
                    subItems.add(TransactionSubEntity(name ="人情",  categoryId = item.id, sortOrder = 12, type = TransactionType.SOCIAL.type))
                    subItems.add(TransactionSubEntity(name ="其他",  categoryId = item.id, sortOrder = 13, type = TransactionType.OTHERS.type))
                }

                "入账" -> {
                    subItems.add(TransactionSubEntity(name = "工资",  categoryId = item.id, sortOrder = 0, type = TransactionType.SALARY.type))
                    subItems.add(TransactionSubEntity(name = "收红包", categoryId = item.id, sortOrder = 1, type = TransactionType.GET_ENVELOPE.type))
                    subItems.add(TransactionSubEntity(name = "人情",  categoryId = item.id, sortOrder = 2, type = TransactionType.SOCIAL.type))
                    subItems.add(TransactionSubEntity(name = "奖金",  categoryId = item.id, sortOrder = 3, type = TransactionType.BONUS.type))
                    subItems.add(TransactionSubEntity(name = "其他",  categoryId = item.id, sortOrder = 4, type = TransactionType.OTHERS.type))
                }

                "不计入收支" -> {
                    subItems.add(TransactionSubEntity(name = "理财", categoryId = item.id, sortOrder = 0, type = TransactionType.FINANCE.type))
                    subItems.add(TransactionSubEntity(name = "借还款", categoryId = item.id, sortOrder = 1, type = TransactionType.DEBTS.type))
                    subItems.add(TransactionSubEntity(name = "其他", categoryId = item.id, sortOrder = 2, type = TransactionType.OTHERS.type))
                }
            }
        }
        transactionDao.insertAllSubItems(subItems)
    }
}

val defaultCategoryData = listOf(
    TransactionEntity(name = "支出"),
    TransactionEntity(name = "入账"),
    TransactionEntity(name = "不计入收支"),
)

