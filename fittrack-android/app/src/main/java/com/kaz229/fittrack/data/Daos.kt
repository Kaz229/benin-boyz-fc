package com.kaz229.fittrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: Workout): Long

    @Delete
    suspend fun delete(workout: Workout)

    @Query("SELECT * FROM workouts ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE epochDay = :epochDay ORDER BY id DESC")
    fun observeForDay(epochDay: Long): Flow<List<Workout>>

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM workouts WHERE epochDay = :epochDay")
    fun observeCaloriesForDay(epochDay: Long): Flow<Int>

    @Query("SELECT epochDay, COALESCE(SUM(caloriesBurned), 0) AS total FROM workouts WHERE epochDay >= :fromDay GROUP BY epochDay")
    fun observeDailyTotals(fromDay: Long): Flow<List<DayTotal>>
}

@Dao
interface MealDao {
    @Insert
    suspend fun insert(meal: Meal): Long

    @Delete
    suspend fun delete(meal: Meal)

    @Query("SELECT * FROM meals WHERE epochDay = :epochDay ORDER BY id DESC")
    fun observeForDay(epochDay: Long): Flow<List<Meal>>

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meals WHERE epochDay = :epochDay")
    fun observeCaloriesForDay(epochDay: Long): Flow<Int>

    @Query("SELECT epochDay, COALESCE(SUM(calories), 0) AS total FROM meals WHERE epochDay >= :fromDay GROUP BY epochDay")
    fun observeDailyTotals(fromDay: Long): Flow<List<DayTotal>>
}

/** Ligne agrégée « un jour, un total » renvoyée par les requêtes GROUP BY. */
data class DayTotal(
    val epochDay: Long,
    val total: Int,
)
