package com.fanda.homebook.data.color

import com.fanda.homebook.data.product.ProductEntity
import kotlinx.coroutines.flow.Flow


interface ColorTypeRepository {
    suspend fun initializeDatabase()
    suspend fun insertWithAutoOrder(entity: ColorTypeEntity): Long
    suspend fun getCount(): Int
    suspend fun resetToDefault(list: List<ColorTypeEntity>)
    suspend fun insert(entity: ColorTypeEntity): Long
    suspend fun update(entity: ColorTypeEntity): Int
    suspend fun delete(entity: ColorTypeEntity): Int
    suspend fun deleteById(id: Int): Int
    fun getItems(): Flow<List<ColorTypeEntity>>
    fun getItemById(id: Int): Flow<ColorTypeEntity>
    suspend fun getItemByName(name: String): ColorTypeEntity?
    suspend fun getMaxSortOrder(): Int?
    suspend fun updateSortOrders(list: List<ColorTypeEntity>): Int
}

class LocalColorTypeRepository(private val colorTypeDao: ColorTypeDao) : ColorTypeRepository {

    override suspend fun initializeDatabase() {
        if (colorTypeDao.getCount() == 0) {
            resetToDefault(defaultColorData)
        }
    }

    override suspend fun insertWithAutoOrder(entity: ColorTypeEntity) = colorTypeDao.insertWithAutoOrder(entity)
    override suspend fun getCount() = colorTypeDao.getCount()

    override suspend fun resetToDefault(list: List<ColorTypeEntity>) = colorTypeDao.resetToDefault(list)

    override suspend fun insert(entity: ColorTypeEntity) = colorTypeDao.insert(entity)

    override suspend fun update(entity: ColorTypeEntity) = colorTypeDao.update(entity)

    override suspend fun delete(entity: ColorTypeEntity) = colorTypeDao.delete(entity)

    override suspend fun deleteById(id: Int) = colorTypeDao.deleteById(id)

    override fun getItems(): Flow<List<ColorTypeEntity>> = colorTypeDao.getItems()

    override fun getItemById(id: Int): Flow<ColorTypeEntity> = colorTypeDao.getItemById(id)

    override suspend fun getItemByName(name: String) = colorTypeDao.getItemByName(name)

    override suspend fun getMaxSortOrder() = colorTypeDao.getMaxSortOrder()
    override suspend fun updateSortOrders(list: List<ColorTypeEntity>) = colorTypeDao.updateSortOrders(list)

}

val defaultColorData = listOf(
    ColorTypeEntity(
        name = "棕色系",
        color = 0xFF6A3D06,
        sortOrder = 0,
    ), ColorTypeEntity(
        name = "黑色系",
        color = 0xFF000000,
        sortOrder = 1,
    ), ColorTypeEntity(
        name = "蓝色系",
        color = 0xFF9CD4EB,
        sortOrder = 2,
    ), ColorTypeEntity(
        name = "绿色系",
        color = 0xFFA4D66B,
        sortOrder = 3,
    ), ColorTypeEntity(
        name = "紫色系",
        color = 0xFFB398F1,
        sortOrder = 4,
    ), ColorTypeEntity(
        name = "红色系",
        color = 0xFFDA4851,
        sortOrder = 5,
    ), ColorTypeEntity(
        name = "灰色系",
        color = 0xFFDADADA,
        sortOrder = 6,
    ), ColorTypeEntity(
        name = "玫红系",
        color = 0xFFE360BE,
        sortOrder = 7,
    ), ColorTypeEntity(
        name = "橙色系",
        color = 0xFFEC9F4C,
        sortOrder = 8,
    ), ColorTypeEntity(
        name = "金色系",
        color = 0xFFEFDD8B,
        sortOrder = 9,
    ), ColorTypeEntity(
        name = "裸色系",
        color = 0xFFEFE5CE,
        sortOrder = 10,
    ), ColorTypeEntity(
        name = "黄色系",
        color = 0xFFF8D854,
        sortOrder = 11,
    ), ColorTypeEntity(
        name = "白色系",
        color = 0xFFFFFFFF,
        sortOrder = 12,
    )
)