package com.azyoot.relearn.util

import android.net.Uri

fun String.stripFragmentFromUrl() = Uri.parse(this)
    .buildUpon()
    .fragment("")
    .build()
    .toString()

fun String.ensureStartsWithHttpsScheme() = let {
    if(startsWith("https")) it
    else if(startsWith("http")) it.replace("http", "https")
    else "https://$it"
}

fun String.isValidUrl() = try {
    Uri.parse(this)?.let {
        it.host.isNullOrBlank().not() &&
                it.scheme.isNullOrBlank().not() &&
                it.host!!.contains("wiktionary", ignoreCase = true)
    } ?: false
} catch (ex: IllegalArgumentException) {
    false
}