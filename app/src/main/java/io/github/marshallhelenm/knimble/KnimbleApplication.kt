package io.github.marshallhelenm.knimble

import android.app.Application
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.exercises.BuiltInExercises
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KnimbleApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Every launch, so edits to the built-in list reach existing installs.
        appScope.launch {
            KnimbleDatabase.getInstance(this@KnimbleApplication).exerciseDao().syncBuiltIn(BuiltInExercises.asExercises())
        }
    }
}
