package io.github.marshallhelenm.knimble.ui.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.ui.components.ExerciseCredits
import io.github.marshallhelenm.knimble.ui.components.ExerciseImage
import io.github.marshallhelenm.knimble.ui.components.SafetyNote
import io.github.marshallhelenm.knimble.ui.components.TagPill
import io.github.marshallhelenm.knimble.ui.components.label
import io.github.marshallhelenm.knimble.ui.theme.TitleFontFamily

@Composable
fun ExerciseDetailScreen(exerciseId: Long, viewModel: ExercisesViewModel, modifier: Modifier = Modifier) {
    val exercise by remember(exerciseId) { viewModel.observe(exerciseId) }.collectAsState(initial = null)
    val current = exercise ?: return
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExerciseImage(current, large = true, modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f))
        Text(current.name, fontFamily = TitleFontFamily, fontSize = 36.sp, lineHeight = 44.sp, textAlign = TextAlign.Center)
        TagPill(current.tag, current.tier)
        Text(
            stringResource(current.tier.label()),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(current.description, style = MaterialTheme.typography.bodyLarge)
        SafetyNote()
        ExerciseCredits(current)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.exercise_use_in_breaks), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = current.enabled, onCheckedChange = { viewModel.setEnabled(current, it) })
        }
    }
}
