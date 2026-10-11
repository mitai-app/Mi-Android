package io.vonley.mi.ui.screens.consoles.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import io.vonley.mi.di.persistence.converters.ConsoleTypeConverter
import io.vonley.mi.di.persistence.converters.FeaturesConverter
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType

@Entity
@TypeConverters(ConsoleTypeConverter::class, FeaturesConverter::class)
data class Console(
    @PrimaryKey override val ip: String,
    override val name: String,
    override val type: PlatformType,
    override val features: List<Feature> = emptyList(),
    override val lastKnownReachable: Boolean,
    override val wifi: String,
    override val pinned: Boolean = false
) : Client
