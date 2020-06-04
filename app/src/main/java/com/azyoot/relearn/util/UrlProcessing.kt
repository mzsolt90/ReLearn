package com.azyoot.relearn.util

import android.net.Uri
import java.net.URLDecoder
import javax.inject.Inject

class UrlProcessing @Inject constructor() {
    fun stripFragmentFromUrl(string: String) = Uri.parse(string)
        .buildUpon()
        .fragment("")
        .build()
        .toString()

    fun ensureStartsWithHttpsScheme(string: String) = string.let {
        if (it.startsWith("https")) it
        else if (it.startsWith("http")) it.replace("http", "https")
        else "https://$it"
    }

    fun removeScheme(string: String) = try {
        Uri.parse(string).buildUpon()
            .scheme("")
            .build()
            .toString()
            .replace(Regex(":(//)?"), "")
    } catch (ex: IllegalArgumentException) {
        string
    }

    fun isValidUrl(string: String) = try {
        Uri.parse(string)?.let {
            it.host.isNullOrBlank().not() &&
                    it.scheme.isNullOrBlank().not() &&
                    it.host!!.split(".").size >= 2 &&
                    it.host!!.split(".").last().length >= 2
        } ?: false
    } catch (ex: IllegalArgumentException) {
        false
    }

    fun urlDecode(string: String) = URLDecoder.decode(string, "UTF-8")
}

