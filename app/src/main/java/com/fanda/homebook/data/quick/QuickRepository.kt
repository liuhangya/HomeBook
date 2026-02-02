package com.fanda.homebook.data.quick

import kotlinx.coroutines.flow.Flow

interface QuickRepository {
    suspend fun insert(entity: QuickEntity): Long

    suspend fun delete(entity: QuickEntity): Int

    suspend fun update(entity: QuickEntity): Int

    suspend fun deleteById(id: Int): Int

    suspend fun getQuickById(id: Int): AddQuickEntity

    fun getQuickListByCategory(
        bookId: Int? = null, categoryId: Int? = null, subCategoryId: Int? = null
    ): Flow<List<AddQuickEntity>>
}

class LocalQuickRepository(private val quickDao: QuickDao) : QuickRepository {
    override suspend fun insert(entity: QuickEntity) = quickDao.insert(entity)

    override suspend fun delete(entity: QuickEntity) = quickDao.delete(entity)

    override suspend fun update(entity: QuickEntity) = quickDao.update(entity)

    override suspend fun deleteById(id: Int) = quickDao.deleteById(id)

    override suspend fun getQuickById(id: Int) = quickDao.getQuickById(id)
    override fun getQuickListByCategory(
        bookId: Int?, categoryId: Int?, subCategoryId: Int?
    ): Flow<List<AddQuickEntity>> = quickDao.getQuickListByCategory(bookId, categoryId, subCategoryId)
}