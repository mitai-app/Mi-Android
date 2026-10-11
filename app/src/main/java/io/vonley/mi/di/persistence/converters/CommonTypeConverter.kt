package io.vonley.mi.di.persistence.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import io.vonley.mi.extensions.fromJson

class CommonTypeConverter {

    @TypeConverter
    fun toType(value: String): Map<String, String> = Gson().fromJson(value)

    @TypeConverter
    fun fromType(value: Map<String, String>) = Gson().toJson(value)

}