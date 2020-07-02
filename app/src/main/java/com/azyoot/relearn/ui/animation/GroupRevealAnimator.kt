package com.azyoot.relearn.ui.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.graphics.Rect
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.Group
import androidx.core.animation.addListener
import com.azyoot.relearn.util.getBoundingRect
import com.azyoot.relearn.util.setAlphaProper
import javax.inject.Inject
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class GroupRevealAnimator @Inject constructor() {
    fun reveal(group: Group, buttonView: View) {
        val boundingRect = group.getBoundingRect().let {
            Rect(
                min(buttonView.left, it.left),
                min(buttonView.top, it.top),
                max(buttonView.right, it.right),
                max(buttonView.bottom, it.bottom)
            )
        }

        val startRadius =
            hypot((buttonView.width / 2).toDouble(), (buttonView.height / 2).toDouble())
        val finalRadius = hypot(boundingRect.width().toDouble(), boundingRect.height().toDouble())

        val animators = mutableListOf<Animator>()
        val set = AnimatorSet()

        group.referencedIds.forEach {
            val view = (group.parent as View).findViewById<View>(it)
            val dx = buttonView.x - view.x
            val dy = buttonView.y - view.y
            val cx = buttonView.width / 2 + dx
            val cy = buttonView.height / 2 + dy

            val anim =
                ViewAnimationUtils.createCircularReveal(
                    view,
                    cx.toInt(),
                    cy.toInt(),
                    startRadius.toFloat(),
                    finalRadius.toFloat()
                )
            animators.add(anim)
        }

        group.setAlphaProper(1F)

        set.playTogether(animators)
        set.start()
    }

    fun unreveal(group: Group, buttonView: View) {
        val boundingRect = group.getBoundingRect().let {
            Rect(
                min(buttonView.left, it.left),
                min(buttonView.top, it.top),
                max(buttonView.right, it.right),
                max(buttonView.bottom, it.bottom)
            )
        }

        val startRadius = hypot(boundingRect.width().toDouble(), boundingRect.height().toDouble())

        val animators = mutableListOf<Animator>()
        val set = AnimatorSet()

        group.referencedIds.forEach {
            val view = (group.parent as View).findViewById<View>(it)
            val dx = buttonView.x - view.x
            val dy = buttonView.y - view.y
            val cx = buttonView.width / 2 + dx
            val cy = buttonView.height / 2 + dy

            val anim =
                ViewAnimationUtils.createCircularReveal(
                    view,
                    cx.toInt(),
                    cy.toInt(),
                    startRadius.toFloat(),
                    0F
                )
            animators.add(anim)
        }

        set.addListener(onEnd = {
            group.setAlphaProper(0F)
        })
        set.playTogether(animators)
        set.start()
    }

}