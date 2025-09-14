package com.example.pregnancydiary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val dueDate: Long, // Storing as a timestamp
    val weekNumber: Int,
    val isCompleted: Boolean = false
)
