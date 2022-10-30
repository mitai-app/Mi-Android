package io.vonley.mi.ui.screens.packages.data.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.ui.screens.packages.data.local.PackageRepositoryDao
import io.vonley.mi.ui.screens.packages.domain.remote.RepoService
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo
import io.vonley.mi.ui.screens.packages.domain.repository.PackageRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PackageRepositoryImpl @Inject constructor(
    private val dao: PackageRepositoryDao,
    private val repo: RepoService
) : PackageRepository {

    private suspend fun fetch(link: String, save: Boolean = true): Repo? {
        val search = repo.search(link)
        if (search.isSuccessful) {
            search.body()?.let {
                it.link = link
                it.packages.forEach { p ->
                    p.repo = link
                }
                if (save) {
                    if (!exists(link)) {
                        dao.insert(it)
                    } else {
                        dao.update(it)
                    }
                }
                return it
            } ?: run {
                // could not resolve
            }
        }
        return null
    }


    override suspend fun addRepository(repoLink: String): Resource<Repo> {
        val fetch = fetch(repoLink, true)
        return fetch?.let { repo ->
            Resource.Success(repo)
        } ?: run {
            Resource.Success(dao.get(repoLink).first())
        }
    }

    override suspend fun searchRelevance(search: String): Resource<List<Repo>> {
        val single = dao.getByName(search).first()
        return Resource.Success(single)
    }

    override suspend fun getRepository(link: String): Resource<List<Repo>> {
        return if (fetch(link) != null) {
            val get = dao.get(link)
            return Resource.Success(get)
        } else {
            Resource.Error("Error", null)
        }
    }

    override suspend fun count(): Int {
        return dao.count()
    }

    override suspend fun getRepositories(): Resource<List<Repo>> {
        if (dao.count() == 0) {
            if (!getDefaultRepo()) {
                return Resource.Error("No value", emptyList())
            }
        }
        return Resource.Success(dao.getAll())
    }


    override suspend fun getDefaultRepo(): Boolean {
        return fetch("https://raw.githubusercontent.com/mitai-app/versioning/main/packages.json") != null
    }

    override suspend fun exists(link: String): Boolean {
        return dao.exists(link)
    }

    override suspend fun delete(link: String) {
        return dao.delete(link)
    }

}