package io.vonley.mi.ui.screens.packages.domain.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo

interface PackageRepository {
    suspend fun getRepositories(): Resource<List<Repo>>
    suspend fun addRepository(repoLink: String): Resource<Repo>
    suspend fun searchRelevance(search: String): Resource<List<Repo>>
    suspend fun delete(link: String)

    /** Downloads [pkg]'s file for [url] into local staging. */
    suspend fun download(pkg: Package, url: String): Payload?
}
