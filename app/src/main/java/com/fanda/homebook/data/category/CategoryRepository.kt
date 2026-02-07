package com.fanda.homebook.data.category

import kotlinx.coroutines.flow.Flow

/**
 * 分类仓库接口
 * 定义分类和子分类数据的操作契约，提供数据访问的抽象层
 */
interface CategoryRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 统计相关方法
    suspend fun getItemCount(): Int
    suspend fun getSubItemCount(): Int

    // 批量操作方法
    suspend fun insertAllItems(list: List<CategoryEntity>)
    suspend fun insertAllSubItems(list: List<SubCategoryEntity>)

    // 单条操作方法
    suspend fun insertItem(entity: CategoryEntity): Long
    suspend fun insertSubItem(entity: SubCategoryEntity): Long
    suspend fun updateItem(entity: CategoryEntity): Int
    suspend fun updateSubItem(entity: SubCategoryEntity): Int
    suspend fun deleteItem(entity: CategoryEntity): Int
    suspend fun deleteSubItem(entity: SubCategoryEntity): Int

    // 查询方法（Flow版本，支持实时更新）
    fun getItems(): Flow<List<CategoryEntity>>
    fun getItemById(id: Int): Flow<CategoryEntity>
    fun getSubItems(): Flow<List<SubCategoryEntity>>
    fun getSubItemsById(id: Int): Flow<List<SubCategoryEntity>>
    fun getSubItemById(id: Int): Flow<SubCategoryEntity>

    // 查询方法（挂起函数版本）
    suspend fun getItemByName(name: String): CategoryEntity?
    suspend fun getSubItemByName(name: String): SubCategoryEntity?

    // 排序相关方法
    suspend fun getItemMaxSortOrder(): Int?
    suspend fun getSubItemMaxSortOrder(): Int?
    suspend fun updateItemsSortOrders(list: List<CategoryEntity>): Int
    suspend fun updateSubItemsSortOrders(list: List<SubCategoryEntity>): Int

    // 关联查询方法
    fun getAllItemsWithSub(): Flow<List<CategoryWithSubCategories>>

    // 自动排序插入方法
    suspend fun insertItemWithAutoOrder(entity: CategoryEntity): Long
    suspend fun insertSubItemWithAutoOrder(entity: SubCategoryEntity): Long
}

/**
 * 本地分类仓库实现类
 * 实现CategoryRepository接口，封装对Room数据库的直接访问
 *
 * @property categoryDao 分类数据访问对象
 */
class LocalCategoryRepository(private val categoryDao: CategoryDao) : CategoryRepository {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认的主分类和子分类数据
     */
    override suspend fun initializeDatabase() {
        if (categoryDao.getItemCount() == 0) {
            // 先批量插入父级分类，让数据库自动生成ID
            categoryDao.resetToDefault(defaultCategoryData)

            // 再查询父级分类，拿到对应的ID用于创建子分类
            val items = categoryDao.getItems()

            // 插入默认的子分类数据
            insertAllDefaultSubItems(items)
        }
    }

    // 以下方法都是直接委托给categoryDao对应的方法
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

    /**
     * 插入所有默认的子分类数据
     * 根据主分类的名称，为每个主分类创建相应的子分类
     *
     * @param items 已插入的主分类列表
     */
    private suspend fun insertAllDefaultSubItems(items: List<CategoryEntity>) {
        val subItems = mutableListOf<SubCategoryEntity>()

        // 遍历所有主分类，根据分类名称创建对应的子分类
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

        // 批量插入所有子分类
        categoryDao.insertAllSubItems(subItems)
    }
}

/**
 * 默认主分类数据
 * 应用首次安装或数据库重置时使用的默认主分类列表
 * 按sortOrder排序，数值越小越靠前
 */
val defaultCategoryData = listOf(
    CategoryEntity(name = "上装", sortOrder = 0),
    CategoryEntity(name = "下装", sortOrder = 1),
    CategoryEntity(name = "鞋靴", sortOrder = 2),
    CategoryEntity(name = "包包", sortOrder = 3),
    CategoryEntity(name = "配饰", sortOrder = 4),
)