package io.github.marshallhelenm.knimble.data.local

import androidx.room.TypeConverter
import io.github.marshallhelenm.knimble.model.Tier

class Converters {
    @TypeConverter
    fun tiersToString(tiers: Set<Tier>): String = tiers.joinToString(",") { it.name }

    @TypeConverter
    fun stringToTiers(value: String): Set<Tier> =
        if (value.isEmpty()) emptySet() else value.split(",").map(Tier::valueOf).toSet()
}
