package com.fanda.homebook.data.rack

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.size.SizeEntity
import kotlinx.coroutines.flow.Flow

@Dao interface RackDao {

    @Query("SELECT COUNT(*) FROM rack") suspend fun getItemCount(): Int

    @Query("SELECT * FROM rack WHERE id = :id") fun getItemById(id: Int): Flow<RackEntity>

    @Query("SELECT * FROM rack_sub_category WHERE id = :id") fun getSubItemById(id: Int): Flow<RackSubCategoryEntity>

    @Update suspend fun updateItem(item: RackEntity): Int

    @Update suspend fun updateSubItem(item: RackSubCategoryEntity): Int

    @Query("SELECT * FROM rack ORDER BY sortOrder ASC") suspend fun getItems(): List<RackEntity>

    @Query("SELECT * FROM rack_sub_category WHERE rackId = :rackId ORDER BY sortOrder ASC") fun getAllSubItemsById(rackId: Int): Flow<List<RackSubCategoryEntity>>

    @Query("SELECT * FROM rack WHERE selected = 1") fun getSelectedItem(): Flow<RackEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllItems(list: List<RackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllSubItems(list: List<RackSubCategoryEntity>)

    @Transaction @Query("SELECT * FROM rack ORDER BY sortOrder ASC") fun getAllItemsWithSub(): Flow<List<RackWithSubCategories>>

    // 重置为默认数据
    @Transaction suspend fun resetToDefault(itemList: List<RackEntity>) {
        insertAllItems(itemList)
    }
}