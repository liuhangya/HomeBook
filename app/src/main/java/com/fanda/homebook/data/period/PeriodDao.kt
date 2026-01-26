package com.fanda.homebook.data.period

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao interface PeriodDao {
    @Query("SELECT * FROM period") suspend fun getTypes(): List<PeriodEntity>

    @Query("SELECT COUNT(*) FROM period") suspend fun getCount(): Int

    @Query("SELECT * FROM period WHERE id = :id") fun getTypeById(id: Int): Flow<PeriodEntity?>

    @Transaction suspend fun resetToDefault(types: List<PeriodEntity>) {
        insertAll(types)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(types: List<PeriodEntity>)
}