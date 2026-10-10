package io.github.marshallhelenm.knimble.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.marshallhelenm.knimble.model.TimerState
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerStateDao {
    @Query("SELECT * FROM timer_state WHERE id = 1")
    suspend fun get(): TimerState?

    @Query("SELECT * FROM timer_state WHERE id = 1")
    fun observe(): Flow<TimerState?>

    @Upsert
    suspend fun save(state: TimerState)
}
