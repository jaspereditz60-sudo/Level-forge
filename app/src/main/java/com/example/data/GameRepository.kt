package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val dao: GameDao) {

    val allProgress: Flow<List<LevelProgressEntity>> = dao.getAllProgress()
    val customLevels: Flow<List<CustomLevelEntity>> = dao.getAllCustomLevels()
    val settingsFlow: Flow<GameSettingsEntity?> = dao.getSettingsFlow()

    suspend fun initializeDefaults(totalLevels: Int = 20) {
        val current = dao.getAllProgress().firstOrNull() ?: emptyList()
        if (current.isEmpty()) {
            val initial = (1..totalLevels).map { num ->
                LevelProgressEntity(
                    levelNumber = num,
                    unlocked = num == 1,
                    completed = false,
                    deaths = 0,
                    bestTimeMs = 0L,
                    stars = 0
                )
            }
            dao.insertInitialProgress(initial)
        }
        val settings = dao.getSettings()
        if (settings == null) {
            dao.saveSettings(GameSettingsEntity())
        }
    }

    suspend fun recordDeath(levelNumber: Int) {
        val current = dao.getProgress(levelNumber) ?: LevelProgressEntity(levelNumber = levelNumber, unlocked = true)
        dao.insertOrUpdateProgress(current.copy(deaths = current.deaths + 1))
        
        val settings = dao.getSettings() ?: GameSettingsEntity()
        dao.saveSettings(settings.copy(totalDeaths = settings.totalDeaths + 1))
    }

    suspend fun completeLevel(levelNumber: Int, levelDeaths: Int, timeMs: Long, totalLevels: Int = 20): Int {
        val stars = when {
            levelDeaths <= 1 -> 3
            levelDeaths <= 4 -> 2
            else -> 1
        }
        val current = dao.getProgress(levelNumber)
        val bestTime = if (current == null || current.bestTimeMs == 0L) timeMs else minOf(current.bestTimeMs, timeMs)
        val highestStars = maxOf(current?.stars ?: 0, stars)

        dao.insertOrUpdateProgress(
            LevelProgressEntity(
                levelNumber = levelNumber,
                unlocked = true,
                completed = true,
                deaths = levelDeaths,
                bestTimeMs = bestTime,
                stars = highestStars
            )
        )

        // Unlock next level
        if (levelNumber < totalLevels) {
            val next = dao.getProgress(levelNumber + 1) ?: LevelProgressEntity(levelNumber = levelNumber + 1)
            dao.insertOrUpdateProgress(next.copy(unlocked = true))
        }

        val settings = dao.getSettings() ?: GameSettingsEntity()
        dao.saveSettings(settings.copy(totalClears = settings.totalClears + 1))
        return stars
    }

    suspend fun saveCustomLevel(name: String, levelDataJson: String): Long {
        return dao.insertCustomLevel(
            CustomLevelEntity(
                name = name,
                levelDataJson = levelDataJson
            )
        )
    }

    suspend fun updateCustomLevel(level: CustomLevelEntity) {
        dao.updateCustomLevel(level)
    }

    suspend fun deleteCustomLevel(id: Long) {
        dao.deleteCustomLevel(id)
    }

    suspend fun updateSettings(sound: Boolean? = null, haptics: Boolean? = null, darkTheme: Boolean? = null) {
        val current = dao.getSettings() ?: GameSettingsEntity()
        dao.saveSettings(
            current.copy(
                soundEnabled = sound ?: current.soundEnabled,
                hapticsEnabled = haptics ?: current.hapticsEnabled,
                darkTheme = darkTheme ?: current.darkTheme
            )
        )
    }

    suspend fun resetAllProgress(totalLevels: Int = 20) {
        val resetList = (1..totalLevels).map { num ->
            LevelProgressEntity(
                levelNumber = num,
                unlocked = num == 1,
                completed = false,
                deaths = 0,
                bestTimeMs = 0L,
                stars = 0
            )
        }
        for (item in resetList) {
            dao.insertOrUpdateProgress(item)
        }
    }
}
