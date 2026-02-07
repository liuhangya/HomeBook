package com.fanda.homebook.data.season

import kotlinx.coroutines.flow.Flow

/**
 * 季节仓库接口
 * 定义季节数据的操作契约，提供数据访问的抽象层
 */
interface SeasonRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 获取所有季节列表（挂起函数版本）
    suspend fun getSeasons(): List<SeasonEntity>

    // 获取季节总数
    suspend fun getCount(): Int

    // 根据ID获取季节（Flow版本，支持实时更新）
    fun getSeasonById(id: Int): Flow<SeasonEntity?>

    // 根据ID列表获取季节（Flow版本，支持实时更新）
    fun getSeasonsByIdsFlow(ids: List<Int>): Flow<List<SeasonEntity>>

    // 批量插入衣柜季节关联数据
    suspend fun insertSeasonRelationAll(entities: List<ClosetSeasonRelation>)

    // 更新某个衣柜的季节关联
    suspend fun updateSeasonsForCloset(closetId: Int, seasonIds: List<Int>)

    // 根据衣柜ID获取关联的季节ID列表
    suspend fun getSeasonIdsByClosetId(closetId: Int): List<Int>
}

/**
 * 本地季节仓库实现类
 * 实现SeasonRepository接口，封装对Room数据库的直接访问
 *
 * @property seasonDao 季节数据访问对象
 */
class LocalSeasonRepository(private val seasonDao: SeasonDao) : SeasonRepository {
    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认季节数据
     */
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            seasonDao.resetToDefault(defaultSeasonData)
        }
    }

    /**
     * 获取所有季节列表
     *
     * @return 季节实体列表
     */
    override suspend fun getSeasons(): List<SeasonEntity> = seasonDao.getSeasonTypes()

    /**
     * 获取季节总数
     *
     * @return 季节总数
     */
    override suspend fun getCount() = seasonDao.getCount()

    /**
     * 根据ID获取季节
     *
     * @param id 季节ID
     * @return Flow流，包含对应的季节实体（可为空）
     */
    override fun getSeasonById(id: Int) = seasonDao.getSeasonTypeById(id)

    /**
     * 根据ID列表获取季节
     *
     * @param ids 季节ID列表
     * @return Flow流，包含对应的季节实体列表
     */
    override fun getSeasonsByIdsFlow(ids: List<Int>): Flow<List<SeasonEntity>> = seasonDao.getSeasonsByIdsFlow(ids)

    /**
     * 批量插入衣柜季节关联数据
     *
     * @param entities 衣柜季节关联实体列表
     */
    override suspend fun insertSeasonRelationAll(entities: List<ClosetSeasonRelation>) = seasonDao.insertSeasonRelationAll(entities)

    /**
     * 更新某个衣柜的季节关联
     * 先删除旧关联，再创建新关联
     *
     * @param closetId 衣柜ID
     * @param seasonIds 新的季节ID列表
     */
    override suspend fun updateSeasonsForCloset(closetId: Int, seasonIds: List<Int>) = seasonDao.updateSeasonsForCloset(closetId, seasonIds)

    /**
     * 根据衣柜ID获取关联的季节ID列表
     *
     * @param closetId 衣柜ID
     * @return 季节ID列表
     */
    override suspend fun getSeasonIdsByClosetId(closetId: Int): List<Int> = seasonDao.getSeasonIdsByClosetId(closetId)
}

/**
 * 默认季节数据
 * 应用首次安装或数据库重置时使用的默认季节列表
 * 包含4个基本季节
 */
val defaultSeasonData = listOf(
    SeasonEntity(name = "春季"),  // 春季
    SeasonEntity(name = "夏季"),  // 夏季
    SeasonEntity(name = "秋季"),  // 秋季
    SeasonEntity(name = "冬季"),  // 冬季
)