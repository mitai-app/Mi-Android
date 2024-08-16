package io.vonley.mi.ui.main.settings

import io.vonley.mi.common.base.BaseContract

interface SettingsContract {

    interface View : BaseContract.View {
        fun onCleared()
        fun initData()
    }

    interface Presenter : BaseContract.Presenter {
        fun clear()
        fun start()
        fun stop()
        fun restart()
    }

}