package io.github.marshallhelenm.knimble.ui.timer

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.model.TimerState
import io.github.marshallhelenm.knimble.ui.components.label
import io.github.marshallhelenm.knimble.ui.theme.Cocoa
import io.github.marshallhelenm.knimble.ui.theme.color
import kotlinx.coroutines.delay
import java.util.Date

private val PRESET_MINUTES = listOf(15, 20, 30, 45, 60)
private const val STEP_MINUTES = 5
private const val MIN_MINUTES = 5
private const val MAX_MINUTES = 180

/** Shows whichever of setup, countdown or break the saved timer is in. */
@Composable
fun TimerScreen(
    state: TimerState,
    breakExercise: Exercise?,
    canScheduleExact: Boolean,
    onAllowExact: () -> Unit,
    onStart: (Int, Set<Tier>) -> Unit,
    onCancel: () -> Unit,
    onPickAnother: () -> Unit,
    onFinishBreak: () -> Unit,
    onChooseExercises: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.onBreak -> BreakContent(
            exercise = breakExercise,
            onPickAnother = onPickAnother,
            onStartAgain = { onStart(state.durationMinutes, state.tiers) },
            onFinish = onFinishBreak,
            onChooseExercises = onChooseExercises,
            modifier = modifier
        )
        state.endsAt != null -> RunningContent(state.endsAt, state.durationMinutes, state.tiers, onCancel, modifier)
        else -> SetupContent(state, canScheduleExact, onAllowExact, onStart, modifier)
    }
}

private val tierSetSaver = listSaver<Set<Tier>, String>(
    save = { tiers -> tiers.map { it.name } },
    restore = { names -> names.map(Tier::valueOf).toSet() }
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupContent(
    state: TimerState,
    canScheduleExact: Boolean,
    onAllowExact: () -> Unit,
    onStart: (Int, Set<Tier>) -> Unit,
    modifier: Modifier = Modifier
) {
    var minutes by rememberSaveable { mutableIntStateOf(state.durationMinutes) }
    var tiers by rememberSaveable(stateSaver = tierSetSaver) { mutableStateOf(state.tiers) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!canScheduleExact) ExactAlarmBanner(onAllowExact)

        Text(stringResource(R.string.setup_how_long), style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalIconButton(
                onClick = { minutes = (minutes - STEP_MINUTES).coerceAtLeast(MIN_MINUTES) },
                enabled = minutes > MIN_MINUTES
            ) {
                Icon(painterResource(R.drawable.ic_remove), contentDescription = stringResource(R.string.setup_shorter))
            }
            Text(
                text = stringResource(R.string.minutes, minutes),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            FilledTonalIconButton(
                onClick = { minutes = (minutes + STEP_MINUTES).coerceAtMost(MAX_MINUTES) },
                enabled = minutes < MAX_MINUTES
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.setup_longer))
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            PRESET_MINUTES.forEach { preset ->
                FilterChip(
                    selected = minutes == preset,
                    onClick = { minutes = preset },
                    label = { Text(stringResource(R.string.minutes_short, preset)) }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.setup_which_stretches), style = MaterialTheme.typography.titleMedium)
        Tier.entries.forEach { tier ->
            TierToggle(
                tier = tier,
                selected = tier in tiers,
                onToggle = { tiers = if (tier in tiers) tiers - tier else tiers + tier }
            )
        }
        if (tiers.isEmpty()) {
            Text(
                stringResource(R.string.setup_no_tiers),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { onStart(minutes, tiers) },
            enabled = tiers.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.start), style = MaterialTheme.typography.titleMedium)
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        AlertSoundSection()
    }
}

@Composable
private fun TierToggle(tier: Tier, selected: Boolean, onToggle: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onToggle,
        label = {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(stringResource(tier.label()), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(tier.hint()), style = MaterialTheme.typography.bodySmall)
            }
        },
        // A tick as well as the fill, so the choice doesn't rest on colour alone.
        leadingIcon = {
            if (selected) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Cocoa)
            } else {
                Box(Modifier.size(14.dp).clip(CircleShape).background(tier.color()))
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = tier.color(),
            selectedLabelColor = Cocoa
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun Tier.hint(): Int = when (this) {
    Tier.SMALL -> R.string.tier_small_hint
    Tier.MEDIUM -> R.string.tier_medium_hint
    Tier.LARGE -> R.string.tier_large_hint
}

@Composable
private fun ExactAlarmBanner(onAllowExact: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.exact_alarm_explanation), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onAllowExact, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.exact_alarm_button))
            }
        }
    }
}

@Composable
private fun RunningContent(endsAt: Long, durationMinutes: Int, tiers: Set<Tier>, onCancel: () -> Unit, modifier: Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(endsAt) {
        while (true) {
            now = System.currentTimeMillis()
            // Wake on the second boundary so the display doesn't skip or stutter.
            delay(1_000 - now % 1_000)
        }
    }
    val remaining = (endsAt - now).coerceAtLeast(0)
    val total = durationMinutes * 60_000f
    val endTime = DateFormat.getTimeFormat(LocalContext.current).format(Date(endsAt))
    val countdown = formatCountdown(remaining)
    val countdownDescription = stringResource(R.string.running_remaining, countdown)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.semantics { contentDescription = countdownDescription }) {
            CircularProgressIndicator(
                progress = { (remaining / total).coerceIn(0f, 1f) },
                modifier = Modifier.size(260.dp),
                strokeWidth = 14.dp,
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(countdown, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Medium)
        }
        Text(stringResource(R.string.running_ends_at, endTime), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.running_tiers, tiers.sortedBy { it.ordinal }.map { stringResource(it.label()) }.joinToString(", ")),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.cancel_timer)) }
    }
}

/** h:mm:ss for an hour or more, otherwise m:ss. */
internal fun formatCountdown(millis: Long): String {
    // Round up, so the display shows 0:00 only once time is really up.
    val totalSeconds = (millis + 999) / 1_000
    val hours = totalSeconds / 3_600
    val minutes = totalSeconds % 3_600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}
