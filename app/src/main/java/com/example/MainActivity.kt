package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.HapticsHelper
import com.example.audio.SoundSynth
import com.example.engine.GameEngine
import com.example.ui.AppScreen
import com.example.ui.CustomForgeScreen
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.LevelSelectScreen
import com.example.ui.SettingsDialog
import com.example.ui.theme.LevelForgeTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val soundSynth = SoundSynth()
        val haptics = HapticsHelper(this)

        val engine = GameEngine(
            soundSynth = soundSynth,
            haptics = haptics,
            onLevelCompleted = { levelNum, deaths, timeMs ->
                viewModel.onLevelCompleted(levelNum, deaths, timeMs)
            },
            onDeathRecorded = { levelNum ->
                viewModel.onDeathRecorded(levelNum)
            }
        )

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val isDarkTheme = settings?.darkTheme ?: true
            val soundEnabled = settings?.soundEnabled ?: true
            val hapticsEnabled = settings?.hapticsEnabled ?: true

            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val currentLevelNum by viewModel.currentLevelNumber.collectAsStateWithLifecycle()
            val customTestLevel by viewModel.customTestLevel.collectAsStateWithLifecycle()
            val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()
            val customLevels by viewModel.customLevels.collectAsStateWithLifecycle()

            var showGlobalSettings by remember { mutableStateOf(false) }

            // Sync settings to engine
            LaunchedEffect(soundEnabled, hapticsEnabled) {
                soundSynth.isEnabled = soundEnabled
                haptics.isEnabled = hapticsEnabled
            }

            LevelForgeTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        AppScreen.GAME -> {
                            LaunchedEffect(customTestLevel, currentLevelNum) {
                                if (customTestLevel != null) {
                                    engine.loadCustomLevel(customTestLevel!!)
                                } else {
                                    engine.loadLevel(currentLevelNum)
                                }
                            }

                            GameScreen(
                                engine = engine,
                                levelNumber = customTestLevel?.levelNumber ?: currentLevelNum,
                                isDarkTheme = isDarkTheme,
                                soundEnabled = soundEnabled,
                                hapticsEnabled = hapticsEnabled,
                                onToggleSound = { viewModel.toggleSound(it) },
                                onToggleHaptics = { viewModel.toggleHaptics(it) },
                                onToggleTheme = { viewModel.toggleTheme(!isDarkTheme) },
                                onNavigateLevelSelect = { viewModel.openLevelSelect() },
                                onNavigateLevelForge = { viewModel.openForge() }
                            )
                        }

                        AppScreen.LEVEL_SELECT -> {
                            LevelSelectScreen(
                                progressList = allProgress,
                                totalDeaths = settings?.totalDeaths ?: 0,
                                isDarkTheme = isDarkTheme,
                                onSelectLevel = { num ->
                                    viewModel.playLevel(num)
                                },
                                onOpenForge = {
                                    viewModel.openForge()
                                },
                                onOpenSettings = {
                                    showGlobalSettings = true
                                }
                            )
                        }

                        AppScreen.FORGE -> {
                            CustomForgeScreen(
                                customLevels = customLevels,
                                isDarkTheme = isDarkTheme,
                                onBack = { viewModel.openLevelSelect() },
                                onTestPlay = { def ->
                                    viewModel.playCustomLevel(def)
                                },
                                onSaveLevel = { name, json ->
                                    viewModel.saveCustomLevel(name, json)
                                },
                                onDeleteLevel = { id ->
                                    viewModel.deleteCustomLevel(id)
                                }
                            )
                        }
                    }

                    if (showGlobalSettings) {
                        SettingsDialog(
                            soundEnabled = soundEnabled,
                            hapticsEnabled = hapticsEnabled,
                            isDarkTheme = isDarkTheme,
                            totalDeaths = settings?.totalDeaths ?: 0,
                            totalClears = settings?.totalClears ?: 0,
                            onToggleSound = { viewModel.toggleSound(it) },
                            onToggleHaptics = { viewModel.toggleHaptics(it) },
                            onToggleTheme = { viewModel.toggleTheme(it) },
                            onResetProgress = { viewModel.resetAllProgress() },
                            onDismiss = { showGlobalSettings = false }
                        )
                    }
                }
            }
        }
    }
}
