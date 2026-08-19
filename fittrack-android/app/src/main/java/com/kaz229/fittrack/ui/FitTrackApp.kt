package com.kaz229.fittrack.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kaz229.fittrack.ui.screens.AddMealDialog
import com.kaz229.fittrack.ui.screens.AddWorkoutDialog
import com.kaz229.fittrack.ui.screens.HomeScreen
import com.kaz229.fittrack.ui.screens.MealsScreen
import com.kaz229.fittrack.ui.screens.ProfileScreen
import com.kaz229.fittrack.ui.screens.WorkoutsScreen

private enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME("home", "Accueil", Icons.Filled.Home),
    WORKOUTS("workouts", "Séances", Icons.Filled.FitnessCenter),
    MEALS("meals", "Repas", Icons.Filled.Restaurant),
    PROFILE("profile", "Profil", Icons.Filled.Person),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitTrackApp(viewModel: FitTrackViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    var showWorkoutDialog by remember { mutableStateOf(false) }
    var showMealDialog by remember { mutableStateOf(false) }

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val summary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val lastWeek by viewModel.lastWeek.collectAsStateWithLifecycle()
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    val meals by viewModel.todayMeals.collectAsStateWithLifecycle()

    val currentDestination = Destination.entries.firstOrNull { destination ->
        currentRoute?.hierarchy?.any { it.route == destination.route } == true
    } ?: Destination.HOME

    Scaffold(
        topBar = { TopAppBar(title = { Text(currentDestination.label) }) },
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == currentDestination,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            when (currentDestination) {
                Destination.WORKOUTS -> FloatingActionButton(onClick = { showWorkoutDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter une séance")
                }
                Destination.MEALS -> FloatingActionButton(onClick = { showMealDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter un repas")
                }
                else -> Unit
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.HOME.route) {
                HomeScreen(summary = summary, profile = profile, lastWeek = lastWeek)
            }
            composable(Destination.WORKOUTS.route) {
                WorkoutsScreen(workouts = workouts, onDelete = viewModel::deleteWorkout)
            }
            composable(Destination.MEALS.route) {
                MealsScreen(meals = meals, onDelete = viewModel::deleteMeal)
            }
            composable(Destination.PROFILE.route) {
                ProfileScreen(
                    profile = profile,
                    onSave = { weight, goal ->
                        viewModel.setWeight(weight)
                        viewModel.setDailyCalorieGoal(goal)
                    },
                )
            }
        }
    }

    if (showWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showWorkoutDialog = false },
            onConfirm = { activityId, duration, note ->
                viewModel.addWorkout(activityId, duration, note)
                showWorkoutDialog = false
            },
        )
    }
    if (showMealDialog) {
        AddMealDialog(
            onDismiss = { showMealDialog = false },
            onConfirm = { name, calories, mealType ->
                viewModel.addMeal(name, calories, mealType)
                showMealDialog = false
            },
        )
    }
}
