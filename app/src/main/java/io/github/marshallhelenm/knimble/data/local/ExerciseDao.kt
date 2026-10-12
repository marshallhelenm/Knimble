package io.github.marshallhelenm.knimble.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    suspend fun getAll(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observeById(id: Long): Flow<Exercise?>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Query("UPDATE exercises SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Insert
    suspend fun insert(exercise: Exercise): Long

    @Update
    suspend fun update(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE builtInKey IS NOT NULL")
    suspend fun getBuiltIn(): List<Exercise>

    @Query("DELETE FROM exercises WHERE builtInKey IS NOT NULL AND builtInKey NOT IN (:keys)")
    suspend fun deleteBuiltInExcept(keys: List<String>)

    @Query("UPDATE exercises SET source = 'USER', builtInKey = NULL WHERE source = 'EDITED' AND builtInKey NOT IN (:keys)")
    suspend fun detachEditedExcept(keys: List<String>)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Makes the built-in rows match [seeds] (matched on builtInKey): new ones are added, and existing ones take the
     * seed's content but keep their on/off state, unless the user has edited them. Ones no longer shipped are
     * removed, except edited ones, which become the user's own rather than losing their work.
     */
    @Transaction
    suspend fun syncBuiltIn(seeds: List<Exercise>) {
        val existing = getBuiltIn().associateBy { it.builtInKey }
        seeds.forEach { seed ->
            val row = existing[seed.builtInKey]
            when {
                row == null -> insert(seed)
                row.source == ExerciseSource.EDITED -> Unit
                else -> update(seed.copy(id = row.id, enabled = row.enabled))
            }
        }
        val shipped = seeds.mapNotNull { it.builtInKey }
        detachEditedExcept(shipped)
        deleteBuiltInExcept(shipped)
    }
}
