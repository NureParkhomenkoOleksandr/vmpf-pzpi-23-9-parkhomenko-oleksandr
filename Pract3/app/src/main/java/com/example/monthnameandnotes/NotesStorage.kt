package com.example.monthnameandnotes

import android.content.Context
import org.json.JSONArray

class NotesStorage(context: Context) {
    private val preferences = context.getSharedPreferences("notes_storage", Context.MODE_PRIVATE)

    fun loadNotes(): List<String> {
        val savedNotes = preferences.getString(NOTES_KEY, null) ?: return emptyList()
        return runCatching {
            val notesArray = JSONArray(savedNotes)
            List(notesArray.length()) { index -> notesArray.getString(index) }
        }.getOrDefault(emptyList())
    }

    fun saveNotes(notes: List<String>): List<String> {
        val notesArray = JSONArray()
        notes.forEach(notesArray::put)
        preferences.edit().putString(NOTES_KEY, notesArray.toString()).apply()
        return notes
    }

    private companion object {
        const val NOTES_KEY = "notes"
    }
}
