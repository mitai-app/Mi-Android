package io.vonley.mi.room.converters

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.ui.compose.screens.packages.data.local.entity.Package

class PackageConverter {

    @TypeConverter
    fun toType(features: String): Array<Package> {
        return GsonBuilder().create().fromJson<Array<Package>>(features)
    }

    @TypeConverter
    fun fromType(values: Array<Package>): String {
        return GsonBuilder().create().toJson(values)
    }

}