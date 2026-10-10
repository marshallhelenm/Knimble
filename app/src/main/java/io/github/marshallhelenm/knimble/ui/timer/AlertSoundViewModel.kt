package io.github.marshallhelenm.knimble.ui.timer

import android.app.Application
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.marshallhelenm.knimble.notifications.AlertSound
import io.github.marshallhelenm.knimble.notifications.BreakSound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertSoundViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private var playing: Ringtone? = null

    val canImport = BreakSound.canImport

    val sound: StateFlow<AlertSound> = BreakSound.changed
        .map { BreakSound.current(app) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BreakSound.current(app))

    private val _importFailed = MutableStateFlow(false)
    val importFailed: StateFlow<Boolean> = _importFailed.asStateFlow()

    fun choosePhoneSound(uri: Uri) {
        _importFailed.value = false
        viewModelScope.launch { BreakSound.choosePhoneSound(app, uri) }
    }

    fun import(source: Uri) {
        _importFailed.value = false
        viewModelScope.launch { _importFailed.value = !BreakSound.import(app, source) }
    }

    fun resetToBundled() {
        _importFailed.value = false
        BreakSound.resetToBundled(app)
    }

    fun preview() {
        playing?.stop()
        playing = RingtoneManager.getRingtone(app, sound.value.uri)?.also { it.play() }
    }

    override fun onCleared() {
        playing?.stop()
    }
}
