package com.azyoot.relearn.util

import android.content.Context
import android.util.DisplayMetrics

fun Context.dpToPx(dp: Int) =
    dp * resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT.toFloat()