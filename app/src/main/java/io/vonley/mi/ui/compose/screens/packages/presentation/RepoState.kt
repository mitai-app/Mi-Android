package io.vonley.mi.ui.compose.screens.packages.presentation

import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo

data class RepoState(
    var loading: Boolean = false,
    var repos: List<Repo> = emptyList(),
    var error: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RepoState) return false

        if (loading != other.loading) return false
        if (repos != other.repos) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loading.hashCode()
        result = 31 * result + repos.hashCode()
        result = 31 * result + error.hashCode()
        return result
    }
}