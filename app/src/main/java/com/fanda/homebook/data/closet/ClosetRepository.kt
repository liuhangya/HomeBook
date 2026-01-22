package com.fanda.homebook.data.closet

import kotlinx.coroutines.flow.Flow


interface ClosetRepository {
    suspend fun getCount(): Int
    suspend fun insert(closet: ClosetEntity): Long
    suspend fun insertAll(closets: List<ClosetEntity>)
    suspend fun update(closet: ClosetEntity): Int
    suspend fun delete(closet: ClosetEntity): Int
    suspend fun deleteAll(closet: List<ClosetEntity>): Int
    suspend fun updateAll(closet: List<ClosetEntity>): Int
    fun getClosets(ownerId: Int, moveToTrash: Boolean = false): Flow<List<AddClosetEntity>>
    fun getClosetsByCategory(
        ownerId: Int,
        categoryId: Int,
        moveToTrash: Boolean = false
    ): Flow<List<AddClosetEntity>>

    fun getClosetsBySubCategory(
        ownerId: Int,
        subCategoryId: Int,
        moveToTrash: Boolean = false
    ): Flow<List<AddClosetEntity>>

    suspend fun getClosetById(ownerId: Int, id: Int): AddClosetEntity
    suspend fun hasClosetsWithSubcategory(
        ownerId: Int, categoryId: Int
    ): Boolean
}

class LocalClosetRepository(private val closetDao: ClosetDao) : ClosetRepository {
    override suspend fun getCount() = closetDao.getCount()

    override suspend fun insert(closet: ClosetEntity) = closetDao.insert(closet)
    override suspend fun insertAll(closets: List<ClosetEntity>) = closetDao.insertAll(closets)

    override suspend fun update(closet: ClosetEntity) = closetDao.update(closet)

    override suspend fun delete(closet: ClosetEntity) = closetDao.delete(closet)
    override suspend fun deleteAll(closet: List<ClosetEntity>) = closetDao.deleteAll(closet)
    override suspend fun updateAll(closet: List<ClosetEntity>) = closetDao.updateAll(closet)

    override fun getClosets(ownerId: Int, moveToTrash: Boolean): Flow<List<AddClosetEntity>> =
        closetDao.getClosets(ownerId, moveToTrash)

    override fun getClosetsByCategory(
        ownerId: Int,
        categoryId: Int,
        moveToTrash: Boolean
    ): Flow<List<AddClosetEntity>> =
        closetDao.getClosetsByCategory(ownerId, categoryId, moveToTrash)

    override fun getClosetsBySubCategory(
        ownerId: Int,
        subCategoryId: Int,
        moveToTrash: Boolean
    ): Flow<List<AddClosetEntity>> =
        closetDao.getClosetsBySubCategory(ownerId, subCategoryId, moveToTrash)

    override suspend fun getClosetById(ownerId: Int, id: Int): AddClosetEntity =
        closetDao.getClosetById(ownerId, id)

    override suspend fun hasClosetsWithSubcategory(ownerId: Int, categoryId: Int) =
        closetDao.hasClosetsWithSubcategory(ownerId, categoryId)
}
