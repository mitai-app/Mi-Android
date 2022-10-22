package io.vonley.mi.ui.compose.screens.packages.data.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.ui.compose.screens.packages.data.local.PackageRepositoryDao
import io.vonley.mi.ui.compose.screens.packages.data.remote.RepoService
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import io.vonley.mi.ui.compose.screens.packages.domain.repository.PackageRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class PackageRepositoryImpl @Inject constructor(
    private val dao: PackageRepositoryDao,
    private val repo: RepoService
) : PackageRepository {

    private suspend fun fetch(link: String): Boolean {
        val search = repo.search(link)
        if (search.isSuccessful) {
            search.body()?.let {
                it.link = link
                it.packages.forEach { p ->
                    p.repo = link
                }
                if (!exists(link)) {
                    dao.insert(it)
                } else {
                    dao.update(it)
                }
                return true
            } ?: run {
                // could not resolve
            }
        }
        return false
    }

    override suspend fun getRepository(link: String): Flow<Resource<List<Repo>>> {
        return if (fetch(link)) {
            val get = dao.get(link)
            flow {
                val data = get.single()
                emit(Resource.Success(data))
            }
        } else {
            flow {
                emit(Resource.Error("Error", null))
            }
        }
    }

    override suspend fun getRepositories(): Flow<Resource<List<Repo>>> {
        if(dao.count() == 0) {
            if (!getDefaultRepo()) {
                return flow {
                    emit(Resource.Error("No value", emptyList()))
                }
            }
        } else {
            val all = dao.getAll()
            all.single().forEach {
                fetch(it.link)
            }
        }
        return flow {
            dao.getAll().onEach {
                emit(Resource.Success(it))
            }.collect()
        }
    }

    private suspend fun getDefaultRepo(): Boolean {
        return fetch("https://raw.githubusercontent.com/mitai-app/versioning/main/packages.json")
    }

    override suspend fun exists(link: String): Boolean {
        return dao.exists(link)
    }

    override suspend fun delete(link: String) {
        return dao.delete(link)
    }

}