package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CustomLevelEntity
import com.example.data.GameRepository
import com.example.data.GameSettingsEntity
import com.example.data.LevelProgressEntity
import com.example.engine.LevelDef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LEVEL_SELECT,
    GAME,
    FORGE
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    val allProgress: StateFlow<List<LevelProgressEntity>>
    val customLevels: StateFlow<List<CustomLevelEntity>>
    val settings: StateFlow<GameSettingsEntity?>

    private val _currentScreen = MutableStateFlow(AppScreen.GAME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentLevelNumber = MutableStateFlow(1)
    val currentLevelNumber: StateFlow<Int> = _currentLevelNumber.asStateFlow()

    private val _customTestLevel = MutableStateFlow<LevelDef?>(null)
    val customTestLevel: StateFlow<LevelDef?> = _customTestLevel.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

        allProgress = repository.allProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        customLevels = repository.customLevels.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        settings = repository.settingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameSettingsEntity()
        )

        viewModelScope.launch {
            repository.initializeDefaults()
        }
    }

    fun playLevel(levelNum: Int) {
        _customTestLevel.value = null
        _currentLevelNumber.value = levelNum
        _currentScreen.value = AppScreen.GAME
    }

    fun playCustomLevel(def: LevelDef) {
        _customTestLevel.value = def
        _currentScreen.value = AppScreen.GAME
    }

    fun openLevelSelect() {
        _currentScreen.value = AppScreen.LEVEL_SELECT
    }

    fun openForge() {
        _currentScreen.value = AppScreen.FORGE
    }

    fun onLevelCompleted(levelNumber: Int, deaths: Int, elapsedMs: Long) {
        if (_customTestLevel.value != null) {
            // Completed custom level, nothing to unlock in campaign
            return
        }
        viewModelScope.launch {
            repository.completeLevel(levelNumber, deaths, elapsedMs)
        }
    }

    fun onDeathRecorded(levelNumber: Int) {
        viewModelScope.launch {
            repository.recordDeath(levelNumber)
        }
    }

    fun saveCustomLevel(name: String, levelDataJson: String) {
        viewModelScope.launch {
            repository.saveCustomLevel(name, levelDataJson)
        }
    }

    fun deleteCustomLevel(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomLevel(id)
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(sound = enabled)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(haptics = enabled)
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(darkTheme = isDark)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            _currentLevelNumber.value = 1
        }
    }
}
