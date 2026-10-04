package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber")
    suspend fun getProgress(levelNumber: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialProgress(list: List<LevelProgressEntity>)

    @Query("SELECT * FROM custom_levels ORDER BY createdAt DESC")
    fun getAllCustomLevels(): Flow<List<CustomLevelEntity>>

    @Query("SELECT * FROM custom_levels WHERE id = :id")
    suspend fun getCustomLevelById(id: Long): CustomLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLevel(level: CustomLevelEntity): Long

    @Update
    suspend fun updateCustomLevel(level: CustomLevelEntity)

    @Query("DELETE FROM custom_levels WHERE id = :id")
    suspend fun deleteCustomLevel(id: Long)

    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<GameSettingsEntity?>

    @Query("SELECT * FROM game_settings WHERE id = 1")
    suspend fun getSettings(): GameSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettingsEntity)
}
