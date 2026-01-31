package com.fanda.homebook.data.book

import kotlinx.coroutines.flow.Flow

interface BookRepository {
    suspend fun initializeDatabase()
    suspend fun insertWithAutoOrder(entity: BookEntity): Long
    suspend fun getCount(): Int
    suspend fun resetToDefault(list: List<BookEntity>)
    suspend fun insert(entity: BookEntity): Long
    suspend fun update(entity: BookEntity): Int
    suspend fun delete(entity: BookEntity): Int
    suspend fun deleteById(id: Int): Int
    fun getItems(): Flow<List<BookEntity>>
    fun getItemById(id: Int): Flow<BookEntity>
    suspend fun getItemByName(name: String): BookEntity?
    suspend fun getMaxSortOrder(): Int?
    suspend fun updateSortOrders(list: List<BookEntity>): Int
}

class LocalBookRepository(private val bookDao: BookDao) : BookRepository {

    override suspend fun initializeDatabase() {
        if (bookDao.getCount() == 0) {
            resetToDefault(defaultBookData)
        }
    }

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

val defaultBookData = listOf(
    BookEntity(name = "居家生活"),
)
