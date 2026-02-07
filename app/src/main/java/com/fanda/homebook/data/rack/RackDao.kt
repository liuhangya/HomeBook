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

/**
 * 货架数据访问对象 (Data Access Object)
 * 定义对RackEntity和RackSubCategoryEntity表的所有数据库操作
 */
@Dao interface RackDao {

    /**
     * 获取货架数据总数
     *
     * @return 货架总数
     */
    @Query("SELECT COUNT(*) FROM rack") suspend fun getItemCount(): Int

    /**
     * 根据ID获取货架（Flow版本，支持实时更新）
     *
     * @param id 货架ID
     * @return Flow流，包含对应的货架实体
     */
    @Query("SELECT * FROM rack WHERE id = :id") fun getItemById(id: Int): Flow<RackEntity>

    /**
     * 根据ID获取货架子分类（Flow版本，支持实时更新）
     *
     * @param id 货架子分类ID
     * @return Flow流，包含对应的货架子分类实体
     */
    @Query("SELECT * FROM rack_sub_category WHERE id = :id") fun getSubItemById(id: Int): Flow<RackSubCategoryEntity>

    /**
     * 更新货架数据
     *
     * @param item 货架实体
     * @return 受影响的行数
     */
    @Update suspend fun updateItem(item: RackEntity): Int

    /**
     * 更新货架子分类数据
     *
     * @param item 货架子分类实体
     * @return 受影响的行数
     */
    @Update suspend fun updateSubItem(item: RackSubCategoryEntity): Int

    /**
     * 获取所有货架列表（挂起函数版本），按排序字段升序排列
     *
     * @return 货架实体列表
     */
    @Query("SELECT * FROM rack ORDER BY sortOrder ASC") suspend fun getItems(): List<RackEntity>

    /**
     * 根据货架ID获取所有子分类列表（Flow版本，支持实时更新）
     *
     * @param rackId 货架ID
     * @return Flow流，包含对应的货架子分类实体列表
     */
    @Query("SELECT * FROM rack_sub_category WHERE rackId = :rackId ORDER BY sortOrder ASC") fun getAllSubItemsById(rackId: Int): Flow<List<RackSubCategoryEntity>>

    /**
     * 获取当前选中的货架（Flow版本，支持实时更新）
     * 用于获取应用当前活跃货架的信息
     *
     * @return Flow流，包含当前选中的货架实体（可为空）
     */
    @Query("SELECT * FROM rack WHERE selected = 1") fun getSelectedItem(): Flow<RackEntity?>

    /**
     * 批量插入货架数据，在发生冲突时覆盖之前的数据
     *
     * @param list 货架实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllItems(list: List<RackEntity>)

    /**
     * 批量插入货架子分类数据，在发生冲突时覆盖之前的数据
     *
     * @param list 货架子分类实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllSubItems(list: List<RackSubCategoryEntity>)

    /**
     * 获取所有货架及其对应的子分类（一对多关系）
     * 使用@Transaction确保关联查询的原子性
     *
     * @return Flow流，包含完整的RackWithSubCategories列表
     */
    @Transaction @Query("SELECT * FROM rack ORDER BY sortOrder ASC") fun getAllItemsWithSub(): Flow<List<RackWithSubCategories>>

    /**
     * 重置为默认货架数据（清空表并插入默认数据列表）
     * 使用@Transaction确保操作的原子性
     *
     * @param itemList 默认货架数据列表
     */
    @Transaction suspend fun resetToDefault(itemList: List<RackEntity>) {
        insertAllItems(itemList)
    }
}