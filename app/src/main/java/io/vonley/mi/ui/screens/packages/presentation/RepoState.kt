package io.vonley.mi.ui.screens.packages.presentation

import io.vonley.mi.ui.screens.packages.data.local.entity.Repo

sealed class RepoState {
    data class Success(val repos: List<Repo> = emptyList()): RepoState()
    data class Error(val error: String): RepoState()
    object Loading: RepoState()
}