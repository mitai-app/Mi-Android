package io.vonley.mi.room.converters

import androidx.room.TypeConverter
import io.vonley.mi.models.enums.Protocol

class ProtocolTypeConverter {

    @TypeConverter
    fun toType(value: String): Protocol = enumValueOf(value)

    @TypeConverter
    fun fromType(value: Protocol) = value.name

}