package com.fanda.homebook.data.closet

import kotlinx.coroutines.flow.Flow


interface ClosetRepository {
    suspend fun getCount(): Int
    suspend fun insert(closet: ClosetEntity): Long
    suspend fun update(closet: ClosetEntity): Int
    suspend fun delete(closet: ClosetEntity): Int
    fun getClosets(ownerId: Int): Flow<List<AddClosetEntity>>
    fun getClosetById(id: Int): Flow<AddClosetEntity>
}

class LocalClosetRepository(private val closetDao: ClosetDao) : ClosetRepository {
    override suspend fun getCount() = closetDao.getCount()

    override suspend fun insert(closet: ClosetEntity) = closetDao.insert(closet)

    override suspend fun update(closet: ClosetEntity) = closetDao.update(closet)

    override suspend fun delete(closet: ClosetEntity) = closetDao.delete(closet)

    override fun getClosets(ownerId: Int): Flow<List<AddClosetEntity>> = closetDao.getClosets(ownerId)
    override fun getClosetById(id: Int): Flow<AddClosetEntity> = closetDao.getClosetById(id)
}
