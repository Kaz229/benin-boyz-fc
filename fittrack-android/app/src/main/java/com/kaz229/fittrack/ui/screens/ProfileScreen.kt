package com.kaz229.fittrack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.kaz229.fittrack.data.UserProfile

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onSave: (weightKg: Double, goal: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var weight by remember(profile.weightKg) {
        mutableStateOf(profile.weightKg.toString())
    }
    var goal by remember(profile.dailyCalorieGoal) {
        mutableStateOf(profile.dailyCalorieGoal.toString())
    }

    val parsedWeight = weight.replace(',', '.').toDoubleOrNull()
    val parsedGoal = goal.toIntOrNull()
    val valid = parsedWeight != null && parsedWeight > 0 && parsedGoal != null && parsedGoal > 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Mon profil",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Poids (kg)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text("Objectif calorique journalier (kcal)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onSave(parsedWeight ?: profile.weightKg, parsedGoal ?: profile.dailyCalorieGoal) },
            enabled = valid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Enregistrer")
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Comment sont calculées les calories ?", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "FitTrack utilise la formule MET : " +
                        "kcal = MET × 3,5 × poids(kg) ÷ 200 × durée(min). " +
                        "Chaque sport a son propre MET (course 9,8 ; football 7,0 ; yoga 2,5…). " +
                        "C'est une estimation, pas une mesure médicale.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
