package com.fanda.homebook.data.rack

import kotlinx.coroutines.flow.Flow

interface RackRepository {

    suspend fun initializeDatabase()

    suspend fun getItemCount(): Int

    fun getItemById(id: Int): Flow<RackEntity>

    fun getSubItemById(id: Int): Flow<RackSubCategoryEntity>

    suspend fun updateItem(item: RackEntity): Int

    suspend fun updateSubItem(item: RackSubCategoryEntity): Int

    suspend fun getItems(): List<RackEntity>

    suspend fun insertAllItems(list: List<RackEntity>)

    suspend fun insertAllSubItems(list: List<RackSubCategoryEntity>)

    fun getAllItemsWithSub(): Flow<List<RackWithSubCategories>>

    fun getAllSubItemsById(rackId: Int): Flow<List<RackSubCategoryEntity>>

    fun getSelectedItem(): Flow<RackEntity?>

}

class LocalRackRepository(private val rackDao: RackDao) : RackRepository {

    override suspend fun initializeDatabase() {
        if (rackDao.getItemCount() == 0) {
            // 先批量插入父级分类，先自动生成ID
            rackDao.resetToDefault(defaultRackData)
            // 再查询父级分类，拿到对应ID进行子分类的处理
            val items = rackDao.getItems()
            insertAllDefaultSubItems(items)
        }
    }

    override suspend fun getItemCount() = rackDao.getItemCount()
    override fun getItemById(id: Int): Flow<RackEntity>  = rackDao.getItemById(id)
    override fun getSubItemById(id: Int): Flow<RackSubCategoryEntity> = rackDao.getSubItemById(id)

    override suspend fun updateItem(item: RackEntity) = rackDao.updateItem(item)
    override suspend fun updateSubItem(item: RackSubCategoryEntity) = rackDao.updateSubItem(item)

    override suspend fun getItems(): List<RackEntity> = rackDao.getItems()

    override suspend fun insertAllItems(list: List<RackEntity>) = rackDao.insertAllItems(list)

    override suspend fun insertAllSubItems(list: List<RackSubCategoryEntity>) = rackDao.insertAllSubItems(list)

    override fun getAllItemsWithSub(): Flow<List<RackWithSubCategories>> = rackDao.getAllItemsWithSub()
    override fun getAllSubItemsById(rackId: Int): Flow<List<RackSubCategoryEntity>> = rackDao.getAllSubItemsById(rackId)

    override fun getSelectedItem(): Flow<RackEntity?> = rackDao.getSelectedItem()

    private suspend fun insertAllDefaultSubItems(items: List<RackEntity>) {
        val subItems = mutableListOf<RackSubCategoryEntity>()
        items.forEach { item ->
            when (item.name) {
                "梳妆台" -> {
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
                    subItems.add(RackSubCategoryEntity(name = "零食", rackId = item.id, sortOrder = 0))
                    subItems.add(RackSubCategoryEntity(name = "主食", rackId = item.id, sortOrder = 1))
                    subItems.add(RackSubCategoryEntity(name = "调料", rackId = item.id, sortOrder = 2))
                }

                "日用百货" -> {
                    subItems.add(RackSubCategoryEntity(name = "纸巾", rackId = item.id, sortOrder = 0))
                    subItems.add(RackSubCategoryEntity(name = "面巾", rackId = item.id, sortOrder = 1))
                    subItems.add(RackSubCategoryEntity(name = "洗头", rackId = item.id, sortOrder = 2))
                    subItems.add(RackSubCategoryEntity(name = "洗澡", rackId = item.id, sortOrder = 3))
                    subItems.add(RackSubCategoryEntity(name = "洗衣", rackId = item.id, sortOrder = 4))
                    subItems.add(RackSubCategoryEntity(name = "护发", rackId = item.id, sortOrder = 5))
                    subItems.add(RackSubCategoryEntity(name = "片剂", rackId = item.id, sortOrder = 6))
                }

                "圆圆" -> {
                    subItems.add(RackSubCategoryEntity(name = "护肤", rackId = item.id, sortOrder = 0))
                    subItems.add(RackSubCategoryEntity(name = "补剂", rackId = item.id, sortOrder = 1))
                }

                "其他囤货" -> {

                }
            }
        }
        rackDao.insertAllSubItems(subItems)
    }

}

val defaultRackData = listOf(
    RackEntity(name = "梳妆台", sortOrder = 0, selected = true),
    RackEntity(name = "米面粮油", sortOrder = 1),
    RackEntity(name = "日用百货", sortOrder = 2),
    RackEntity(name = "圆圆", sortOrder = 3),
//    RackEntity(name = "其他囤货", sortOrder = 4),
)

