package io.vonley.mi.room.converters

import androidx.room.TypeConverter
import io.vonley.mi.ui.compose.screens.packages.data.local.entity.PackageType

class PackageTypeConverter {

    @TypeConverter
    fun toType(value: Int): PackageType = enumValues<PackageType>()[value]

    @TypeConverter
    fun fromType(value: PackageType) = value.ordinal

}