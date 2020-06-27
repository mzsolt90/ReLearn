package com.azyoot.relearn.ui.main.relearn

import android.animation.Animator
import android.animation.AnimatorSet
import android.graphics.Rect
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
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
    abstract fun bind(state: ReLearnCardViewState)

    abstract val isRevealed: Boolean

    var actionsListener: (action: ReLearnAction) -> Unit = {}

    private fun View.rect() = Rect(left, top, right, bottom)

    private fun Group.getBoundingRect() =
        referencedIds.fold((parent as View).findViewById<View>(referencedIds[0]).rect(),
            { rect: Rect, id: Int ->
                rect.let {
                    val view = (parent as View).findViewById<View>(id)
                    Rect(
                        min(view.left, it.left),
                        min(view.top, it.top),
                        max(view.right, it.right),
                        max(view.bottom, it.bottom)
                    )
                }
            })

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
            val view = itemView.findViewById<View>(it)
            val dx = buttonView.x - view.x
            val dy = buttonView.y - view.y
            val cx = buttonView.width / 2 + dx
            val cy = buttonView.height / 2 + dy

            view.visibility = View.VISIBLE
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

        group.visibility = View.VISIBLE

        set.addListener(onEnd = { actionsListener(ReLearnAction.SetExpanded(true)) })
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
            group.visibility = View.GONE
            actionsListener(ReLearnAction.SetExpanded(false))
        })
        set.playTogether(animators)
        set.start()
    }
}