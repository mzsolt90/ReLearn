package com.azyoot.relearn.ui.main.relearn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.azyoot.relearn.R
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.common.ReLearnTranslationFormatter
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber

class ReLearnNextCardViewHolder @AssistedInject constructor(
    private val reLearnTranslationFormatter: ReLearnTranslationFormatter,
    @Assisted private val viewBinding: ItemRelearnCardBinding
) :
    ReLearnBaseViewHolder(viewBinding.root) {

    init {
        viewBinding.buttonAccept.setOnClickListener {
            actionsListener(ReLearnAction.AcceptReLearn)
        }
        viewBinding.card.setOnClickListener {
            actionsListener(ReLearnAction.ViewReLearn)
        }
        viewBinding.buttonDelete.setOnClickListener {
            actionsListener(ReLearnAction.DeleteReLearn)
        }
    }

    override fun bind(state: ReLearnCardViewState) {
        Timber.d("Binding state $state")

        when (state) {
            is ReLearnCardViewState.Loading, is ReLearnCardViewState.Initial -> {
                viewBinding.groupProgress.visibility = View.VISIBLE
                viewBinding.groupLoaded.visibility = View.GONE
            }
            else -> {
                viewBinding.groupProgress.visibility = View.GONE
                viewBinding.groupLoaded.visibility = View.VISIBLE
            }
        }

        if (state is ReLearnCardViewState.ReLearnTranslationState) {
            viewBinding.groupActions.visibility = View.VISIBLE
            bindTranslationData(state.reLearnTranslation)
        }

        if (state is ReLearnCardViewState.Accepted) {
            animateOutActions()
        }
    }

    private fun animateOutActions() {
        if (viewBinding.groupActions.visibility != View.VISIBLE) return

        val originalTopMargin =
            (viewBinding.buttonAccept.layoutParams as ConstraintLayout.LayoutParams).topMargin

        val slideUpPx =
            viewBinding.buttonAccept.height +
                    (viewBinding.buttonAccept.layoutParams as ConstraintLayout.LayoutParams).topMargin
        (viewBinding.buttonAccept.layoutParams as ConstraintLayout.LayoutParams).bottomMargin
        val originalHeightOverflow =
            viewBinding.scene.height - viewBinding.scene.minHeight
        val animationHeightCorrectionRatio = if (originalHeightOverflow < slideUpPx) {
            1F / (1F + (slideUpPx - originalHeightOverflow) / slideUpPx)
        } else 1F

        val translationLayoutParams =
            viewBinding.sourceTranslation.layoutParams as ConstraintLayout.LayoutParams
        val originalBottomMargin = translationLayoutParams.bottomMargin
        translationLayoutParams.bottomMargin = originalBottomMargin + slideUpPx
        viewBinding.sourceTranslation.layoutParams = translationLayoutParams

        val constraintSet = ConstraintSet()
        constraintSet.clone(viewBinding.scene)
        constraintSet.clear(R.id.button_accept, ConstraintSet.TOP)
        constraintSet.applyTo(viewBinding.scene)

        val fade = ObjectAnimator.ofFloat(1F, 0F)
            .apply {
                duration = (TRANSITION_DURATION_MS / 2).toLong()
                addUpdateListener {
                    viewBinding.buttonAccept.alpha = it.animatedValue as Float
                }
            }

        val move = ObjectAnimator.ofInt(slideUpPx, 0).apply {
            duration = (TRANSITION_DURATION_MS / 2 * animationHeightCorrectionRatio).toLong()
            addUpdateListener {
                translationLayoutParams.bottomMargin =
                    originalBottomMargin + it.animatedValue as Int
                viewBinding.sourceTranslation.layoutParams = translationLayoutParams
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    viewBinding.groupActions.visibility = View.GONE

                    constraintSet.setMargin(
                        R.id.source_translation,
                        ConstraintSet.BOTTOM,
                        originalBottomMargin
                    )

                    constraintSet.setMargin(
                        R.id.button_accept,
                        ConstraintSet.TOP,
                        originalTopMargin
                    )
                    constraintSet.setAlpha(R.id.button_accept, 1F)
                    constraintSet.connect(
                        R.id.button_accept,
                        ConstraintSet.TOP,
                        R.id.source_translation,
                        ConstraintSet.BOTTOM
                    )

                    constraintSet.applyTo(viewBinding.scene)

                    actionsListener(ReLearnAction.AcceptAnimationFinished)
                }
            })
        }

        val set = AnimatorSet()
        set.playSequentially(fade, move)
        set.start()
    }

    private fun bindTranslationData(reLearnTranslation: ReLearnTranslation) {
        viewBinding.sourceTitle.text = reLearnTranslation.sourceText
        viewBinding.sourceTranslation.text =
            reLearnTranslationFormatter.formatTranslationTextForNotification(reLearnTranslation)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(viewBinding: ItemRelearnCardBinding): ReLearnNextCardViewHolder
    }

    companion object {
        private const val TRANSITION_DURATION_MS = 400
    }
}