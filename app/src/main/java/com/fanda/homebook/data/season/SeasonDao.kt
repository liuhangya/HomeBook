package com.fanda.homebook.data.season

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao interface SeasonDao {
    @Query("SELECT * FROM season") suspend fun getSeasonTypes(): List<SeasonEntity>

    @Query("SELECT COUNT(*) FROM season") suspend fun getCount(): Int

    @Query("SELECT * FROM season WHERE id = :id") fun getSeasonTypeById(id: Int): Flow<SeasonEntity?>

    @Transaction suspend fun resetToDefault(seasonTypes: List<SeasonEntity>) {
        insertAll(seasonTypes)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(seasonTypes: List<SeasonEntity>)
}