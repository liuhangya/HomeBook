package com.fanda.homebook.book.state

import com.fanda.homebook.data.quick.AddQuickEntity

/**
 * 查询方式枚举类
 * 用于控制仪表板详情页的数据查询和展示方式
 *
 * @property AMOUNT 按金额排序/分组查询方式
 * @property TIME 按时间顺序查询方式
 */
enum class QueryWay {
    AMOUNT,  // 按金额排序或分组展示
    TIME     // 按时间顺序展示
}

/**
 * 仪表板详情页UI状态数据类
 * 管理仪表板详情页的数据展示状态，包括查询方式、数据和标题
 *
 * @property queryWay 数据查询和展示方式，默认为按金额查询
 * @property data 显示的快速添加实体数据列表，默认为空列表
 * @property title 页面标题文本，默认为空字符串
 */
data class DashboardDetailUiState(
    // 查询方式：控制数据是按金额排序还是按时间顺序显示
    val queryWay: QueryWay = QueryWay.AMOUNT,

    // 要显示的数据列表，每个AddQuickEntity代表一笔交易记录
    val data : List<AddQuickEntity> = emptyList(),

    // 页面标题，通常用于显示筛选条件或统计类型
    val title : String = "",
)