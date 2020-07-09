package com.azyoot.relearn.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class SnackbarManager(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job + Dispatchers.Main)

    private var currentSnackBar: Snackbar? = null

    fun show(
        text: String,
        @StringRes actionTextRes: Int = 0,
        duration: Long = TimeUnit.SECONDS.toMillis(250),
        actionCallback: (View) -> Unit = {}
    ) {
        currentSnackBar?.dismiss()

        val snackbar = Snackbar.make(this, text, Snackbar.LENGTH_INDEFINITE)
        if (actionTextRes != 0) {
            snackbar.setAction(actionTextRes, actionCallback)
        }
        snackbar.show()
        currentSnackBar = snackbar
        scheduleHideSnackbar(snackbar, duration)
    }

    private fun scheduleHideSnackbar(snackbar: Snackbar, duration: Long) = coroutineScope.launch {
        delay(duration)
        currentSnackBar?.run {
            if (snackbar == this) {
                currentSnackBar?.dismiss()
                currentSnackBar = null
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        job.cancel()
        currentSnackBar = null
    }
}