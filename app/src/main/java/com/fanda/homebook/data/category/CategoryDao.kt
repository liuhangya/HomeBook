package com.fanda.homebook.data.category

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.flow.Flow

@Dao interface CategoryDao {
    // 获取分类数据总数
    @Query("SELECT COUNT(*) FROM category") suspend fun getItemCount(): Int

    // 获取分类数据总数
    @Query("SELECT COUNT(*) FROM sub_category") suspend fun getSubItemCount(): Int

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllItems(list: List<CategoryEntity>)

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllSubItems(list: List<SubCategoryEntity>)

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertItem(entity: CategoryEntity): Long

    // 在发生冲突时覆盖之前的数据
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSubItem(entity: SubCategoryEntity): Long

    @Update suspend fun updateItem(entity: CategoryEntity): Int

    @Update suspend fun updateSubItem(entity: SubCategoryEntity): Int

    @Delete suspend fun deleteItem(entity: CategoryEntity): Int

    @Delete suspend fun deleteSubItem(entity: SubCategoryEntity): Int

    // Flow 版本，支持实时更新，按排序字段升序
    @Query("SELECT * FROM category ORDER BY sortOrder ASC") fun getItemsStream(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM category ORDER BY sortOrder ASC") suspend fun getItems(): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE id = :id") fun getItemById(id: Int): Flow<CategoryEntity>

    @Query("SELECT * FROM sub_category WHERE id = :id") fun getSubItemById(id: Int): Flow<SubCategoryEntity>

    @Query("SELECT * FROM sub_category ORDER BY sortOrder ASC") fun getSubItems(): Flow<List<SubCategoryEntity>>

    @Query("SELECT * FROM sub_category WHERE categoryId = :id  ORDER BY sortOrder ASC") fun getSubItemsById(id: Int): Flow<List<SubCategoryEntity>>

    @Query("SELECT * FROM category WHERE name = :name") suspend fun getItemByName(name: String): CategoryEntity?

    @Query("SELECT * FROM sub_category WHERE name = :name") suspend fun getSubItemByName(name: String): SubCategoryEntity?

    // 获取最大排序值
    @Query("SELECT MAX(sortOrder) FROM category") suspend fun getItemMaxSortOrder(): Int?

    @Query("SELECT MAX(sortOrder) FROM sub_category") suspend fun getSubItemMaxSortOrder(): Int?

    // 批量更新排序
    @Update suspend fun updateItemsSortOrders(list: List<CategoryEntity>): Int

    @Update suspend fun updateSubItemsSortOrders(list: List<SubCategoryEntity>): Int

    @Transaction @Query("SELECT * FROM category ORDER BY sortOrder ASC") fun getAllItemsWithSub(): Flow<List<CategoryWithSubCategories>>


    // 插入时自动设置排序（放在最后）
    @Transaction suspend fun insertItemWithAutoOrder(entity: CategoryEntity): Long {
        val maxOrder = getItemMaxSortOrder() ?: 0
        LogUtils.i("最大排序值为：$maxOrder")
        val insertEntity = entity.copy(sortOrder = maxOrder + 1)
        LogUtils.i("插入数据：$insertEntity")
        return insertItem(insertEntity)
    }

    // 插入时自动设置排序（放在最后）
    @Transaction suspend fun insertSubItemWithAutoOrder(entity: SubCategoryEntity): Long {
        val maxOrder = getSubItemMaxSortOrder() ?: 0
        LogUtils.i("最大排序值为：$maxOrder")
        val insertEntity = entity.copy(sortOrder = maxOrder + 1)
        LogUtils.i("插入数据：$insertEntity")
        return insertSubItem(insertEntity)
    }

    // 重置为默认数据
    @Transaction suspend fun resetToDefault(itemList: List<CategoryEntity>) {
        insertAllItems(itemList)
    }
}