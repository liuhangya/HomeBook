package com.fanda.homebook.data.rack

import kotlinx.coroutines.flow.Flow

/**
 * 货架仓库接口
 * 定义货架数据的操作契约，提供数据访问的抽象层
 */
interface RackRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 获取货架总数
    suspend fun getItemCount(): Int

    // 根据ID获取货架（Flow版本，支持实时更新）
    fun getItemById(id: Int): Flow<RackEntity>

    // 根据ID获取货架子分类（Flow版本，支持实时更新）
    fun getSubItemById(id: Int): Flow<RackSubCategoryEntity>

    // 更新货架数据
    suspend fun updateItem(item: RackEntity): Int

    // 更新货架子分类数据
    suspend fun updateSubItem(item: RackSubCategoryEntity): Int

    // 获取所有货架列表（挂起函数版本）
    suspend fun getItems(): List<RackEntity>

    // 批量插入货架数据
    suspend fun insertAllItems(list: List<RackEntity>)

    // 批量插入货架子分类数据
    suspend fun insertAllSubItems(list: List<RackSubCategoryEntity>)

    // 获取所有货架及其对应的子分类（Flow版本，支持实时更新）
    fun getAllItemsWithSub(): Flow<List<RackWithSubCategories>>

    // 根据货架ID获取所有子分类列表（Flow版本，支持实时更新）
    fun getAllSubItemsById(rackId: Int): Flow<List<RackSubCategoryEntity>>

    // 获取当前选中的货架（Flow版本，支持实时更新）
    fun getSelectedItem(): Flow<RackEntity?>
}

/**
 * 本地货架仓库实现类
 * 实现RackRepository接口，封装对Room数据库的直接访问
 *
 * @property rackDao 货架数据访问对象
 */
class LocalRackRepository(private val rackDao: RackDao) : RackRepository {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认的货架和货架子分类数据
     */
    override suspend fun initializeDatabase() {
        if (rackDao.getItemCount() == 0) {
            // 先批量插入主货架数据，让数据库自动生成ID
            rackDao.resetToDefault(defaultRackData)

            // 再查询主货架，拿到对应的ID用于创建子分类
            val items = rackDao.getItems()

            // 插入默认的货架子分类数据
            insertAllDefaultSubItems(items)
        }
    }

    // 以下方法都是直接委托给rackDao对应的方法
    override suspend fun getItemCount() = rackDao.getItemCount()
    override fun getItemById(id: Int): Flow<RackEntity> = rackDao.getItemById(id)
    override fun getSubItemById(id: Int): Flow<RackSubCategoryEntity> = rackDao.getSubItemById(id)
    override suspend fun updateItem(item: RackEntity) = rackDao.updateItem(item)
    override suspend fun updateSubItem(item: RackSubCategoryEntity) = rackDao.updateSubItem(item)
    override suspend fun getItems(): List<RackEntity> = rackDao.getItems()
    override suspend fun insertAllItems(list: List<RackEntity>) = rackDao.insertAllItems(list)
    override suspend fun insertAllSubItems(list: List<RackSubCategoryEntity>) = rackDao.insertAllSubItems(list)
    override fun getAllItemsWithSub(): Flow<List<RackWithSubCategories>> = rackDao.getAllItemsWithSub()
    override fun getAllSubItemsById(rackId: Int): Flow<List<RackSubCategoryEntity>> = rackDao.getAllSubItemsById(rackId)
    override fun getSelectedItem(): Flow<RackEntity?> = rackDao.getSelectedItem()

    /**
     * 插入所有默认的货架子分类数据
     * 根据货架的名称，为每个货架创建相应的子分类
     *
     * @param items 已插入的主货架列表
     */
    private suspend fun insertAllDefaultSubItems(items: List<RackEntity>) {
        val subItems = mutableListOf<RackSubCategoryEntity>()

        // 遍历所有主货架，根据货架名称创建对应的子分类
        items.forEach { item ->
            when (item.name) {
                "梳妆台" -> {
                    // 化妆品/护肤品分类
                    subItems.add(RackSubCategoryEntity(name = "洁面", rackId = item.id, sortOrder = 0))
                    subItems.add(RackSubCategoryEntity(name = "水", rackId = item.id, sortOrder = 1))
                    subItems.add(RackSubCategoryEntity(name = "乳液", rackId = item.id, sortOrder = 2))
                    subItems.add(RackSubCategoryEntity(name = "精华", rackId = item.id, sortOrder = 3))
                    subItems.add(RackSubCategoryEntity(name = "面霜", rackId = item.id, sortOrder = 4))
                    subItems.add(RackSubCategoryEntity(name = "眼霜", rackId = item.id, sortOrder = 5))
                    subItems.add(RackSubCategoryEntity(name = "面膜", rackId = item.id, sortOrder = 6))
                    subItems.add(RackSubCategoryEntity(name = "防晒", rackId = item.id, sortOrder = 7))
                    subItems.add(RackSubCategoryEntity(name = "底妆", rackId = item.id, sortOrder = 8))
                    subItems.add(RackSubCategoryEntity(name = "唇膏", rackId = item.id, sortOrder = 9))
                    subItems.add(RackSubCategoryEntity(name = "眼影", rackId = item.id, sortOrder = 10))
                    subItems.add(RackSubCategoryEntity(name = "润肤", rackId = item.id, sortOrder = 11))
                }

                "米面粮油" -> {
                    // 食品分类
                    subItems.add(RackSubCategoryEntity(name = "零食", rackId = item.id, sortOrder = 0))
                    subItems.add(RackSubCategoryEntity(name = "主食", rackId = item.id, sortOrder = 1))
                    subItems.add(RackSubCategoryEntity(name = "调料", rackId = item.id, sortOrder = 2))
                }

                "日用百货" -> {
                    // 日用品分类
                    subItems.add(RackSubCategoryEntity(name = "纸巾", rackId = item.id, sortOrder = 0))
                    subItems.add(RackSubCategoryEntity(name = "面巾", rackId = item.id, sortOrder = 1))
                    subItems.add(RackSubCategoryEntity(name = "洗头", rackId = item.id, sortOrder = 2))
                    subItems.add(RackSubCategoryEntity(name = "洗澡", rackId = item.id, sortOrder = 3))
                    subItems.add(RackSubCategoryEntity(name = "洗衣", rackId = item.id, sortOrder = 4))
                    subItems.add(RackSubCategoryEntity(name = "护发", rackId = item.id, sortOrder = 5))
                    subItems.add(RackSubCategoryEntity(name = "片剂", rackId = item.id, sortOrder = 6))
                }

                "圆圆" -> {
                    // 个人专属分类（可能是特定用户的物品）
                    subItems.add(RackSubCategoryEntity(name = "护肤", rackId = item.id, sortOrder = 0))
                    subItems.add(RackSubCategoryEntity(name = "补剂", rackId = item.id, sortOrder = 1))
                }

                "其他囤货" -> {
                    // 其他未分类的囤货，暂时没有子分类
                }
            }
        }

        // 批量插入所有货架子分类
        rackDao.insertAllSubItems(subItems)
    }
}

/**
 * 默认货架数据
 * 应用首次安装或数据库重置时使用的默认货架列表
 * 按sortOrder排序，数值越小越靠前
 */
val defaultRackData = listOf(
    RackEntity(name = "梳妆台", sortOrder = 0, selected = true),  // 默认选中"梳妆台"作为当前活跃货架
    RackEntity(name = "米面粮油", sortOrder = 1),
    RackEntity(name = "日用百货", sortOrder = 2),
    RackEntity(name = "圆圆", sortOrder = 3),
    // 注释掉的货架，可根据需要启用
    // RackEntity(name = "其他囤货", sortOrder = 4),
)