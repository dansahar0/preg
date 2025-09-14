package com.example.pregnancydiary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pregnancydiary.data.model.DailyInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dailyInfo: List<DailyInfo>)

    @Query("SELECT * FROM daily_info WHERE weekNumber = :weekNumber ORDER BY dayOfPregnancy ASC")
    fun getDailyInfoForWeek(weekNumber: Int): Flow<List<DailyInfo>>

    @Query("SELECT * FROM daily_info WHERE dayOfPregnancy = :day")
    fun getDailyInfoForDay(day: Int): Flow<DailyInfo>
}
