package com.fanda.homebook.data.transaction

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fanda.homebook.data.category.CategoryWithSubCategories
import kotlinx.coroutines.flow.Flow

@Dao interface TransactionDao {
    // 获取分类数据总数
    @Query("SELECT COUNT(*) FROM transaction_category") suspend fun getItemCount(): Int

    // 获取分类数据总数
    @Query("SELECT COUNT(*) FROM transaction_sub_category") suspend fun getSubItemCount(): Int

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllItems(list: List<TransactionEntity>)

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllSubItems(list: List<TransactionSubEntity>)

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertItem(entity: TransactionEntity): Long

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSubItem(entity: TransactionSubEntity): Long

    @Update suspend fun updateItem(entity: TransactionEntity): Int

    @Update suspend fun updateSubItem(entity: TransactionSubEntity): Int

    @Delete suspend fun deleteItem(entity: TransactionEntity): Int

    @Delete suspend fun deleteSubItem(entity: TransactionSubEntity): Int

    // Flow 版本，支持实时更新，按排序字段升序
    @Query("SELECT * FROM transaction_category") fun getItemsStream(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transaction_category") suspend fun getItems(): List<TransactionEntity>

    @Query("SELECT * FROM transaction_category WHERE id = :id") fun getItemById(id: Int): Flow<TransactionEntity>

    @Query("SELECT * FROM transaction_sub_category WHERE id = :id") fun getSubItemById(id: Int): Flow<TransactionSubEntity>

    @Query("SELECT * FROM transaction_sub_category ORDER BY sortOrder ASC") fun getSubItems(): Flow<List<TransactionSubEntity>>

    @Query("SELECT * FROM transaction_sub_category WHERE categoryId = :id  ORDER BY sortOrder ASC") fun getSubItemsById(id: Int): Flow<List<TransactionSubEntity>>

    @Query("SELECT * FROM transaction_category WHERE name = :name") suspend fun getItemByName(name: String): TransactionEntity?

    @Query("SELECT * FROM transaction_sub_category WHERE name = :name") suspend fun getSubItemByName(name: String): TransactionSubEntity?

    @Query("SELECT MAX(sortOrder) FROM transaction_sub_category") suspend fun getSubItemMaxSortOrder(): Int?

    // 批量更新排序
    @Update suspend fun updateItemsSortOrders(list: List<TransactionEntity>): Int

    @Update suspend fun updateSubItemsSortOrders(list: List<TransactionSubEntity>): Int

    @Transaction @Query("SELECT * FROM transaction_category") fun getAllItemsWithSub(): Flow<List<TransactionWithSubCategories>>


    // 重置为默认数据
    @Transaction suspend fun resetToDefault(itemList: List<TransactionEntity>) {
        insertAllItems(itemList)
    }
}