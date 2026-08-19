package com.kaz229.fittrack.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class FitTrackRepository(
    private val workoutDao: WorkoutDao,
    private val mealDao: MealDao,
    private val userPreferences: UserPreferences,
) {

    val profile: Flow<UserProfile> = userPreferences.profile

    fun workoutsForDay(date: LocalDate): Flow<List<Workout>> =
        workoutDao.observeForDay(date.toEpochDay())

    fun mealsForDay(date: LocalDate): Flow<List<Meal>> =
        mealDao.observeForDay(date.toEpochDay())

    fun allWorkouts(): Flow<List<Workout>> = workoutDao.observeAll()

    fun summaryForDay(date: LocalDate): Flow<DailySummary> {
        val day = date.toEpochDay()
        return combine(
            mealDao.observeCaloriesForDay(day),
            workoutDao.observeCaloriesForDay(day),
        ) { caloriesIn, caloriesOut ->
            DailySummary(epochDay = day, caloriesIn = caloriesIn, caloriesOut = caloriesOut)
        }
    }

    /** Bilan des [days] derniers jours, du plus ancien au plus récent. */
    fun recentSummaries(today: LocalDate, days: Int): Flow<List<DailySummary>> {
        val fromDay = today.minusDays((days - 1).toLong()).toEpochDay()
        return combine(
            mealDao.observeDailyTotals(fromDay),
            workoutDao.observeDailyTotals(fromDay),
        ) { intake, burned ->
            val intakeByDay = intake.associate { it.epochDay to it.total }
            val burnedByDay = burned.associate { it.epochDay to it.total }
            (0 until days).map { offset ->
                val day = fromDay + offset
                DailySummary(
                    epochDay = day,
                    caloriesIn = intakeByDay[day] ?: 0,
                    caloriesOut = burnedByDay[day] ?: 0,
                )
            }
        }
    }

    suspend fun addWorkout(activityId: String, durationMinutes: Int, note: String, date: LocalDate) {
        val activity = Activities.byId(activityId)
        val weight = userPreferences.profile.first().weightKg
        workoutDao.insert(
            Workout(
                activityId = activity.id,
                durationMinutes = durationMinutes,
                caloriesBurned = Activities.caloriesBurned(activity, durationMinutes, weight),
                epochDay = date.toEpochDay(),
                note = note,
            ),
        )
    }

    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)

    suspend fun addMeal(name: String, calories: Int, mealType: MealType, date: LocalDate) {
        mealDao.insert(
            Meal(
                name = name,
                calories = calories,
                mealType = mealType.id,
                epochDay = date.toEpochDay(),
            ),
        )
    }

    suspend fun deleteMeal(meal: Meal) = mealDao.delete(meal)

    suspend fun setWeight(weightKg: Double) = userPreferences.setWeight(weightKg)

    suspend fun setDailyCalorieGoal(goal: Int) = userPreferences.setDailyCalorieGoal(goal)

    companion object {
        @Volatile
        private var instance: FitTrackRepository? = null

        fun get(context: Context): FitTrackRepository = instance ?: synchronized(this) {
            instance ?: run {
                val db = FitTrackDatabase.get(context)
                FitTrackRepository(
                    workoutDao = db.workoutDao(),
                    mealDao = db.mealDao(),
                    userPreferences = UserPreferences(context.applicationContext),
                ).also { instance = it }
            }
        }
    }
}
