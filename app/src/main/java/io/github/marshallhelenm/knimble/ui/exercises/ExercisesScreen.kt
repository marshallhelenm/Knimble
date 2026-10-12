package io.github.marshallhelenm.knimble.ui.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.ui.components.ExerciseImage
import io.github.marshallhelenm.knimble.ui.components.label
import io.github.marshallhelenm.knimble.ui.theme.color

@Composable
fun ExercisesScreen(viewModel: ExercisesViewModel, onOpen: (Exercise) -> Unit, modifier: Modifier = Modifier) {
    val byTier by viewModel.byTier.collectAsStateWithLifecycle()
    val areas by viewModel.areas.collectAsStateWithLifecycle()
    val availableAreas by viewModel.availableAreas.collectAsStateWithLifecycle()
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Text(
                stringResource(R.string.exercises_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item { AreaFilter(available = availableAreas, selected = areas, onToggle = viewModel::toggleArea) }
        byTier.forEach { (tier, exercises) ->
            item(key = tier.name) { TierHeader(tier, on = exercises.count { it.enabled }, total = exercises.size) }
            items(exercises, key = { it.id }) { exercise ->
                ExerciseRow(exercise, onOpen = { onOpen(exercise) }, onToggle = { viewModel.setEnabled(exercise, it) })
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun AreaFilter(available: List<BodyArea>, selected: Set<BodyArea>, onToggle: (BodyArea) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(available) { area ->
            FilterChip(
                selected = area in selected,
                onClick = { onToggle(area) },
                label = { Text(stringResource(area.label())) },
                leadingIcon = if (area in selected) {
                    { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
private fun TierHeader(tier: Tier, on: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(width = 6.dp, height = 28.dp).clip(RoundedCornerShape(3.dp)).background(tier.color()))
        Spacer(Modifier.width(10.dp))
        Text(stringResource(tier.label()), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.exercises_on_count, on, total),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseRow(exercise: Exercise, onOpen: () -> Unit, onToggle: (Boolean) -> Unit) {
    val toggleDescription = stringResource(R.string.exercise_toggle, exercise.name)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExerciseImage(exercise, large = false, modifier = Modifier.size(56.dp))
        Column(Modifier.weight(1f)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(
                exercise.tag,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = exercise.enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.semantics { contentDescription = toggleDescription }
        )
    }
}
