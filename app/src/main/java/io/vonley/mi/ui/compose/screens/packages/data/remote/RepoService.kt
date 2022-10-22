package io.vonley.mi.ui.compose.screens.packages.data.remote

import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface RepoService {
    @GET
    suspend fun search(@Url url: String): Response<Repo>
}