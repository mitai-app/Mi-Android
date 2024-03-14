package io.vonley.mi.ui.screens.consoles.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.di.persistence.converters.ConsoleTypeConverter
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.di.persistence.converters.FeaturesConverter
import java.net.Socket
import kotlin.coroutines.CoroutineContext

@Entity
@Parcelize
@TypeConverters(ConsoleTypeConverter::class, FeaturesConverter::class)
data class Console(
    @PrimaryKey override val ip: String,
    override var name: String,
    override var type: PlatformType,
    override var features: List<Feature> = emptyList(),
    override var lastKnownReachable: Boolean,
    override var wifi: String,
    override var pinned: Boolean = false
) : Client, Parcelable, CoroutineScope {

    @Ignore
    var socket: Socket? = null

    fun connect() {
        launch {
            socket = Socket()
        }
    }

    @Ignore
    private val job = Job()


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override fun toString(): String {
        return """
                    \nIP: $ip
                    Name: $name
                    Type: $type
                    Features: $features
                    Reachable: $lastKnownReachable
                """.trimIndent()
    }
}