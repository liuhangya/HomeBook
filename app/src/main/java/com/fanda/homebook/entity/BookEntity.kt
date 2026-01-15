package com.fanda.homebook.entity

data class AmountItemEntity(val name: String, val amount: Float, val type: TransactionType)

data class DailyItemEntity(val category:Int ,val type: TransactionType, val amount: Float, val name: String,val payWay: String,val remark: String)

data class DailyAmountEntity(val id :Int,val date: String, val week:String,val income: Float, val expense: Float, val children : List<DailyItemEntity>)

data class DashBoarItemEntity(val id:Int ,val type: TransactionType, val amount: Float, val name: String,val ratio: Float)
