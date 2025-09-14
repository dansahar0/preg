package com.example.pregnancydiary.ui.detail

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.pregnancydiary.data.PregnancyRepository
import com.example.pregnancydiary.data.local.AppDatabase
import com.example.pregnancydiary.data.model.DailyInfo
import com.example.pregnancydiary.data.model.Note
import com.example.pregnancydiary.export.PdfExporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File


data class DetailScreenState(
    val dailyInfo: List<DailyInfo> = emptyList(),
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)

class DetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val weekId: Int = checkNotNull(savedStateHandle["weekId"])
    private val repository: PregnancyRepository

    private val _shareEvent = MutableSharedFlow<Uri>()
    val shareEvent = _shareEvent.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PregnancyRepository(database.noteDao(), database.dailyInfoDao(), database.reminderDao())
    }

    val uiState: StateFlow<DetailScreenState> = combine(
        repository.getDailyInfoForWeek(weekId),
        repository.getNotesForWeek(weekId)
    ) { dailyInfo, notes ->
        DetailScreenState(dailyInfo = dailyInfo, notes = notes, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailScreenState()
    )

    fun addNote(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val newNote = Note(weekNumber = weekId, text = text)
            repository.insertNote(newNote)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun exportToPdf() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState.isLoading) return@launch

            val exporter = PdfExporter()
            val context = getApplication<Application>().applicationContext
            val file = File(context.cacheDir, "week_${weekId}_summary.pdf")

            exporter.export(
                destination = file,
                weekNumber = weekId,
                dailyInfo = currentState.dailyInfo,
                notes = currentState.notes
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            _shareEvent.emit(uri)
        }
    }
}
