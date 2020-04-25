package com.azyoot.relearn.util

import android.net.Uri

fun String.stripFragmentFromUrl() = Uri.parse(this)
    .buildUpon()
    .fragment("")
    .build()
    .toString()

fun String.isValidUrl() = try {
    Uri.parse(this)?.let {
        it.host.isNullOrBlank().not() &&
                it.scheme.isNullOrBlank().not()
    } ?: false
} catch (ex: IllegalArgumentException) {
    false
}