package com.fanda.homebook.data.plan

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao interface PlanDao {

    @Query("SELECT * FROM plan_amount WHERE year = :year AND month =:month AND bookId = :bookId") fun getPlanByYearAndMonth(year: Int, month: Int,bookId: Int): Flow<PlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: PlanEntity)

    @Update suspend fun update(item: PlanEntity): Int


}