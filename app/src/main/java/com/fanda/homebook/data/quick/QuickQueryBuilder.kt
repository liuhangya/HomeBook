package com.fanda.homebook.data.quick

import androidx.sqlite.db.SimpleSQLiteQuery

/**
 * 查询参数数据类
 * 用于封装快速记账查询的各种参数
 *
 * @property bookId 账本ID（可选）
 * @property subCategoryId 子分类ID（可选）
 * @property year 查询年份
 * @property month 查询月份
 * @property refresh 是否强制刷新缓存
 */
data class QueryParams(
    val bookId: Int?, val subCategoryId: Int?, val year: Int, val month: Int, val refresh: Boolean
)

/**
 * 快速记账查询构建器
 * 用于动态构建SQL查询条件，支持灵活的查询参数组合
 */
class QuickQueryBuilder {
    private val conditions = mutableListOf<String>()  // WHERE条件列表
    private val args = mutableListOf<Any>()           // 查询参数值列表

    /**
     * 构建最终的SQLite查询对象
     *
     * @return SimpleSQLiteQuery 包含SQL语句和参数的查询对象
     */
    fun build(): SimpleSQLiteQuery {
        // 构建WHERE子句
        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else {
            ""
        }

        // 构建完整的SQL查询语句
        val query = """
            SELECT * FROM quick
            $whereClause
            ORDER BY date DESC  -- 按日期降序排列，最新的记录在前
        """.trimIndent()

        // 创建查询对象，包含SQL语句和参数数组
        return SimpleSQLiteQuery(query, args.toTypedArray())
    }

    /**
     * 添加账本ID查询条件
     *
     * @param bookId 账本ID（可选）
     * @return 当前构建器实例，支持链式调用
     */
    fun withBookId(bookId: Int?): QuickQueryBuilder {
        bookId?.let {
            conditions.add("bookId = ?")
            args.add(it)
        }
        return this
    }

    /**
     * 添加主分类ID查询条件
     *
     * @param categoryId 主分类ID（可选）
     * @return 当前构建器实例，支持链式调用
     */
    fun withCategoryId(categoryId: Int?): QuickQueryBuilder {
        categoryId?.let {
            conditions.add("categoryId = ?")
            args.add(it)
        }
        return this
    }

    /**
     * 添加子分类ID查询条件
     *
     * @param subCategoryId 子分类ID（可选）
     * @return 当前构建器实例，支持链式调用
     */
    fun withSubCategoryId(subCategoryId: Int?): QuickQueryBuilder {
        subCategoryId?.let {
            conditions.add("subCategoryId = ?")
            args.add(it)
        }
        return this
    }

    /**
     * 添加分类类型查询条件
     *
     * @param categoryType 分类类型（支出/收入/不计入收支等）
     * @return 当前构建器实例，支持链式调用
     */
    fun withCategoryType(categoryType: Int?): QuickQueryBuilder {
        categoryType?.let {
            conditions.add("categoryType = ?")
            args.add(it)
        }
        return this
    }
}