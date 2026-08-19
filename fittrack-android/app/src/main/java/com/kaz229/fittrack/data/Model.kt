package com.kaz229.fittrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Une séance de sport. Les calories brûlées sont calculées à l'enregistrement
 * (formule MET) puis stockées, pour que l'historique reste stable même si
 * le poids de l'utilisateur change plus tard.
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val epochDay: Long,
    val note: String = "",
)

/** Un repas ou une collation, avec les calories ingérées. */
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Int,
    val mealType: String,
    val epochDay: Long,
)

/** Bilan d'une journée : ingéré, brûlé, et solde net. */
data class DailySummary(
    val epochDay: Long,
    val caloriesIn: Int,
    val caloriesOut: Int,
) {
    val net: Int get() = caloriesIn - caloriesOut
}
