package com.fanda.homebook.data.transaction

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.flow.Flow

/**
 * 交易分类数据访问对象 (Data Access Object)
 * 定义对TransactionEntity和TransactionSubEntity表的所有数据库操作
 */
@Dao interface TransactionDao {
    // 获取交易主分类数据总数
    @Query("SELECT COUNT(*) FROM transaction_category") suspend fun getItemCount(): Int

    // 获取交易子分类数据总数
    @Query("SELECT COUNT(*) FROM transaction_sub_category") suspend fun getSubItemCount(): Int

    // 批量插入交易主分类
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllItems(list: List<TransactionEntity>)

    // 批量插入交易子分类
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllSubItems(list: List<TransactionSubEntity>)

    // 单条插入交易主分类，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertItem(entity: TransactionEntity): Long

    // 单条插入交易子分类，在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSubItem(entity: TransactionSubEntity): Long

    // 更新交易主分类
    @Update suspend fun updateItem(entity: TransactionEntity): Int

    // 更新交易子分类
    @Update suspend fun updateSubItem(entity: TransactionSubEntity): Int

    // 删除交易主分类
    @Delete suspend fun deleteItem(entity: TransactionEntity): Int

    // 删除交易子分类
    @Delete suspend fun deleteSubItem(entity: TransactionSubEntity): Int

    // Flow版本查询，支持实时更新
    @Query("SELECT * FROM transaction_category") fun getItemsStream(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transaction_category") suspend fun getItems(): List<TransactionEntity>

    @Query("SELECT * FROM transaction_category WHERE id = :id") fun getItemById(id: Int): Flow<TransactionEntity>

    @Query("SELECT * FROM transaction_sub_category WHERE id = :id") fun getSubItemById(id: Int): Flow<TransactionSubEntity>

    @Query("SELECT * FROM transaction_sub_category ORDER BY sortOrder ASC") fun getSubItems(): Flow<List<TransactionSubEntity>>

    @Query("SELECT * FROM transaction_sub_category WHERE categoryId = :id ORDER BY sortOrder ASC") fun getSubItemsById(id: Int): Flow<List<TransactionSubEntity>>

    @Query("SELECT * FROM transaction_category WHERE name = :name") suspend fun getItemByName(name: String): TransactionEntity?

    @Query("SELECT * FROM transaction_sub_category WHERE name = :name AND categoryId = :parentId") suspend fun getSubItemByName(name: String, parentId: Int): TransactionSubEntity?

    // 获取交易子分类最大排序值
    @Query("SELECT MAX(sortOrder) FROM transaction_sub_category") suspend fun getSubItemMaxSortOrder(): Int?

    // 批量更新交易主分类排序字段
    @Update suspend fun updateItemsSortOrders(list: List<TransactionEntity>): Int

    // 批量更新交易子分类排序字段
    @Update suspend fun updateSubItemsSortOrders(list: List<TransactionSubEntity>): Int

    // 获取所有交易主分类及其对应的子分类（一对多关系）
    @Transaction @Query("SELECT * FROM transaction_category") fun getAllItemsWithSub(): Flow<List<TransactionWithSubCategories>>

    /**
     * 插入交易子分类时自动设置排序（放在最后）
     * 使用@Transaction确保获取最大排序值和插入操作的原子性
     *
     * @param entity 要插入的交易子分类实体
     * @return 新插入记录的ID
     */
    @Transaction suspend fun insertSubItemWithAutoOrder(entity: TransactionSubEntity): Long {
        val maxOrder = getSubItemMaxSortOrder() ?: 0
        LogUtils.i("最大排序值为：$maxOrder")
        val insertEntity = entity.copy(sortOrder = maxOrder + 1)
        LogUtils.i("插入数据：$insertEntity")
        return insertSubItem(insertEntity)
    }

    /**
     * 重置为默认交易分类数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param itemList 默认交易主分类数据列表
     */
    @Transaction suspend fun resetToDefault(itemList: List<TransactionEntity>) {
        insertAllItems(itemList)
    }
}