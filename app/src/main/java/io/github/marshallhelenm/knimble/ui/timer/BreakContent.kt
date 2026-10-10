package io.github.marshallhelenm.knimble.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.ui.components.ExerciseCredits
import io.github.marshallhelenm.knimble.ui.components.ExerciseImage
import io.github.marshallhelenm.knimble.ui.components.SafetyNote
import io.github.marshallhelenm.knimble.ui.components.TagPill
import io.github.marshallhelenm.knimble.ui.components.TierAndAreas
import io.github.marshallhelenm.knimble.ui.theme.TitleFontFamily

@Composable
fun BreakContent(
    exercise: Exercise?,
    onPickAnother: () -> Unit,
    onStartAgain: () -> Unit,
    onFinish: () -> Unit,
    onChooseExercises: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (exercise == null) {
            Text(
                stringResource(R.string.break_nothing_on),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            OutlinedButton(onClick = onChooseExercises) { Text(stringResource(R.string.break_choose_exercises)) }
        } else {
            ExerciseImage(exercise, large = true, modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f))
            Text(
                exercise.name,
                fontFamily = TitleFontFamily,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                textAlign = TextAlign.Center
            )
            TagPill(exercise.tag, exercise.tier)
            TierAndAreas(exercise)
            Text(exercise.description, style = MaterialTheme.typography.bodyLarge)
            SafetyNote()
            ExerciseCredits(exercise)
            TextButton(onClick = onPickAnother) { Text(stringResource(R.string.break_pick_another)) }
        }
        Button(onClick = onStartAgain, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.break_start_again), style = MaterialTheme.typography.titleMedium)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onFinish) { Text(stringResource(R.string.break_done)) }
        }
    }
}
