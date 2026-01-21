package com.fanda.homebook.data.category

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun initializeDatabase()
    suspend fun getItemCount(): Int
    suspend fun getSubItemCount(): Int
    suspend fun insertAllItems(list: List<CategoryEntity>)
    suspend fun insertAllSubItems(list: List<SubCategoryEntity>)
    suspend fun insertItem(entity: CategoryEntity): Long
    suspend fun insertSubItem(entity: SubCategoryEntity): Long
    suspend fun updateItem(entity: CategoryEntity): Int
    suspend fun updateSubItem(entity: SubCategoryEntity): Int
    suspend fun deleteItem(entity: CategoryEntity): Int
    suspend fun deleteSubItem(entity: SubCategoryEntity): Int
    fun getItems(): Flow<List<CategoryEntity>>
    fun getItemById(id: Int): Flow<CategoryEntity>
    fun getSubItems(): Flow<List<SubCategoryEntity>>
    fun getSubItemsById(id: Int): Flow<List<SubCategoryEntity>>
    fun getSubItemById(id: Int): Flow<SubCategoryEntity>
    suspend fun getItemByName(name: String): CategoryEntity?
    suspend fun getSubItemByName(name: String): SubCategoryEntity?
    suspend fun getItemMaxSortOrder(): Int?
    suspend fun getSubItemMaxSortOrder(): Int?
    suspend fun updateItemsSortOrders(list: List<CategoryEntity>): Int
    suspend fun updateSubItemsSortOrders(list: List<SubCategoryEntity>): Int
    fun getAllItemsWithSub(): Flow<List<CategoryWithSubCategories>>
    suspend fun insertItemWithAutoOrder(entity: CategoryEntity): Long
    suspend fun insertSubItemWithAutoOrder(entity: SubCategoryEntity): Long
}

class LocalCategoryRepository(private val categoryDao: CategoryDao) : CategoryRepository {
    override suspend fun initializeDatabase() {
        if (categoryDao.getItemCount() == 0) {
            // 先批量插入父级分类，先自动生成ID
            categoryDao.resetToDefault(defaultCategoryData)
            // 再查询父级分类，拿到对应ID进行子分类的处理
            val items = categoryDao.getItems()
            insertAllDefaultSubItems(items)
        }
    }

    override suspend fun getItemCount() = categoryDao.getItemCount()

    override suspend fun getSubItemCount() = categoryDao.getSubItemCount()

    override suspend fun insertAllItems(list: List<CategoryEntity>) = categoryDao.insertAllItems(list)

    override suspend fun insertAllSubItems(list: List<SubCategoryEntity>) = categoryDao.insertAllSubItems(list)

    override suspend fun insertItem(entity: CategoryEntity) = categoryDao.insertItem(entity)

    override suspend fun insertSubItem(entity: SubCategoryEntity) = categoryDao.insertSubItem(entity)

    override suspend fun updateItem(entity: CategoryEntity) = categoryDao.updateItem(entity)

    override suspend fun updateSubItem(entity: SubCategoryEntity) = categoryDao.updateSubItem(entity)

    override suspend fun deleteItem(entity: CategoryEntity) = categoryDao.deleteItem(entity)

    override suspend fun deleteSubItem(entity: SubCategoryEntity) = categoryDao.deleteSubItem(entity)

    override fun getItems(): Flow<List<CategoryEntity>> = categoryDao.getItemsStream()

    override fun getItemById(id: Int): Flow<CategoryEntity> = categoryDao.getItemById(id)

    override fun getSubItems(): Flow<List<SubCategoryEntity>> = categoryDao.getSubItems()

    override fun getSubItemsById(id: Int): Flow<List<SubCategoryEntity>> = categoryDao.getSubItemsById(id)
    override fun getSubItemById(id: Int) = categoryDao.getSubItemById(id)

    override suspend fun getItemByName(name: String): CategoryEntity? = categoryDao.getItemByName(name)

    override suspend fun getSubItemByName(name: String): SubCategoryEntity? = categoryDao.getSubItemByName(name)

    override suspend fun getItemMaxSortOrder() = categoryDao.getItemMaxSortOrder()

    override suspend fun getSubItemMaxSortOrder() = categoryDao.getSubItemMaxSortOrder()

    override suspend fun updateItemsSortOrders(list: List<CategoryEntity>) = categoryDao.updateItemsSortOrders(list)

    override suspend fun updateSubItemsSortOrders(list: List<SubCategoryEntity>) = categoryDao.updateSubItemsSortOrders(list)

    override fun getAllItemsWithSub(): Flow<List<CategoryWithSubCategories>> = categoryDao.getAllItemsWithSub()

    override suspend fun insertItemWithAutoOrder(entity: CategoryEntity) = categoryDao.insertItemWithAutoOrder(entity)

    override suspend fun insertSubItemWithAutoOrder(entity: SubCategoryEntity) = categoryDao.insertSubItemWithAutoOrder(entity)


    private suspend fun insertAllDefaultSubItems(items: List<CategoryEntity>) {
        val subItems = mutableListOf<SubCategoryEntity>()
        items.forEach { item ->
            when (item.name) {
                "上装" -> {
                    subItems.add(SubCategoryEntity(name = "打底", categoryId = item.id, sortOrder = 0))
                    subItems.add(SubCategoryEntity(name = "毛衣", categoryId = item.id, sortOrder = 1))
                    subItems.add(SubCategoryEntity(name = "T恤", categoryId = item.id, sortOrder = 2))
                    subItems.add(SubCategoryEntity(name = "卫衣", categoryId = item.id, sortOrder = 3))
                    subItems.add(SubCategoryEntity(name = "外套", categoryId = item.id, sortOrder = 4))
                    subItems.add(SubCategoryEntity(name = "开衫", categoryId = item.id, sortOrder = 5))
                    subItems.add(SubCategoryEntity(name = "大衣", categoryId = item.id, sortOrder = 6))
                    subItems.add(SubCategoryEntity(name = "羽绒服", categoryId = item.id, sortOrder = 7))
                }

                "下装" -> {
                    subItems.add(SubCategoryEntity(name = "休闲裤", categoryId = item.id, sortOrder = 0))
                    subItems.add(SubCategoryEntity(name = "牛仔裤", categoryId = item.id, sortOrder = 1))
                    subItems.add(SubCategoryEntity(name = "运动裤", categoryId = item.id, sortOrder = 2))
                    subItems.add(SubCategoryEntity(name = "打底裤", categoryId = item.id, sortOrder = 3))
                    subItems.add(SubCategoryEntity(name = "半身裙", categoryId = item.id, sortOrder = 4))
                    subItems.add(SubCategoryEntity(name = "短裤", categoryId = item.id, sortOrder = 5))
                    subItems.add(SubCategoryEntity(name = "短裙", categoryId = item.id, sortOrder = 6))
                    subItems.add(SubCategoryEntity(name = "西装裤", categoryId = item.id, sortOrder = 7))
                }

                "鞋靴" -> {
                    subItems.add(SubCategoryEntity(name = "皮鞋", categoryId = item.id, sortOrder = 0))
                    subItems.add(SubCategoryEntity(name = "帆布鞋", categoryId = item.id, sortOrder = 1))
                    subItems.add(SubCategoryEntity(name = "单鞋", categoryId = item.id, sortOrder = 2))
                    subItems.add(SubCategoryEntity(name = "短靴", categoryId = item.id, sortOrder = 3))
                    subItems.add(SubCategoryEntity(name = "长筒靴", categoryId = item.id, sortOrder = 4))
                }

                "包包" -> {
                    subItems.add(SubCategoryEntity(name = "钱包", categoryId = item.id, sortOrder = 0))
                    subItems.add(SubCategoryEntity(name = "斜挎包", categoryId = item.id, sortOrder = 1))
                    subItems.add(SubCategoryEntity(name = "背包", categoryId = item.id, sortOrder = 2))
                    subItems.add(SubCategoryEntity(name = "妈妈包", categoryId = item.id, sortOrder = 3))
                    subItems.add(SubCategoryEntity(name = "手提包", categoryId = item.id, sortOrder = 4))
                    subItems.add(SubCategoryEntity(name = "箱包", categoryId = item.id, sortOrder = 5))
                }

                "配饰" -> {
                    subItems.add(SubCategoryEntity(name = "帽子", categoryId = item.id, sortOrder = 0))
                    subItems.add(SubCategoryEntity(name = "围巾", categoryId = item.id, sortOrder = 1))
                    subItems.add(SubCategoryEntity(name = "项链", categoryId = item.id, sortOrder = 2))
                    subItems.add(SubCategoryEntity(name = "眼镜", categoryId = item.id, sortOrder = 3))
                    subItems.add(SubCategoryEntity(name = "手表", categoryId = item.id, sortOrder = 4))
                }
            }
        }
        categoryDao.insertAllSubItems(subItems)
    }
}

val defaultCategoryData = listOf(
    CategoryEntity(name = "上装", sortOrder = 0),
    CategoryEntity(name = "下装", sortOrder = 1),
    CategoryEntity(name = "鞋靴", sortOrder = 2),
    CategoryEntity(name = "包包", sortOrder = 3),
    CategoryEntity(name = "配饰", sortOrder = 4),
)

