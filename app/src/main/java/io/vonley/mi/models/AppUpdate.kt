package io.vonley.mi.models

import io.vonley.mi.utils.Semver

/** Latest release metadata published at the versioning repo. */
data class AppUpdate(val version: String, val changes: String, val build: String)

val String.ver: Semver
    get() = Semver(this)
