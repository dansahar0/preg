package com.example.pregnancydiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimmyale3102.compose_calendar.ComposeCalendar
import com.jimmyale3102.compose_calendar.model.CalendarDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onExportClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddReminderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // You can optionally show a message to the user if permission is denied
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        viewModel.shareEvent.collect { uri ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/calendar")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open with"))
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                actions = {
                    IconButton(onClick = onExportClicked) {
                        Icon(Icons.Default.Share, contentDescription = "Export Reminders")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddReminderDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ComposeCalendar(
                events = uiState.events,
                dayContent = { calendarDate ->
                    DayContent(
                        calendarDate = calendarDate,
                        pregnancyDateMap = uiState.pregnancyDateMap
                    )
                }
            )
        }

        if (showAddReminderDialog) {
            AddReminderDialog(
                onDismissRequest = { showAddReminderDialog = false },
                onConfirm = { title, dueDate ->
                    viewModel.addReminder(title, dueDate)
                    showAddReminderDialog = false
                }
            )
        }
    }
}

@Composable
private fun DayContent(
    calendarDate: CalendarDate,
    pregnancyDateMap: Map<LocalDate, String>
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                if (calendarDate.isCurrentDay) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = calendarDate.day.toString(),
                color = if (calendarDate.isFromCurrentMonth) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
            // Display "Wk/Day" if it exists for this date
            pregnancyDateMap[calendarDate.date]?.let {
                Text(
                    text = it,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
