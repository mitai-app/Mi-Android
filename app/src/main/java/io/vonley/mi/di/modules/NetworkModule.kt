package io.vonley.mi.di.modules

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.BuildConfig
import io.vonley.mi.di.annotations.GuestInterceptorOkHttpClient
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.impl.MiServerImpl
import io.vonley.mi.di.network.impl.PSXServiceImpl
import io.vonley.mi.di.network.protocols.ccapi.CCAPI
import io.vonley.mi.di.network.protocols.ccapi.CCAPIImpl
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.di.network.protocols.klog.KLogImpl
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPI
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIImpl
import io.vonley.mi.ui.screens.consoles.data.local.ConsoleDao
import io.vonley.mi.ui.screens.consoles.data.remote.SyncServiceImpl
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.ftp.data.remote.MiFTPClientImpl
import io.vonley.mi.ui.screens.ftp.domain.remote.MiFTPClient
import io.vonley.mi.ui.screens.packages.data.remote.RemotePackageInstallerImpl
import io.vonley.mi.ui.screens.packages.domain.remote.RemotePackageInstaller
import io.vonley.mi.ui.screens.packages.domain.remote.RepoService
import io.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Retrofit requires a base URL; every RepoService call passes a full @Url. */
    private const val BASE_URL = "http://localhost/"

    @Provides
    @Singleton
    @GuestInterceptorOkHttpClient
    fun provideGuestClient(@ApplicationContext context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(Cache(File(context.cacheDir, "http"), 20L * 1024 * 1024))
        if (BuildConfig.DEBUG) {
            // BODY logging buffers entire responses, which breaks streamed package downloads.
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRepoService(@GuestInterceptorOkHttpClient client: OkHttpClient): RepoService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setStrictness(Strictness.LENIENT).create()))
            .client(client)
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideSyncService(
        @ApplicationContext context: Context,
        dao: ConsoleDao,
        @SharedPreferenceStorage manager: SharedPreferenceManager,
        @GuestInterceptorOkHttpClient client: OkHttpClient
    ): SyncService = SyncServiceImpl(context, dao, manager, client)

    @Provides
    @Singleton
    fun providePSXService(impl: PSXServiceImpl): PSXService = impl

    @Provides
    @Singleton
    fun provideMiServer(
        @ApplicationContext context: Context,
        @SharedPreferenceStorage manager: SharedPreferenceManager,
        service: PSXService
    ): MiServer = MiServerImpl(context, manager, service)

    @Provides
    @Singleton
    fun provideFTPClient(
        @SharedPreferenceStorage manager: SharedPreferenceManager
    ): MiFTPClient = MiFTPClientImpl(manager)

    @Provides
    @Singleton
    fun provideRemotePackageInstaller(sync: SyncService): RemotePackageInstaller =
        RemotePackageInstallerImpl(sync)

    @Provides
    @Singleton
    fun provideCCAPI(service: PSXService): CCAPI = CCAPIImpl(service)

    @Provides
    @Singleton
    fun providePS3MAPI(service: PSXService): PS3MAPI = PS3MAPIImpl(service)

    @Provides
    @Singleton
    fun provideKLog(service: PSXService): KLog = KLogImpl(service)
}
