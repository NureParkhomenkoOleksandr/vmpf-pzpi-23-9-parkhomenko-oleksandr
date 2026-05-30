package com.example.monthnameandnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.monthnameandnotes.ui.theme.MonthNameAndNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonthNameAndNotesTheme {
                MonthNameAndNotesApp()
            }
        }
    }
}
