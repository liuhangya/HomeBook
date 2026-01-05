package com.fanda.homebook.entity

data class StateMenuEntity(val id: Int, val name: String, val count: Int)

enum class StockState{
    USING,
    NOT_OPEN,
    USED_UP
}

data class StockGridEntity(val name: String, val label: String, val photoUrl: String, var expiryTime:Int ,val expiryDes:String,val remark: String,var status: StockState)