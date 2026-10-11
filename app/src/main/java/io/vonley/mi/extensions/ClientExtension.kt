package io.vonley.mi.extensions

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

inline fun <reified T> Gson.fromJson(json: String): T =
    fromJson(json, object : TypeToken<T>() {}.type)

inline fun <reified T> String.fromJson(): T? =
    GsonBuilder().create().fromJson<T>(this, object : TypeToken<T>() {}.type)

inline fun <reified T> T.toJson(): String =
    GsonBuilder().create().toJson(this)
