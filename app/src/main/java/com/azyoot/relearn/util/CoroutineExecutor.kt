package com.azyoot.relearn.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

fun CoroutineScope.executor() = object : Executor {
    override fun execute(command: Runnable) {
        this@executor.launch {
            command.run()
        }
    }
}