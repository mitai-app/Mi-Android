package io.vonley.mi.room.converters

import androidx.room.TypeConverter
import io.vonley.mi.models.enums.PlatformType

class ConsoleTypeConverter {

    @TypeConverter
    fun toType(value: Int): PlatformType = enumValues<PlatformType>()[value]

    @TypeConverter
    fun fromType(value: PlatformType) = value.ordinal

}