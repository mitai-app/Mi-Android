package io.vonley.mi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.vonley.mi.room.converters.*
import io.vonley.mi.ui.main.console.domain.model.Console
import io.vonley.mi.ui.compose.screens.packages.data.local.PackageRepositoryDao
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Package
import io.vonley.mi.ui.main.console.data.local.ConsoleDao

@Database(
    entities = [
        Console::class,
        Repo::class,
        Package::class
    ], version = 4, exportSchema = true
)
@TypeConverters(
    ConsoleTypeConverter::class,
    FeaturesConverter::class,
    ProtocolTypeConverter::class,
    CommonTypeConverter::class,
    PackageTypeConverter::class,
    PackageTypeConverter::class
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun consoleDao(): ConsoleDao
    abstract fun repoDao(): PackageRepositoryDao
}