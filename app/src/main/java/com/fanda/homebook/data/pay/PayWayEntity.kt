package com.fanda.homebook.data.pay

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pay_way") data class PayWayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String, val sortOrder: Int = 0 // 排序序号，默认为0
)
