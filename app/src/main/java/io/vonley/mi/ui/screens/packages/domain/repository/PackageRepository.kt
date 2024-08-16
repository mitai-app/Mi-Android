package io.vonley.mi.ui.screens.packages.domain.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo

interface PackageRepository {
    suspend fun getRepository(link: String): Resource<List<Repo>>
    suspend fun getRepositories(): Resource<List<Repo>>
    suspend fun addRepository(repoLink: String): Resource<Repo>
    suspend fun searchRelevance(search: String): Resource<List<Repo>>

    suspend fun exists(link: String): Boolean
    suspend fun delete(link: String)
    suspend fun count(): Int
    suspend fun getDefaultRepo(): Boolean
    suspend fun fromPackage(pkg: Package, key: String): Payload?
}