package io.vonley.mi.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.webman.WebManImplTest
import io.vonley.mi.di.network.protocols.webman.Webman
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebmanModule {

    @Provides
    @Singleton
    fun provideWebman(
        service: PSXService,
        @ApplicationContext context: Context
    ): Webman = WebManImplTest(service, context.assets)
}
