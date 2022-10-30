package io.vonley.mi.di.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.vonley.mi.di.persistence.converters.*
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.packages.data.local.PackageRepositoryDao
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.consoles.data.local.ConsoleDao

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