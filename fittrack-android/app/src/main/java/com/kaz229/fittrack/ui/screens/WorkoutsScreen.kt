package com.kaz229.fittrack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaz229.fittrack.data.Activities
import com.kaz229.fittrack.data.Workout
import java.time.LocalDate

@Composable
fun WorkoutsScreen(
    workouts: List<Workout>,
    onDelete: (Workout) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (workouts.isEmpty()) {
        EmptyState(
            message = "Aucune séance enregistrée.\nAppuie sur + pour ajouter ta première séance.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(workouts, key = { it.id }) { workout ->
            WorkoutCard(workout = workout, onDelete = { onDelete(workout) })
        }
    }
}

@Composable
private fun WorkoutCard(workout: Workout, onDelete: () -> Unit) {
    val activity = Activities.byId(workout.activityId)
    val date = LocalDate.ofEpochDay(workout.epochDay)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${workout.durationMinutes} min · ${workout.caloriesBurned} kcal · " +
                        "${date.dayOfMonth}/${date.monthValue}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (workout.note.isNotBlank()) {
                    Text(text = workout.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer la séance")
            }
        }
    }
}
