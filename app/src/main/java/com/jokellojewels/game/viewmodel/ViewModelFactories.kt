package com.jokellojewels.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jokellojewels.game.AppContainer

/** Plain factories — the repository and sound manager come from the Application (no DI framework). */

class ProgressViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProgressViewModel::class.java)) { "Unknown ViewModel: $modelClass" }
        return ProgressViewModel(container.repository) as T
    }
}

class QuizViewModelFactory(
    private val categoryId: String,
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(QuizViewModel::class.java)) { "Unknown ViewModel: $modelClass" }
        return QuizViewModel(categoryId, container.repository, container.soundManager) as T
    }
}

class PairsViewModelFactory(
    private val level: Int,
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PairsViewModel::class.java)) { "Unknown ViewModel: $modelClass" }
        return PairsViewModel(level, container.repository, container.soundManager) as T
    }
}
