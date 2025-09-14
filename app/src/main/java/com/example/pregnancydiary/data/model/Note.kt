package com.example.pregnancydiary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekNumber: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
