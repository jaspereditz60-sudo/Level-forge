package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val unlocked: Boolean = false,
    val completed: Boolean = false,
    val deaths: Int = 0,
    val bestTimeMs: Long = 0L,
    val stars: Int = 0
)

@Entity(tableName = "custom_levels")
data class CustomLevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val levelDataJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val deaths: Int = 0,
    val completed: Boolean = false
)

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val darkTheme: Boolean = true,
    val totalDeaths: Int = 0,
    val totalClears: Int = 0
)
