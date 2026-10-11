package io.vonley.mi.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.ui.screens.consoles.data.repository.ConsoleRepositoryImpl
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import io.vonley.mi.ui.screens.ftp.data.repository.FTPRepositoryImpl
import io.vonley.mi.ui.screens.ftp.domain.repository.FTPRepository
import io.vonley.mi.ui.screens.packages.data.remote.PayloadSenderImpl
import io.vonley.mi.ui.screens.packages.data.repository.PackageRepositoryImpl
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadSender
import io.vonley.mi.ui.screens.packages.domain.repository.PackageRepository
import io.vonley.mi.ui.screens.settings.data.SettingsRepositoryImpl
import io.vonley.mi.ui.screens.settings.domain.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConsoleRepository(impl: ConsoleRepositoryImpl): ConsoleRepository

    @Binds
    @Singleton
    abstract fun bindFTPRepository(impl: FTPRepositoryImpl): FTPRepository

    @Binds
    @Singleton
    abstract fun bindPackageRepository(impl: PackageRepositoryImpl): PackageRepository

    @Binds
    @Singleton
    abstract fun bindPayloadSender(impl: PayloadSenderImpl): PayloadSender

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
