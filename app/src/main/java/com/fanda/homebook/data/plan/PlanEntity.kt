package com.fanda.homebook.data.plan

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_amount") data class PlanEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0,val bookId: Int, val amount: Float, val year: Int, val month: Int)
