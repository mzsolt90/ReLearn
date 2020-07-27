package com.azyoot.relearn.ui.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun CoroutineScope.runOnAnimationStart(animator: Animator, block: Animator.() -> Unit) {
    launch {
        suspendCancellableCoroutine<Unit> { continuation ->
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    continuation.resume(Unit)
                }
            })
        }
        animator.block()
    }
}

fun CoroutineScope.runOnAnimationEnd(animator: Animator, block: Animator.() -> Unit) {
    launch {
        suspendCancellableCoroutine<Unit> { continuation ->
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    continuation.resume(Unit)
                }
            })
        }
        animator.block()
    }
}