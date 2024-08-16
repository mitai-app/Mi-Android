package io.vonley.mi.ui.screens.packages.presentation

import io.vonley.mi.ui.screens.packages.data.local.entity.Repo

sealed class PackageState {
    data class Success(val repos: List<Repo> = emptyList()): PackageState()
    data class Error(val error: String): PackageState()
    object Loading: PackageState()
}