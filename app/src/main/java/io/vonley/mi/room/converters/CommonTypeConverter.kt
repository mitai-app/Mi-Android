package io.vonley.mi.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.PackageType

class CommonTypeConverter {

    @TypeConverter
    fun toType(value: String): Map<String, String> = Gson().fromJson(value)

    @TypeConverter
    fun fromType(value: Map<String, String>) = Gson().toJson(value)

}