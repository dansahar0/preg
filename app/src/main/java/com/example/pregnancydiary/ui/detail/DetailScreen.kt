package com.example.pregnancydiary.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    onExportClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var newNoteText by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.shareEvent.collect { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF"))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Week Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    IconButton(onClick = onExportClicked) {
                        Icon(Icons.Default.Share, contentDescription = "Export to PDF")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Daily Developments", style = MaterialTheme.typography.headlineMedium)
            }
            if (uiState.isLoading) {
                item { CircularProgressIndicator() }
            } else {
                items(uiState.dailyInfo) { info ->
                    DailyInfoCard(info)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("My Notes", style = MaterialTheme.typography.headlineMedium)
            }
            items(uiState.notes) { note ->
                NoteCard(note, onDelete = { viewModel.deleteNote(note) })
            }

            item {
                AddNoteSection(
                    textValue = newNoteText,
                    onTextChange = { newNoteText = it },
                    onAddClick = {
                        viewModel.addNote(newNoteText.text)
                        newNoteText = TextFieldValue("") // Clear text field
                    }
                )
            }
        }
    }
}

@Composable
fun DailyInfoCard(info: com.example.pregnancydiary.data.model.DailyInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(info.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(info.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun NoteCard(note: com.example.pregnancydiary.data.model.Note, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(note.text, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete note")
            }
        }
    }
}

@Composable
fun AddNoteSection(
    textValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = onTextChange,
            label = { Text("Add a new note") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add note")
        }
    }
}
