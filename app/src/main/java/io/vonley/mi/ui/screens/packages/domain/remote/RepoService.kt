package io.vonley.mi.ui.screens.packages.domain.remote

import io.vonley.mi.ui.screens.packages.data.local.entity.Repo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RepoService {
    @GET
    suspend fun search(@Url url: String): Response<Repo>

    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}