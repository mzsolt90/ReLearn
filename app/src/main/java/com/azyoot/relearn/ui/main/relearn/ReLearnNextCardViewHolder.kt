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

    override val isExpanded: Boolean
        get() = viewBinding.groupShowHide.visibility == View.VISIBLE

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
        viewBinding.showHide.setOnClickListener {
            actionsListener(ReLearnAction.SetExpanded(!isExpanded))
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
            viewBinding.groupShowHide.visibility = if(state.isExpanded) View.VISIBLE else View.INVISIBLE

            viewBinding.showHide.setIconResource(if(state.isExpanded) R.drawable.ic_visible else R.drawable.ic_invisible)

            bindTranslationData(state.reLearnTranslation)

            if (state.relearnState is ReLearnCardReLearnState.Accepted) {
                animateOutActions()
            }
        }
    }

    private fun animateOutActions() {
        if (viewBinding.groupActions.visibility != View.VISIBLE) return
        viewBinding.showHide.isEnabled = false

        val originalButtonTopMargin =
            (viewBinding.buttonAccept.layoutParams as ConstraintLayout.LayoutParams).topMargin

        val slideUpPx =
            viewBinding.buttonAccept.height +
                    originalButtonTopMargin +
                    (viewBinding.buttonAccept.layoutParams as ConstraintLayout.LayoutParams).bottomMargin
        //we're going to compensate for the minimum height of the scene and the new state will not
        //scroll up the entire height of the hidden view
        val originalHeightOverflow =
            viewBinding.scene.height - viewBinding.scene.minHeight
        val animationHeightCorrectionRatio = if (originalHeightOverflow < slideUpPx) {
            1F / (1F + (slideUpPx - originalHeightOverflow) / slideUpPx)
        } else 1F

        //we're going to animate the bottom margin of the remaining view (the translation text)
        //first we need to initialize it
        val translationLayoutParams =
            viewBinding.sourceTranslation.layoutParams as ConstraintLayout.LayoutParams
        val originalBottomMargin = translationLayoutParams.bottomMargin
        translationLayoutParams.bottomMargin = originalBottomMargin + slideUpPx
        viewBinding.sourceTranslation.layoutParams = translationLayoutParams

        val constraintSet = ConstraintSet()
        constraintSet.clone(viewBinding.scene)
        //this will detach the buttons from the translation so it no longer prevents the card from collapsing
        constraintSet.clear(R.id.button_accept, ConstraintSet.TOP)
        constraintSet.clear(R.id.button_delete, ConstraintSet.TOP)
        constraintSet.applyTo(viewBinding.scene)

        val fade = ObjectAnimator.ofFloat(1F, 0F)
            .apply {
                duration = (TRANSITION_DURATION_MS / 2).toLong()
                addUpdateListener {
                    viewBinding.buttonAccept.alpha = it.animatedValue as Float
                    viewBinding.buttonDelete.alpha = it.animatedValue as Float
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
                    setConstraintsAfterAcceptAnimation(constraintSet, originalButtonTopMargin, originalBottomMargin)
                    actionsListener(ReLearnAction.AcceptAnimationFinished)
                }
            })
        }

        val set = AnimatorSet()
        set.playSequentially(fade, move)
        set.start()
    }

    private fun connectButtonToTranslationAndSetAlpha(constraintSet: ConstraintSet, buttonId: Int, originalTopMargin: Int){
        constraintSet.setMargin(
            buttonId,
            ConstraintSet.TOP,
            originalTopMargin
        )
        constraintSet.setAlpha(buttonId, 1F)
        constraintSet.connect(
            buttonId,
            ConstraintSet.TOP,
            R.id.source_translation,
            ConstraintSet.BOTTOM
        )
    }

    private fun setConstraintsAfterAcceptAnimation(constraintSet: ConstraintSet, originalTopMargin: Int, originalBottomMargin: Int){
        viewBinding.groupActions.visibility = View.GONE
        viewBinding.showHide.isEnabled = true

        constraintSet.setMargin(
            R.id.source_translation,
            ConstraintSet.BOTTOM,
            originalBottomMargin
        )

       connectButtonToTranslationAndSetAlpha(constraintSet, R.id.button_accept, originalTopMargin)
       connectButtonToTranslationAndSetAlpha(constraintSet, R.id.button_delete, originalTopMargin)

        constraintSet.applyTo(viewBinding.scene)

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