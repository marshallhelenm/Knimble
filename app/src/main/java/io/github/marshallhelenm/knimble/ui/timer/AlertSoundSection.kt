package io.github.marshallhelenm.knimble.ui.timer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.marshallhelenm.knimble.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertSoundSection(modifier: Modifier = Modifier, viewModel: AlertSoundViewModel = viewModel()) {
    val sound by viewModel.sound.collectAsStateWithLifecycle()
    val importFailed by viewModel.importFailed.collectAsStateWithLifecycle()

    val pickPhoneSound = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        result.data
            ?.let { IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java) }
            ?.let(viewModel::choosePhoneSound)
    }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::import)
    }
    val pickerTitle = stringResource(R.string.sound_title)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(R.drawable.ic_music_note),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.sound_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    if (sound.isBundled) stringResource(R.string.sound_bundled) else sound.name ?: stringResource(R.string.sound_untitled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = viewModel::preview) {
                Icon(painterResource(R.drawable.ic_play), contentDescription = stringResource(R.string.sound_play))
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, pickerTitle)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, if (sound.isBundled) null else sound.uri)
                try {
                    pickPhoneSound.launch(intent)
                } catch (_: ActivityNotFoundException) {
                    // Some trimmed-down Android builds ship no sound picker; importing a file still works there.
                }
            }) { Text(stringResource(R.string.sound_phone)) }
            if (viewModel.canImport) {
                OutlinedButton(onClick = { pickFile.launch("audio/*") }) { Text(stringResource(R.string.sound_file)) }
            }
            if (!sound.isBundled) {
                TextButton(onClick = viewModel::resetToBundled) { Text(stringResource(R.string.sound_reset)) }
            }
        }
        if (importFailed) {
            Text(stringResource(R.string.sound_import_failed), color = MaterialTheme.colorScheme.error)
        }
    }
}
