package io.github.marshallhelenm.knimble.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.ui.components.AppBottomBar
import io.github.marshallhelenm.knimble.ui.components.AppDestination
import io.github.marshallhelenm.knimble.ui.components.AppTopBar
import io.github.marshallhelenm.knimble.ui.exercises.EditExerciseScreen
import io.github.marshallhelenm.knimble.ui.exercises.EditExerciseViewModel
import io.github.marshallhelenm.knimble.ui.exercises.ExerciseDetailScreen
import io.github.marshallhelenm.knimble.ui.exercises.ExercisesScreen
import io.github.marshallhelenm.knimble.ui.exercises.ExercisesViewModel
import io.github.marshallhelenm.knimble.ui.timer.TimerScreen
import io.github.marshallhelenm.knimble.ui.timer.TimerViewModel

private enum class AppScreen { Timer, Exercises, ExerciseDetail, EditExercise }

@Composable
fun KnimbleApp(canScheduleExact: Boolean, onAllowExact: () -> Unit, modifier: Modifier = Modifier) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Timer) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    // Null while on EditExercise means a new exercise.
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    // Bumped on every visit to the editor so each visit gets a fresh form instead of the last one's leftovers.
    var editSession by rememberSaveable { mutableIntStateOf(0) }
    var askDiscard by rememberSaveable { mutableStateOf(false) }

    val timerViewModel: TimerViewModel = viewModel()
    val exercisesViewModel: ExercisesViewModel = viewModel()
    // Owned here rather than by the edit screen so every way out (back, the top bar) can check for unsaved edits.
    val editViewModel = if (currentScreen == AppScreen.EditExercise) {
        viewModel<EditExerciseViewModel>(key = "edit-exercise-$editSession")
    } else {
        null
    }
    // Collected only while the app is on screen, so a ringing break is only "seen" when it can be.
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()
    val breakExercise by timerViewModel.breakExercise.collectAsStateWithLifecycle()

    // Held here so the list keeps its place across a visit to an exercise, and so the floating buttons can see it.
    val exercisesListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val scrollingUp = exercisesListState.isScrollingUp()
    // Both buttons step out of the way while scrolling down through the list, and come back on any scroll up.
    val showAdd = scrollingUp || !exercisesListState.canScrollBackward
    val scrolledFar by remember {
        derivedStateOf { exercisesListState.firstVisibleItemIndex > 3 }
    }
    val showScrollToTop = scrollingUp && scrolledFar

    fun scrollExercisesToTop() {
        scope.launch { exercisesListState.animateScrollToItem(0) }
    }

    fun openEditor(id: Long?) {
        editingId = id
        editSession++
        currentScreen = AppScreen.EditExercise
    }

    // Where the editor was opened from: an existing exercise's page, or the list for a new one.
    fun closeEditor() {
        currentScreen = if (editingId == null) AppScreen.Exercises else AppScreen.ExerciseDetail
    }

    fun leaveEditor() {
        if (editViewModel?.hasUnsavedChanges() == true) askDiscard = true else closeEditor()
    }

    // A ringing break brings the user to it, and showing it is what stops the repeats. Mid-edit, the form stays put
    // so nothing typed is lost: the alert has already sounded in the open app, and the break waits on the Timer tab.
    LaunchedEffect(timerState?.ringing) {
        if (timerState?.ringing == true) {
            if (currentScreen != AppScreen.EditExercise) currentScreen = AppScreen.Timer
            timerViewModel.stopRinging()
        }
    }

    // Timer is the root, so back there falls through to the system and closes the app.
    BackHandler(enabled = currentScreen != AppScreen.Timer) {
        when (currentScreen) {
            AppScreen.EditExercise -> leaveEditor()
            AppScreen.ExerciseDetail -> currentScreen = AppScreen.Exercises
            else -> currentScreen = AppScreen.Timer
        }
    }

    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_body)) },
            confirmButton = {
                TextButton(onClick = {
                    askDiscard = false
                    closeEditor()
                }) { Text(stringResource(R.string.discard), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { askDiscard = false }) { Text(stringResource(R.string.keep_editing)) } }
        )
    }

    val title = when (currentScreen) {
        AppScreen.Timer -> stringResource(if (timerState?.onBreak == true) R.string.title_break else R.string.app_name)
        AppScreen.Exercises -> stringResource(R.string.tab_exercises)
        AppScreen.ExerciseDetail -> stringResource(R.string.title_exercise)
        AppScreen.EditExercise -> stringResource(if (editingId == null) R.string.new_exercise else R.string.edit_exercise)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = title,
                onBack = when (currentScreen) {
                    AppScreen.ExerciseDetail -> { { currentScreen = AppScreen.Exercises } }
                    AppScreen.EditExercise -> ::leaveEditor
                    else -> null
                },
                actions = {
                    if (currentScreen == AppScreen.ExerciseDetail) {
                        IconButton(onClick = { openEditor(detailId) }) {
                            Icon(painterResource(R.drawable.ic_edit), contentDescription = stringResource(R.string.edit_exercise))
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Hidden while editing, so a stray tap can't leave the form behind.
            if (currentScreen != AppScreen.EditExercise) {
                AppBottomBar(
                    // Detail is a sub-screen of Exercises.
                    current = if (currentScreen == AppScreen.Timer) AppDestination.Timer else AppDestination.Exercises,
                    onSelect = { selected ->
                        // Tapping the tab you're already on jumps back to the top of the list.
                        if (selected == AppDestination.Exercises && currentScreen == AppScreen.Exercises) {
                            scrollExercisesToTop()
                        }
                        currentScreen = when (selected) {
                            AppDestination.Timer -> AppScreen.Timer
                            AppDestination.Exercises -> AppScreen.Exercises
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.Exercises) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedVisibility(visible = showScrollToTop, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                        SmallFloatingActionButton(onClick = ::scrollExercisesToTop) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_upward),
                                contentDescription = stringResource(R.string.scroll_to_top)
                            )
                        }
                    }
                    AnimatedVisibility(visible = showAdd, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                        FloatingActionButton(onClick = { openEditor(null) }) {
                            Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_exercise))
                        }
                    }
                }
            }
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
                listState = exercisesListState,
                modifier = screenModifier
            )

            AppScreen.ExerciseDetail -> detailId?.let {
                ExerciseDetailScreen(exerciseId = it, viewModel = exercisesViewModel, modifier = screenModifier)
            }

            AppScreen.EditExercise -> editViewModel?.let {
                EditExerciseScreen(
                    exerciseId = editingId,
                    viewModel = it,
                    onSaved = { id ->
                        detailId = id
                        currentScreen = AppScreen.ExerciseDetail
                    },
                    onDeleted = {
                        detailId = null
                        currentScreen = AppScreen.Exercises
                    },
                    modifier = screenModifier
                )
            }
        }
    }
}

/** True while the list is still or moving toward the top; false while it's moving down. */
@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            val up = if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousOffset >= firstVisibleItemScrollOffset
            }
            previousIndex = firstVisibleItemIndex
            previousOffset = firstVisibleItemScrollOffset
            up
        }
    }.value
}
