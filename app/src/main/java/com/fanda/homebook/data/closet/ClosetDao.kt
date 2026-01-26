package com.fanda.homebook.data.closet

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClosetDao {

    // 获取数据总数
    @Query("SELECT COUNT(*) FROM closet")
    suspend fun getCount(): Int

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClosetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ClosetEntity>)

    @Update
    suspend fun update(entity: ClosetEntity): Int

    @Delete
    suspend fun delete(entity: ClosetEntity): Int

    @Delete
    suspend fun deleteAll(closet: List<ClosetEntity>): Int

    @Update
    suspend fun updateAll(closet: List<ClosetEntity>): Int

    @Query("DELETE FROM closet WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Transaction
    @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND moveToTrash = :moveToTrash")
    fun getClosets(ownerId: Int, moveToTrash: Boolean = false): Flow<List<AddClosetEntity>>

    @Transaction
    @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND categoryId IS NULL AND subCategoryId IS NULL")
    fun getNoCategoryClosets(ownerId: Int): Flow<List<AddClosetEntity>>

    @Transaction
    @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND categoryId = :categoryId AND moveToTrash = :moveToTrash")
    fun getClosetsByCategory(ownerId: Int, categoryId: Int, moveToTrash: Boolean = false): Flow<List<AddClosetEntity>>

    @Transaction
    @Query("SELECT * FROM closet WHERE ownerId = :ownerId AND subCategoryId = :subCategoryId AND moveToTrash = :moveToTrash")
    fun getClosetsBySubCategory(ownerId: Int, subCategoryId: Int, moveToTrash: Boolean = false): Flow<List<AddClosetEntity>>

    // 用于判断当前分类下有没有子分类
    @Transaction
    @Query("SELECT COUNT(*) > 0 FROM closet WHERE ownerId = :ownerId AND categoryId = :categoryId AND subCategoryId IS NOT NULL AND moveToTrash = :moveToTrash")
    suspend fun hasClosetsWithSubcategory(
        ownerId: Int, categoryId: Int, moveToTrash: Boolean = false
    ): Boolean

    // 关联查询对象
    @Transaction
    @Query("SELECT * FROM closet WHERE  ownerId = :ownerId AND id = :id")
    suspend fun getClosetById( ownerId: Int,id: Int): AddClosetEntity

}