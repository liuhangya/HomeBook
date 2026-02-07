package com.fanda.homebook.data.period

import kotlinx.coroutines.flow.Flow

/**
 * 使用期限仓库接口
 * 定义使用期限数据的操作契约，提供数据访问的抽象层
 */
interface PeriodRepository {
    // 初始化数据库，如果数据库为空则填充默认数据
    suspend fun initializeDatabase()

    // 获取所有使用期限类型列表（挂起函数版本）
    suspend fun getTypes(): List<PeriodEntity>

    // 获取使用期限类型总数
    suspend fun getCount(): Int

    // 根据ID获取使用期限类型（Flow版本，支持实时更新）
    fun getTypeById(id: Int): Flow<PeriodEntity?>
}

/**
 * 本地使用期限仓库实现类
 * 实现PeriodRepository接口，封装对Room数据库的直接访问
 *
 * @property periodDao 使用期限数据访问对象
 */
class LocalPeriodRepository(private val periodDao: PeriodDao) : PeriodRepository {
    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则填充默认使用期限数据
     */
    override suspend fun initializeDatabase() {
        if (getCount() == 0) {
            periodDao.resetToDefault(defaultPeriodData)
        }
    }

    /**
     * 获取所有使用期限类型列表
     *
     * @return 使用期限实体列表
     */
    override suspend fun getTypes(): List<PeriodEntity> = periodDao.getTypes()

    /**
     * 获取使用期限类型总数
     *
     * @return 使用期限类型总数
     */
    override suspend fun getCount() = periodDao.getCount()

    /**
     * 根据ID获取使用期限类型
     *
     * @param id 使用期限类型ID
     * @return Flow流，包含对应的使用期限实体（可为空）
     */
    override fun getTypeById(id: Int) = periodDao.getTypeById(id)
}

/**
 * 默认使用期限数据
 * 应用首次安装或数据库重置时使用的默认使用期限列表
 * 包含3种常见的使用期限类型
 */
val defaultPeriodData = listOf(
    PeriodEntity(name = "全天"),  // 全天可用，无时间限制
    PeriodEntity(name = "日用"),  // 仅白天使用
    PeriodEntity(name = "夜用"),  // 仅夜间使用
)