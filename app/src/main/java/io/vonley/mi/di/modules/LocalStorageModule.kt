package io.vonley.mi.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.common.files.LocalFileSource
import io.vonley.mi.common.files.LocalFileSourceImpl
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.persistence.AppDatabase
import io.vonley.mi.ui.screens.consoles.data.local.ConsoleDao
import io.vonley.mi.ui.screens.packages.data.local.PackageRepositoryDao
import io.vonley.mi.utils.SharedPreferenceManager
import io.vonley.mi.utils.SharedPreferenceManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalStorageModule {

    const val PREFERENCE_FILE: String = "mi.prefs"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "mi.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideConsoleDao(appDatabase: AppDatabase): ConsoleDao = appDatabase.consoleDao()

    @Provides
    @Singleton
    fun providePackageRepositoryDao(appDatabase: AppDatabase): PackageRepositoryDao = appDatabase.repoDao()

    @Provides
    @Singleton
    @SharedPreferenceStorage
    fun provideSharePreferenceManager(
        @ApplicationContext context: Context
    ): SharedPreferenceManager {
        val preferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        return SharedPreferenceManagerImpl(context, preferences)
    }

    @Provides
    @Singleton
    fun provideLocalFileSource(@ApplicationContext context: Context): LocalFileSource =
        LocalFileSourceImpl(context)
}
