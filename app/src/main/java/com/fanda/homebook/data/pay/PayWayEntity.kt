package com.fanda.homebook.data.pay

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 付款方式实体类
 * 对应数据库中的pay_way表，用于存储支付方式信息
 *
 * @property id 主键ID，自增长
 * @property name 付款方式名称，如"微信支付"、"支付宝"、"现金"、"银行卡"等
 * @property sortOrder 排序序号，用于控制付款方式在列表中的显示顺序，默认值为0
 */
@Entity(tableName = "pay_way") data class PayWayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 付款方式名称
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0
)