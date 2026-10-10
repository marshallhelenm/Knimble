package io.github.marshallhelenm.knimble.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.marshallhelenm.knimble.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    suspend fun getAll(): List<Exercise>

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

    /**
     * Makes the built-in rows match [seeds] (matched on builtInKey): new ones are added, existing ones take the
     * seed's text and tier but keep their on/off state, and ones no longer shipped are removed.
     */
    @Transaction
    suspend fun syncBuiltIn(seeds: List<Exercise>) {
        val existing = getBuiltIn().associateBy { it.builtInKey }
        seeds.forEach { seed ->
            val row = existing[seed.builtInKey]
            if (row == null) insert(seed) else update(seed.copy(id = row.id, enabled = row.enabled))
        }
        deleteBuiltInExcept(seeds.mapNotNull { it.builtInKey })
    }
}
