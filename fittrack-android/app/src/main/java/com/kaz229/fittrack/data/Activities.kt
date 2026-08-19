package com.kaz229.fittrack.data

import kotlin.math.roundToInt

/**
 * Catalogue d'activités avec leur MET (Metabolic Equivalent of Task),
 * valeurs issues du Compendium of Physical Activities.
 */
data class ActivityType(
    val id: String,
    val label: String,
    val met: Double,
)

object Activities {
    val all = listOf(
        ActivityType("football", "Football", 7.0),
        ActivityType("course", "Course à pied", 9.8),
        ActivityType("marche", "Marche rapide", 4.3),
        ActivityType("velo", "Vélo", 7.5),
        ActivityType("natation", "Natation", 8.3),
        ActivityType("muscu", "Musculation", 5.0),
        ActivityType("hiit", "HIIT / Cross-training", 8.0),
        ActivityType("basket", "Basket", 6.5),
        ActivityType("tennis", "Tennis", 7.3),
        ActivityType("yoga", "Yoga / Étirements", 2.5),
        ActivityType("autre", "Autre", 5.0),
    )

    fun byId(id: String): ActivityType = all.firstOrNull { it.id == id } ?: all.last()

    /**
     * Calories brûlées = MET x 3.5 x poids(kg) / 200 x durée(min).
     * C'est l'estimation standard utilisée par la plupart des trackers.
     */
    fun caloriesBurned(activity: ActivityType, durationMinutes: Int, weightKg: Double): Int =
        (activity.met * 3.5 * weightKg / 200.0 * durationMinutes).roundToInt()
}

enum class MealType(val id: String, val label: String) {
    BREAKFAST("petit_dejeuner", "Petit-déjeuner"),
    LUNCH("dejeuner", "Déjeuner"),
    DINNER("diner", "Dîner"),
    SNACK("collation", "Collation");

    companion object {
        fun byId(id: String): MealType = entries.firstOrNull { it.id == id } ?: SNACK
    }
}
