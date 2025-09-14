package com.example.pregnancydiary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_info")
data class DailyInfo(
    @PrimaryKey
    val dayOfPregnancy: Int, // Day 1 to 280
    val weekNumber: Int,
    val title: String,
    val description: String
)
