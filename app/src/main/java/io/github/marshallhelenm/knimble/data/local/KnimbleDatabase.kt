package io.github.marshallhelenm.knimble.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.TimerState

@Database(
    entities = [Exercise::class, TimerState::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KnimbleDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun timerStateDao(): TimerStateDao

    companion object {
        @Volatile
        private var INSTANCE: KnimbleDatabase? = null

        fun getInstance(context: Context): KnimbleDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KnimbleDatabase::class.java,
                    "knimble.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
