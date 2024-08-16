package io.vonley.mi.di.persistence.converters

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.models.enums.Feature

class FeaturesConverter {

    @TypeConverter
    fun toType(features: String): List<Feature> {
        return GsonBuilder().create().fromJson<List<Int>>(features)
            .map { enumValues<Feature>()[it] }
    }

    @TypeConverter
    fun fromType(values: List<Feature>): String {
        val transform: (Feature) -> Int = { it.ordinal }
        val map = values.map(transform)
        return GsonBuilder().create().toJson(map)
    }

}