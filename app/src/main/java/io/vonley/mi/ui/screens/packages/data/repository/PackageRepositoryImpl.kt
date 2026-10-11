package io.vonley.mi.ui.screens.packages.data.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.common.files.LocalFileSource
import io.vonley.mi.extensions.e
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.packages.data.local.PackageRepositoryDao
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo
import io.vonley.mi.ui.screens.packages.domain.remote.RepoService
import io.vonley.mi.ui.screens.packages.domain.repository.PackageRepository
import javax.inject.Inject

class PackageRepositoryImpl @Inject constructor(
    private val dao: PackageRepositoryDao,
    private val repo: RepoService,
    private val files: LocalFileSource,
) : PackageRepository {

    private suspend fun fetch(link: String): Repo? {
        val response = repo.search(link)
        if (!response.isSuccessful) return null
        val fetched = response.body() ?: return null
        fetched.link = link
        fetched.packages.forEach { p -> p.repo = link }
        if (dao.exists(link)) dao.update(fetched) else dao.insert(fetched)
        return fetched
    }

    override suspend fun addRepository(repoLink: String): Resource<Repo> {
        val fetched = runCatching { fetch(repoLink) }
            .onFailure { "Unable to fetch $repoLink: ${it.message}".e(TAG, it) }
            .getOrNull()
        return fetched?.let { Resource.Success(it) }
            ?: Resource.Error("Unable to load a repository from $repoLink")
    }

    override suspend fun searchRelevance(search: String): Resource<List<Repo>> {
        val query = search.trim().lowercase()
        val matches = dao.getAll().mapNotNull { repo ->
            val repoMatches = repo.title.lowercase().contains(query) || repo.author.lowercase().contains(query)
            val packages = repo.packages.filter { pkg ->
                pkg.name.lowercase().contains(query) || pkg.author.lowercase().contains(query)
            }
            when {
                repoMatches -> repo
                packages.isNotEmpty() -> repo.copy(packages = packages.toTypedArray())
                else -> null
            }
        }
        return Resource.Success(matches)
    }

    override suspend fun getRepositories(): Resource<List<Repo>> {
        if (dao.count() == 0 && runCatching { fetch(DEFAULT_REPO) }.getOrNull() == null) {
            return Resource.Error("Unable to load the default repository", emptyList())
        }
        return Resource.Success(dao.getAll())
    }

    override suspend fun download(pkg: Package, url: String): Payload? {
        val response = repo.downloadFile(url)
        val body = response.body() ?: return null
        return files.stage(pkg.name + pkg.fileExtension, body.byteStream())
    }

    override suspend fun delete(link: String) {
        dao.delete(link)
    }

    private companion object {
        const val TAG = "PackageRepository"
        const val DEFAULT_REPO = "https://raw.githubusercontent.com/mitai-app/versioning/main/packages.json"
    }
}
