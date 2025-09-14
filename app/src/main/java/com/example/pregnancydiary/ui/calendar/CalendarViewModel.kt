package com.example.pregnancydiary.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jimmyale3102.compose_calendar.model.CalendarEvent
import com.example.pregnancydiary.data.PregnancyRepository
import com.example.pregnancydiary.data.local.AppDatabase
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.pregnancydiary.data.model.Reminder
import com.example.pregnancydiary.export.IcsExporter
import com.example.pregnancydiary.notifications.AlarmScheduler
import com.example.pregnancydiary.notifications.AlarmSchedulerImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CalendarScreenState(
    val events: List<CalendarEvent> = emptyList(),
    val pregnancyDateMap: Map<LocalDate, String> = emptyMap() // Maps a date to a "Wk/Day" string
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PregnancyRepository
    private val alarmScheduler: AlarmScheduler

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PregnancyRepository(database.noteDao(), database.dailyInfoDao(), database.reminderDao())
        alarmScheduler = AlarmSchedulerImpl(application)
    }

    private val _shareEvent = MutableSharedFlow<Uri>()
    val shareEvent = _shareEvent.asSharedFlow()

    // TODO: This is a hardcoded value for demonstration purposes.
    // In a real application, this should be a setting that the user can configure.
    // 1. Create a new screen (e.g., SettingsScreen) where the user can pick a date.
    // 2. Store this date in a persistent storage like SharedPreferences or a new Room table.
    // 3. Create a repository method to retrieve this date.
    // 4. In this ViewModel, fetch the date from the repository instead of using a hardcoded value.
    private val pregnancyStartDate: LocalDate = LocalDate.now().minusWeeks(10)

    val uiState: StateFlow<CalendarScreenState> = repository.getAllReminders()
        .map { reminders ->
            // Convert reminders to calendar events
            val events = reminders.map { reminder ->
                val date = Instant.ofEpochMilli(reminder.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                CalendarEvent(
                    date = date,
                    title = reminder.title
                    // You can add color, description etc. here if the library supports it
                )
            }

            // Create the map of pregnancy dates
            val pregnancyDateMap = mutableMapOf<LocalDate, String>()
            for (day in 0 until 280) { // 40 weeks * 7 days
                val date = pregnancyStartDate.plusDays(day.toLong())
                val week = (day / 7) + 1
                val dayOfWeek = (day % 7) + 1
                pregnancyDateMap[date] = "W$week D$dayOfWeek"
            }

            CalendarScreenState(events = events, pregnancyDateMap = pregnancyDateMap)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarScreenState()
        )

    fun addReminder(title: String, dueDate: Long) {
        viewModelScope.launch {
            val date = Instant.ofEpochMilli(dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(pregnancyStartDate, date)
            val weekNumber = (daysSinceStart / 7).toInt() + 1

            val newReminder = Reminder(
                title = title,
                dueDate = dueDate,
                weekNumber = weekNumber
            )
            repository.insertOrUpdateReminder(newReminder)
            alarmScheduler.schedule(newReminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            alarmScheduler.cancel(reminder)
        }
    }

    fun exportReminders() {
        viewModelScope.launch {
            val reminders = repository.getAllReminders().first()
            if (reminders.isEmpty()) return@launch

            val exporter = IcsExporter()
            val icsContent = exporter.export(reminders)

            val context = getApplication<Application>().applicationContext
            val file = File(context.cacheDir, "reminders.ics")
            file.writeText(icsContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            _shareEvent.emit(uri)
        }
    }
}
