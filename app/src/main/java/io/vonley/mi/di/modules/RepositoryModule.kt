package io.vonley.mi.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.ui.compose.screens.consoles.data.local.ConsoleDao
import io.vonley.mi.ui.compose.screens.consoles.data.remote.SyncService
import io.vonley.mi.ui.compose.screens.consoles.data.repository.ConsoleRepositoryImpl
import io.vonley.mi.ui.compose.screens.consoles.domain.repository.ConsoleRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


}