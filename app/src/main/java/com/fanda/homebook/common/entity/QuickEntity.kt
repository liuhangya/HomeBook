package com.fanda.homebook.common.entity

/**
 * 交易金额类型枚举
 * 定义不同类型的交易金额分类
 */
enum class TransactionAmountType {
    EXPENSE,      // 支出 - 消费类支出
    INCOME,       // 入账 - 收入类入账
    EXCLUDED,     // 不计入收支 - 转账、借贷等不影响总收支的交易
    PLAN,         // 预算 - 预算计划金额
}

/**
 * 支出分类实体类
 * 定义交易分类的基本信息
 *
 * @property name 分类名称（如：餐饮、交通、购物等）
 * @property icon 分类图标资源ID
 * @property type 所属的交易金额类型（支出/收入/不计入收支等）
 */
data class TransactionCategory(
    val name: String, val icon: Int, val type: TransactionAmountType
)

/**
 * 底部弹窗类型枚举
 * 定义应用中可能出现的各种底部弹窗类型
 */
enum class ShowBottomSheetType {
    PAY_WAY,            // 付款方式选择
    MONTH_PLAN,         // 月度计划设置
    PRODUCT,            // 产品选择
    STOCK_PRODUCT,      // 库存产品选择
    STOCK_CATEGORY,     // 库存分类选择
    CATEGORY,           // 交易分类选择
    SHELF_MONTH,        // 货架月份选择
    YEAR_MONTH,         // 年月选择
    USAGE_PERIOD,       // 使用期限选择
    COLOR,              // 颜色选择
    SEASON,             // 季节选择
    SIZE,               // 尺码选择
    EXPIRE_DATE,        // 过期日期选择
    DATE,               // 日期选择
    OPEN_DATE,          // 开封日期选择
    OWNER,              // 所有者选择
    BUY_DATE,           // 购买日期选择
    SELECT_IMAGE,       // 图片选择
    DELETE,             // 删除确认
    EDIT,               // 编辑操作
    ADD,                // 添加操作
    COPY,               // 复制操作
    MOVE,               // 移动操作
    ALL_SELECTED,       // 全选操作
    USED_UP,            // 用完标记
    RACK,               // 货架选择
    SORT_WAY,           // 排序方式
    CATEGORY_WAY,       // 分类方式
    INFO_WAY,           // 信息方式
    NONE,               // 无弹窗
}