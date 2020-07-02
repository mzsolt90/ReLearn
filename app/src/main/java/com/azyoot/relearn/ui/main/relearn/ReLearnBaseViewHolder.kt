package com.azyoot.relearn.ui.main.relearn

import android.animation.Animator
import android.animation.AnimatorSet
import android.graphics.Rect
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.Group
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.util.getBoundingRect
import com.azyoot.relearn.util.setAlphaProper
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

sealed class ReLearnAction {
    object AcceptReLearn : ReLearnAction()
    object ViewReLearn : ReLearnAction()
    object AcceptAnimationFinished : ReLearnAction()
    object DeleteReLearn : ReLearnAction()
    data class SetExpanded(val isExpanded: Boolean) : ReLearnAction()
}

abstract class ReLearnBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var actionsListener: (action: ReLearnAction) -> Unit = {}

    private var boundState: ReLearnCardViewState = ReLearnCardViewState.Initial
    protected val isRevealed: Boolean
        get() = boundState.let { it is ReLearnCardViewState.ReLearnTranslationState && it.isRevealed }


    fun bind(state: ReLearnCardViewState){
        bind(state, boundState)
        boundState = state
    }


    fun reveal(group: Group, buttonView: View) {
        if(isRevealed) return
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
            val view = itemView.findViewById<View>(it)
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
        if(!isRevealed) return
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
            val view = itemView.findViewById<View>(it)
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

    abstract fun bind(newState: ReLearnCardViewState, oldState: ReLearnCardViewState)
}