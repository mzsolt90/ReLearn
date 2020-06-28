package com.azyoot.relearn.ui.main

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.di.ui.LifecycleScopedFactory
import com.azyoot.relearn.domain.config.MAX_HISTORY
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.common.AndroidEffectsProducer
import com.azyoot.relearn.ui.common.ViewEffectsProducer
import com.azyoot.relearn.ui.common.ViewModelsList
import com.azyoot.relearn.ui.common.lifecycleScoped
import com.azyoot.relearn.ui.main.relearn.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.math.min

sealed class ReLearnAdapterEffect {
    data class LaunchReLearnEffect(val reLearnTranslation: ReLearnTranslation) :
        ReLearnAdapterEffect()

    data class ReLearnDeletedEffect(val relearn: ReLearnTranslation, val position: Int) :
        ReLearnAdapterEffect()

    object ShowNextReLearnEffect : ReLearnAdapterEffect()
}

@ExperimentalCoroutinesApi
class ReLearnAdapter @AssistedInject constructor(
    private val nextReLearnCardFactory: ReLearnNextCardViewHolder.Factory,
    private val historyReLearnCardFactory: ReLearnHistoryCardViewHolder.Factory,
    private val coroutineScope: CoroutineScope,
    private val viewModelsFactory: ViewModelProvider.Factory,
    private val lifecycleScopedFactory: LifecycleScopedFactory,
    @Assisted private val sourceCount: Int,
    @Assisted viewModelStoreOwner: ViewModelStoreOwner
) : RecyclerView.Adapter<ReLearnBaseViewHolder>(),
    ViewEffectsProducer<ReLearnAdapterEffect> by AndroidEffectsProducer() {

    private data class HolderAndBindingJob(val holder: ReLearnBaseViewHolder, val job: Job)

    private val viewModelsList: ViewModelsList<ReLearnCardViewState, ReLearnCardEffect, ReLearnCardViewModel>
            by viewModelStoreOwner.lifecycleScoped {
                lifecycleScopedFactory
            }

    private val bindingJobs = SparseArray<HolderAndBindingJob>()

    init {
        Timber.v("Setting up viewmodels")
        repeat(itemCount) {
            viewModelsList.add(viewModelsFactory.create(ReLearnCardViewModel::class.java))
        }

        viewModelsList.getEffects().onEach {
            val effect = it.effect
            if (effect is ReLearnCardEffect.ReLearnDeleted) {
                Timber.v("Relearn effect @ position ${it.position}: ${effect.reLearnTranslation.sourceText} - $effect")
            } else {
                Timber.v("Relearn effect @ position ${it.position}: $effect")
            }

            handleEffect(it.effect, it.position)
        }.launchIn(coroutineScope)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            ITEM_TYPE_NEXT -> nextReLearnCardFactory.create(
                ItemRelearnCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            ITEM_TYPE_HISTORY -> historyReLearnCardFactory.create(
                ItemRelearnHistoryCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw NotImplementedError("View type is not supported")
        }

    override fun getItemId(position: Int) =
        viewModelsList[position].hashCode().toLong()

    override fun getItemCount() = min(sourceCount, MAX_HISTORY) + 1

    private fun bindViewHolderState(
        viewModel: ReLearnCardViewModel,
        holder: ReLearnBaseViewHolder,
        position: Int
    ) {
        if (bindingJobs[position] != null) {
            bindingJobs[position].job.cancel()
        }
        val job = viewModel.getViewState().onEach {
            if (it is ReLearnCardViewState.ReLearnTranslationState) {
                Timber.v("Binding view state @ position $position: ${it.reLearnTranslation.sourceText} - $it")
            } else {
                Timber.v("Binding view state @ position $position: $it")
            }

            holder.bind(it)
        }.launchIn(coroutineScope)

        bindingJobs[position] = HolderAndBindingJob(holder, job)
    }

    override fun onBindViewHolder(holder: ReLearnBaseViewHolder, position: Int) {
        Timber.v("Binding view holder at position: $position")
        val viewModel = viewModelsList[position]
        if (isNextReLearn(position)) {
            viewModel.loadInitialNextReLearn()
        } else {
            viewModel.loadInitialNthHistory(itemCount - position - 1)
        }

        bindViewHolderState(viewModel, holder, position)

        holder.actionsListener = {
            handleAction(it, position)
        }
    }

    override fun onViewRecycled(holder: ReLearnBaseViewHolder) {
        super.onViewRecycled(holder)

        var holderPosition: Int? = null
        bindingJobs.forEach { position, job ->
            if (job.holder === holder) {
                holderPosition = position
                return@forEach
            }
        }
        holderPosition?.apply {
            Timber.v("Recycling view holder, previously used at position $holderPosition")
            bindingJobs[this].job.cancel()
            bindingJobs.remove(this)
        }
    }

    private fun handleEffect(effect: ReLearnCardEffect, position: Int) {
        when (effect) {
            is ReLearnCardEffect.Launch -> sendEffect(
                ReLearnAdapterEffect.LaunchReLearnEffect(
                    effect.reLearnTranslation
                )
            )
            is ReLearnCardEffect.ReLearnDeleted -> onReLearnDeleted(
                effect.reLearnTranslation,
                position
            )
        }
    }

    private fun handleAction(action: ReLearnAction, position: Int) {
        Timber.v("Adapter action $action at position $position")

        val viewModel = viewModelsList[position]

        when (action) {
            ReLearnAction.ViewReLearn -> {
                viewModel.launchReLearn()
            }
            ReLearnAction.AcceptReLearn -> viewModel.acceptReLearn()
            ReLearnAction.AcceptAnimationFinished -> {
                addNewPageForNextReLearn()
                //remove last item in history as it would be limit+1.
                removeLastHistoryPage()
                //signal host to scroll
                sendEffect(ReLearnAdapterEffect.ShowNextReLearnEffect)
            }
            ReLearnAction.DeleteReLearn -> viewModel.deleteReLearn()
            is ReLearnAction.SetExpanded -> {
                viewModel.setExpanded(action.isExpanded)
            }
        }
    }

    private fun removeViewModelAt(position: Int) {
        Timber.d("Removing viewmodel @ position $position")

        if (bindingJobs[position] != null) {
            bindingJobs[position].job.cancel()
            bindingJobs.remove(position)
        }

        viewModelsList.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun removeLastHistoryPage() {
        removeViewModelAt(0)
    }

    private fun addViewModelAt(position: Int) =
        viewModelsFactory.create(ReLearnCardViewModel::class.java).also {
            Timber.v("Adding new view model @ position $position")
            viewModelsList.add(position, it)
            notifyItemInserted(position)
        }

    private fun onReLearnDeleted(
        relearn: ReLearnTranslation,
        position: Int
    ) {
        Timber.d("View model deleted @ position $position")
        //remove viewmodel holding the deleted data, don't bind the deleting state
        removeViewModelAt(position)

        sendEffect(ReLearnAdapterEffect.ReLearnDeletedEffect(relearn, position))

        if (isNextReLearn(position)) {
            //add viewmodel for next relearn
            //at this point we only have 19 items!
            val newViewModel = addViewModelAt(itemCount - 1)
            //actually load next relearn's data
            newViewModel.loadInitialNextReLearn()
            return
        }

        //add new viewmodel at the beginning as we can now load one more in history
        addViewModelAt(0)
    }

    private fun addNewPageForNextReLearn() {
        //add viewmodel for next relearn
        val newViewModel = addViewModelAt(itemCount)

        //actually load next relearn's data
        newViewModel.loadInitialNextReLearn()
    }

    fun undoReLearnDelete(relearn: ReLearnTranslation, position: Int) {
        if (isNextReLearn(position)) {
            removeViewModelAt(itemCount - 1)
        } else {
            removeViewModelAt(0)
        }

        val newViewModel = addViewModelAt(position)
        newViewModel.undeleteReLearn(relearn)

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) =
        if (isNextReLearn(position)) ITEM_TYPE_NEXT else ITEM_TYPE_HISTORY

    private fun isNextReLearn(position: Int) = position == itemCount - 1

    @AssistedInject.Factory
    interface Factory {
        fun create(sourceCount: Int, viewModelStoreOwner: ViewModelStoreOwner): ReLearnAdapter
    }

    companion object {
        private const val ITEM_TYPE_HISTORY = 1
        private const val ITEM_TYPE_NEXT = 2
    }
}
