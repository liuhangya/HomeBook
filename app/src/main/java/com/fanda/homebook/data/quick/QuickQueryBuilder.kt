package com.fanda.homebook.data.quick

import androidx.sqlite.db.SimpleSQLiteQuery

// 查询参数数据类
data class QueryParams(
    val bookId: Int?, val categoryId: Int?,val subCategoryId: Int?, val year: Int ,val month: Int
)

class QuickQueryBuilder {
    private val conditions = mutableListOf<String>()
    private val args = mutableListOf<Any>()

    fun build(): SimpleSQLiteQuery {
        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else {
            ""
        }

        val query = """
            SELECT * FROM quick
            $whereClause
            ORDER BY date DESC
        """.trimIndent()

        return SimpleSQLiteQuery(query, args.toTypedArray())
    }

    fun withBookId(bookId: Int?): QuickQueryBuilder {
        bookId?.let {
            conditions.add("bookId = ?")
            args.add(it)
        }
        return this
    }

    fun withCategoryId(categoryId: Int?): QuickQueryBuilder {
        categoryId?.let {
            conditions.add("categoryId = ?")
            args.add(it)
        }
        return this
    }

    fun withSubCategoryId(subCategoryId: Int?): QuickQueryBuilder {
        subCategoryId?.let {
            conditions.add("subCategoryId = ?")
            args.add(it)
        }
        return this
    }

}