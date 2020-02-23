package com.azyoot.relearn.util

import android.net.Uri

fun String.stripFragmentFromUrl() = Uri.parse(this)
        .buildUpon()
        .fragment("")
        .build()
        .toString()