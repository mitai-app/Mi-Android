package io.vonley.mi.di.modules

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.ui.main.MainContract
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.main.console.presentation.ConsoleFragment
import io.vonley.mi.ui.main.console.presentation.adapters.ConsoleRecyclerAdapter
import io.vonley.mi.ui.main.console.presentation.sheets.ProtocolSheetFragment
import io.vonley.mi.ui.main.ftp.FTPFragment
import io.vonley.mi.ui.main.home.HomeFragment
import io.vonley.mi.ui.main.payload.PayloadFragment
import io.vonley.mi.ui.main.settings.SettingsFragment

@Module
@InstallIn(FragmentComponent::class)
object FragmentContractPresenter {

    @Provides
    fun provideSettingFragment(activity: Fragment): SettingsFragment {
        return activity as SettingsFragment
    }

    @Provides
    fun provideConsoleFragment(activity: Fragment): ConsoleFragment {
        return activity as ConsoleFragment
    }

    @Provides
    fun providePayloadFragment(activity: Fragment): PayloadFragment {
        return activity as PayloadFragment
    }

    @Provides
    fun provideHomeFragment(activity: Fragment): HomeFragment {
        return activity as HomeFragment
    }

    @Provides
    fun provideFTPFragment(activity: Fragment): FTPFragment {
        return activity as FTPFragment
    }

    @Provides
    fun provideConsoleOptionSheetFragment(activity: Fragment): ProtocolSheetFragment {
        return if (activity is ProtocolSheetFragment) activity else ProtocolSheetFragment()
    }

    @Provides
    @FragmentScoped
    fun provideConsoleRecyclerAdapter(
        view: MainContract.View,
        service: SyncService,
        sheet: ProtocolSheetFragment,
        fragment: ConsoleFragment,
        klog: KLog,
    ): ConsoleRecyclerAdapter {
        return ConsoleRecyclerAdapter(view, service, sheet, fragment.childFragmentManager, klog)
    }

}