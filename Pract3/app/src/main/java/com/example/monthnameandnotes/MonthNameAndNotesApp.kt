package com.example.monthnameandnotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.monthnameandnotes.ui.theme.MonthNameAndNotesTheme

@Composable
fun MonthNameAndNotesApp() {
    val context = LocalContext.current.applicationContext
    val notesStorage = remember(context) { NotesStorage(context) }
    var monthNumber by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable { mutableStateOf("") }
    var notes by remember { mutableStateOf(notesStorage.loadNotes()) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Місяці та нотатки",
                style = MaterialTheme.typography.headlineMedium
            )

            MonthNameCard(
                monthNumber = monthNumber,
                onMonthNumberChange = { monthNumber = it.filter(Char::isDigit).take(2) }
            )

            NotesCard(
                noteText = noteText,
                notes = notes,
                onNoteTextChange = { noteText = it },
                onAddNote = {
                    val trimmedNote = noteText.trim()
                    if (trimmedNote.isNotEmpty()) {
                        notes = notesStorage.saveNotes(notes + trimmedNote)
                        noteText = ""
                    }
                },
                onDeleteNote = { index ->
                    notes = notesStorage.saveNotes(
                        notes.filterIndexed { noteIndex, _ -> noteIndex != index }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthNameAndNotesPreview() {
    MonthNameAndNotesTheme {
        MonthNameAndNotesApp()
    }
}
