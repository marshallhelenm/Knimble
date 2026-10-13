package io.github.marshallhelenm.knimble.ui.exercises

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.exercises.PhotoDraft
import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.Tier
import io.github.marshallhelenm.knimble.ui.components.ExercisePicture
import io.github.marshallhelenm.knimble.ui.components.label
import io.github.marshallhelenm.knimble.ui.components.placeholderFor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditExerciseScreen(
    exerciseId: Long?,
    viewModel: EditExerciseViewModel,
    onSaved: (Long) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(exerciseId) { viewModel.load(exerciseId) }
    if (!viewModel.loaded) return

    val form = viewModel.form
    val errors = if (viewModel.showErrors) form.errors() else emptySet()
    var confirm by rememberSaveable { mutableStateOf<Confirm?>(null) }
    // The multi-photo picker needs room for at least two, so the last free slot uses the single picker.
    val pickSeveral = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxOf(2, viewModel.photoSlotsLeft))
    ) { uris -> if (uris.isNotEmpty()) viewModel.addPhotos(uris) }
    val pickOne = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.addPhotos(listOf(it)) }
    }
    val imagesOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (viewModel.photos.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExercisePicture(
                    photo = null,
                    placeholder = placeholderFor(form.areas),
                    large = false,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { pickSeveral.launch(imagesOnly) }) { Text(stringResource(R.string.edit_add_photos)) }
                    Text(
                        stringResource(R.string.edit_photo_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.photos.forEachIndexed { index, draft ->
                    Box {
                        ExercisePicture(
                            photo = when (draft) {
                                is PhotoDraft.Stored -> draft.uri.toUri()
                                is PhotoDraft.Picked -> draft.source
                            },
                            placeholder = placeholderFor(form.areas),
                            large = false,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        FilledTonalIconButton(
                            onClick = { viewModel.removePhoto(index) },
                            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(28.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.edit_remove_photo, index + 1),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (viewModel.photoSlotsLeft > 0) {
                    OutlinedButton(onClick = {
                        if (viewModel.photoSlotsLeft == 1) pickOne.launch(imagesOnly) else pickSeveral.launch(imagesOnly)
                    }) { Text(stringResource(R.string.edit_add_photos)) }
                }
                Text(
                    stringResource(if (viewModel.photoSlotsLeft > 0) R.string.edit_photos_hint else R.string.edit_photos_full),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (viewModel.photoFailed) {
            Text(stringResource(R.string.edit_photo_failed), color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = form.name,
            onValueChange = { viewModel.form = form.copy(name = it) },
            label = { Text(stringResource(R.string.edit_name)) },
            isError = FormError.NAME_MISSING in errors,
            supportingText = if (FormError.NAME_MISSING in errors) {
                { Text(stringResource(R.string.edit_name_missing)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.tag,
            onValueChange = { viewModel.form = form.copy(tag = it) },
            label = { Text(stringResource(R.string.edit_tag)) },
            supportingText = { Text(stringResource(R.string.edit_tag_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.edit_tier), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tier.entries.forEach { tier ->
                FilterChip(
                    selected = form.tier == tier,
                    onClick = { viewModel.form = form.copy(tier = tier) },
                    label = { Text(stringResource(tier.label())) }
                )
            }
        }

        Text(stringResource(R.string.edit_areas), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BodyArea.entries.forEach { area ->
                FilterChip(
                    selected = area in form.areas,
                    onClick = {
                        viewModel.form = form.copy(areas = if (area in form.areas) form.areas - area else form.areas + area)
                    },
                    label = { Text(stringResource(area.label())) }
                )
            }
        }

        OutlinedTextField(
            value = form.description,
            onValueChange = { viewModel.form = form.copy(description = it) },
            label = { Text(stringResource(R.string.edit_description)) },
            isError = FormError.DESCRIPTION_MISSING in errors,
            supportingText = if (FormError.DESCRIPTION_MISSING in errors) {
                { Text(stringResource(R.string.edit_description_missing)) }
            } else {
                null
            },
            minLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.edit_sources), style = MaterialTheme.typography.titleSmall)
        Text(
            stringResource(R.string.edit_sources_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        form.credits.forEachIndexed { index, credit ->
            fun update(changed: CreditDraft) {
                viewModel.form = form.copy(credits = form.credits.toMutableList().also { it[index] = changed })
            }
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = credit.name,
                        onValueChange = { update(credit.copy(name = it)) },
                        label = { Text(stringResource(R.string.edit_source_name)) },
                        isError = FormError.SOURCE_NAME_MISSING in errors && credit.name.isBlank() && credit.url.isNotBlank(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = credit.url,
                        onValueChange = { update(credit.copy(url = it)) },
                        label = { Text(stringResource(R.string.edit_source_link)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconButton(onClick = { viewModel.form = form.copy(credits = form.credits.filterIndexed { i, _ -> i != index }) }) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.edit_remove_source))
                }
            }
        }
        if (FormError.SOURCE_NAME_MISSING in errors) {
            Text(stringResource(R.string.edit_source_name_missing), color = MaterialTheme.colorScheme.error)
        }
        if (FormError.SOURCE_LINK_INVALID in errors) {
            Text(stringResource(R.string.edit_source_link_invalid), color = MaterialTheme.colorScheme.error)
        }
        TextButton(onClick = { viewModel.form = form.copy(credits = form.credits + CreditDraft()) }) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.edit_add_source))
        }

        Button(
            onClick = { viewModel.save(onSaved) },
            enabled = !viewModel.busy,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
        }

        if (viewModel.canReset || viewModel.canDelete) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        if (viewModel.canReset) {
            TextButton(onClick = { confirm = Confirm.RESET }) { Text(stringResource(R.string.edit_reset)) }
        }
        if (viewModel.canDelete) {
            TextButton(onClick = { confirm = Confirm.DELETE }) {
                Text(stringResource(R.string.edit_delete), color = MaterialTheme.colorScheme.error)
            }
        }
    }

    when (confirm) {
        Confirm.DELETE -> ConfirmDialog(
            title = stringResource(R.string.edit_delete_title, viewModel.original?.name.orEmpty()),
            body = stringResource(R.string.edit_delete_body),
            action = stringResource(R.string.edit_delete),
            destructive = true,
            onConfirm = {
                confirm = null
                viewModel.delete(onDeleted)
            },
            onDismiss = { confirm = null }
        )
        Confirm.RESET -> ConfirmDialog(
            title = stringResource(R.string.edit_reset_title),
            body = stringResource(R.string.edit_reset_body),
            action = stringResource(R.string.edit_reset),
            destructive = false,
            onConfirm = {
                confirm = null
                viewModel.resetToOriginal { viewModel.original?.let { onSaved(it.id) } }
            },
            onDismiss = { confirm = null }
        )
        null -> Unit
    }
}

private enum class Confirm { DELETE, RESET }

@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    action: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(action, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
