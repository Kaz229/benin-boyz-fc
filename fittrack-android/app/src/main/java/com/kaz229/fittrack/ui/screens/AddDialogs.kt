package com.kaz229.fittrack.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kaz229.fittrack.data.Activities
import com.kaz229.fittrack.data.MealType

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (activityId: String, durationMinutes: Int, note: String) -> Unit,
) {
    var activityId by remember { mutableStateOf(Activities.all.first().id) }
    var duration by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val parsedDuration = duration.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle séance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Activité")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Activities.all.forEach { activity ->
                        FilterChip(
                            selected = activity.id == activityId,
                            onClick = { activityId = activity.id },
                            label = { Text(activity.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Durée (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(activityId, parsedDuration ?: 0, note.trim()) },
                enabled = parsedDuration != null && parsedDuration > 0,
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}

@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, calories: Int, mealType: MealType) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.LUNCH) }
    val parsedCalories = calories.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau repas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = type == mealType,
                            onClick = { mealType = type },
                            label = { Text(type.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Aliment / plat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), parsedCalories ?: 0, mealType) },
                enabled = name.isNotBlank() && parsedCalories != null && parsedCalories > 0,
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}

