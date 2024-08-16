package io.vonley.mi.di.persistence.converters

import androidx.room.TypeConverter
import io.vonley.mi.ui.screens.packages.data.local.entity.PackageType

class PackageTypeConverter {

    @TypeConverter
    fun toType(value: Int): PackageType = enumValues<PackageType>()[value]

    @TypeConverter
    fun fromType(value: PackageType) = value.ordinal

}