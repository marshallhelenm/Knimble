package io.github.marshallhelenm.knimble.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.ui.components.AppBottomBar
import io.github.marshallhelenm.knimble.ui.components.AppDestination
import io.github.marshallhelenm.knimble.ui.components.AppTopBar
import io.github.marshallhelenm.knimble.ui.exercises.ExerciseDetailScreen
import io.github.marshallhelenm.knimble.ui.exercises.ExercisesScreen
import io.github.marshallhelenm.knimble.ui.exercises.ExercisesViewModel
import io.github.marshallhelenm.knimble.ui.timer.TimerScreen
import io.github.marshallhelenm.knimble.ui.timer.TimerViewModel

private enum class AppScreen { Timer, Exercises, ExerciseDetail }

@Composable
fun KnimbleApp(canScheduleExact: Boolean, onAllowExact: () -> Unit, modifier: Modifier = Modifier) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Timer) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    val timerViewModel: TimerViewModel = viewModel()
    val exercisesViewModel: ExercisesViewModel = viewModel()
    // Collected only while the app is on screen, so a ringing break is only "seen" when it can be.
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()
    val breakExercise by timerViewModel.breakExercise.collectAsStateWithLifecycle()

    // A ringing break brings the user to it, and showing it is what stops the repeats.
    LaunchedEffect(timerState?.ringing) {
        if (timerState?.ringing == true) {
            currentScreen = AppScreen.Timer
            timerViewModel.stopRinging()
        }
    }

    // Timer is the root, so back there falls through to the system and closes the app.
    BackHandler(enabled = currentScreen != AppScreen.Timer) {
        currentScreen = if (currentScreen == AppScreen.ExerciseDetail) AppScreen.Exercises else AppScreen.Timer
    }

    val title = when (currentScreen) {
        AppScreen.Timer -> stringResource(if (timerState?.onBreak == true) R.string.title_break else R.string.app_name)
        AppScreen.Exercises -> stringResource(R.string.tab_exercises)
        AppScreen.ExerciseDetail -> stringResource(R.string.title_exercise)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = title,
                onBack = if (currentScreen == AppScreen.ExerciseDetail) { { currentScreen = AppScreen.Exercises } } else null
            )
        },
        bottomBar = {
            AppBottomBar(
                // Detail is a sub-screen of Exercises.
                current = if (currentScreen == AppScreen.Timer) AppDestination.Timer else AppDestination.Exercises,
                onSelect = { selected ->
                    currentScreen = when (selected) {
                        AppDestination.Timer -> AppScreen.Timer
                        AppDestination.Exercises -> AppScreen.Exercises
                    }
                }
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        when (currentScreen) {
            AppScreen.Timer -> timerState?.let { state ->
                TimerScreen(
                    state = state,
                    breakExercise = breakExercise,
                    canScheduleExact = canScheduleExact,
                    onAllowExact = onAllowExact,
                    onStart = timerViewModel::start,
                    onCancel = timerViewModel::cancel,
                    onPickAnother = timerViewModel::pickAnother,
                    onFinishBreak = timerViewModel::finishBreak,
                    onChooseExercises = { currentScreen = AppScreen.Exercises },
                    modifier = screenModifier
                )
            }

            AppScreen.Exercises -> ExercisesScreen(
                viewModel = exercisesViewModel,
                onOpen = {
                    detailId = it.id
                    currentScreen = AppScreen.ExerciseDetail
                },
                modifier = screenModifier
            )

            AppScreen.ExerciseDetail -> detailId?.let {
                ExerciseDetailScreen(exerciseId = it, viewModel = exercisesViewModel, modifier = screenModifier)
            }
        }
    }
}
