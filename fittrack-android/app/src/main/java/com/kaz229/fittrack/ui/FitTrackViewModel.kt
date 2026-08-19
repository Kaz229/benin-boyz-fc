package com.kaz229.fittrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaz229.fittrack.data.DailySummary
import com.kaz229.fittrack.data.FitTrackRepository
import com.kaz229.fittrack.data.Meal
import com.kaz229.fittrack.data.MealType
import com.kaz229.fittrack.data.UserProfile
import com.kaz229.fittrack.data.Workout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class FitTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FitTrackRepository.get(application)
    private val today: LocalDate = LocalDate.now()

    val profile: StateFlow<UserProfile> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    val todaySummary: StateFlow<DailySummary> = repository.summaryForDay(today)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DailySummary(today.toEpochDay(), 0, 0),
        )

    val lastWeek: StateFlow<List<DailySummary>> = repository.recentSummaries(today, days = 7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayWorkouts: StateFlow<List<Workout>> = repository.workoutsForDay(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayMeals: StateFlow<List<Meal>> = repository.mealsForDay(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allWorkouts: StateFlow<List<Workout>> = repository.allWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addWorkout(activityId: String, durationMinutes: Int, note: String) = viewModelScope.launch {
        repository.addWorkout(activityId, durationMinutes, note, today)
    }

    fun deleteWorkout(workout: Workout) = viewModelScope.launch {
        repository.deleteWorkout(workout)
    }

    fun addMeal(name: String, calories: Int, mealType: MealType) = viewModelScope.launch {
        repository.addMeal(name, calories, mealType, today)
    }

    fun deleteMeal(meal: Meal) = viewModelScope.launch {
        repository.deleteMeal(meal)
    }

    fun setWeight(weightKg: Double) = viewModelScope.launch {
        repository.setWeight(weightKg)
    }

    fun setDailyCalorieGoal(goal: Int) = viewModelScope.launch {
        repository.setDailyCalorieGoal(goal)
    }
}
