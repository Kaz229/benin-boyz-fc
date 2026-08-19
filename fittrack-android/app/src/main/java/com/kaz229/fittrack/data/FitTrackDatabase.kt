package com.kaz229.fittrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Workout::class, Meal::class], version = 1, exportSchema = false)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun mealDao(): MealDao

    companion object {
        @Volatile
        private var instance: FitTrackDatabase? = null

        fun get(context: Context): FitTrackDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                FitTrackDatabase::class.java,
                "fittrack.db",
            ).build().also { instance = it }
        }
    }
}
