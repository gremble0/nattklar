package com.example.nattklar.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.nattklar.model.dataprocessing.NightEvents
import com.example.nattklar.model.dataobjects.NewsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Responsible for holding and managing the UI-related data to be displayed on the newsscreen.
 */
@RequiresApi(Build.VERSION_CODES.O)
class NewsViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState(listOf()))
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    /**
     * Loads night events.
     */
    fun setNightEvents() {
        uiState.value.news = NightEvents.getNightEvents()
    }
}