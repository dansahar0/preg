package com.example.pregnancydiary.data

import com.example.pregnancydiary.data.local.DailyInfoDao
import com.example.pregnancydiary.data.local.NoteDao
import com.example.pregnancydiary.data.model.DailyInfo
import com.example.pregnancydiary.data.model.Note
import kotlinx.coroutines.flow.Flow

import com.example.pregnancydiary.data.local.ReminderDao

class PregnancyRepository(
    private val noteDao: NoteDao,
    private val dailyInfoDao: DailyInfoDao,
    private val reminderDao: ReminderDao
) {
    fun getNotesForWeek(weekNumber: Int): Flow<List<Note>> {
        return noteDao.getNotesForWeek(weekNumber)
    }

    suspend fun insertNote(note: Note) {
        noteDao.insertOrUpdateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    fun getDailyInfoForWeek(weekNumber: Int): Flow<List<DailyInfo>> {
        return dailyInfoDao.getDailyInfoForWeek(weekNumber)
    }

    fun getAllReminders(): Flow<List<com.example.pregnancydiary.data.model.Reminder>> {
        return reminderDao.getAllReminders()
    }
}
