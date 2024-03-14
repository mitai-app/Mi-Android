package io.vonley.mi.common

sealed class Resource<T>(val status: String? = null) {
    class Success<T>(val data: T, status: String? = null) : Resource<T>(status)
    class Error<T>(status: String, val data: T? = null) : Resource<T>(status)
    class Loading<T>(val data: T? = null) : Resource<T>(status = "Loading")
}