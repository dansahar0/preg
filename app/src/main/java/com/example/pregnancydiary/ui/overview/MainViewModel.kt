package com.example.pregnancydiary.ui.overview

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WeeklyOverviewState(
    val weeks: List<Int> = (1..42).toList() // Standard range for pregnancy tracking
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyOverviewState())
    val uiState: StateFlow<WeeklyOverviewState> = _uiState.asStateFlow()

    // In the future, this ViewModel will have functions to fetch data
    // from the database when a week is selected.
}
