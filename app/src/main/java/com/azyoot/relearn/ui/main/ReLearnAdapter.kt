package com.azyoot.relearn.ui.main

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.set
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.domain.config.MAX_HISTORY
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.main.relearn.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Provider
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
    private val viewModelProvider: Provider<ReLearnCardViewModel>,
    private val nextReLearnCardFactory: ReLearnNextCardViewHolder.Factory,
    private val historyReLearnCardFactory: ReLearnHistoryCardViewHolder.Factory,
    private val coroutineScope: CoroutineScope,
    @Assisted private val sourceCount: Int
) : RecyclerView.Adapter<ReLearnBaseViewHolder>(), LifecycleOwner {

    private val effectsInternal = MutableLiveData<ReLearnAdapterEffect>()
    val effectsLiveData: LiveData<ReLearnAdapterEffect>
        get() = effectsInternal

    private val viewModels = mutableListOf<ReLearnCardViewModel>()
    private val bindingJobs = SparseArray<Job>()
    private val bindingEffectsJobs = SparseArray<Job>()
    private val viewModelStateJobs = SparseArray<Job>()

    private val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle() = lifecycleRegistry

    init {
        Timber.v("Setting up viewmodels")
        repeat(itemCount) { position ->
            viewModels.add(viewModelProvider.get().also { setupViewModel(it, position) })
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    private fun setupViewModel(viewModel: ReLearnCardViewModel, position: Int) {
        if (viewModelStateJobs[position] != null) viewModelStateJobs[position].cancel()
        viewModelStateJobs[position] = viewModel.getViewState().onEach {
            if (it is ReLearnCardViewState.ReLearnTranslationState) {
                Timber.v("View state update @ position $position: ${it.reLearnTranslation.sourceText} - $it")

                when (it.relearnState) {
                    is ReLearnCardReLearnState.Deleted -> onReLearnDeleted(
                        viewModel,
                        it.reLearnTranslation,
                        position
                    )
                }
            } else {
                Timber.v("View state update @ position $position: $it")
            }

        }.launchIn(coroutineScope)
    }

    private fun reindexViewModels() {
        viewModels.forEachIndexed { index, viewModel ->
            setupViewModel(viewModel, index)
        }
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

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getItemId(position: Int) =
        viewModels[position].hashCode().toLong()

    override fun getItemCount() = min(sourceCount, MAX_HISTORY) + 1

    private fun bindViewHolderState(
        viewModel: ReLearnCardViewModel,
        holder: ReLearnBaseViewHolder,
        position: Int
    ) {
        if (bindingJobs[position] != null) {
            bindingJobs[position].cancel()
        }
        bindingJobs[position] = viewModel.getViewState().onEach {
            if (it is ReLearnCardViewState.ReLearnTranslationState) {
                Timber.v("Binding view state @ position $position: ${it.reLearnTranslation.sourceText} - $it")
            } else {
                Timber.v("Binding view state @ position $position: $it")
            }

            holder.bind(it)
        }.launchIn(coroutineScope)

        if (bindingEffectsJobs[position] != null) {
            bindingEffectsJobs[position].cancel()
        }
        bindingEffectsJobs[position] = viewModel.getEffects().onEach {
            handleEffect(it, position)
        }.launchIn(coroutineScope)
    }

    override fun onBindViewHolder(holder: ReLearnBaseViewHolder, position: Int) {
        Timber.v("Binding view holder at position: $position")
        val viewModel = viewModels[position]
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

    private fun handleEffect(effect: ReLearnCardEffect, position: Int) {
        when (effect) {
            is ReLearnCardEffect.Launch -> effectsInternal.postValue(
                ReLearnAdapterEffect.LaunchReLearnEffect(
                    effect.reLearnTranslation
                )
            )
        }
    }

    private fun handleAction(action: ReLearnAction, position: Int) {
        Timber.v("Adapter action $action at position $position")

        val viewModel = viewModels[position]

        when (action) {
            ReLearnAction.ViewReLearn -> viewModel.launchReLearn()
            ReLearnAction.AcceptReLearn -> viewModel.acceptReLearn()
            ReLearnAction.AcceptAnimationFinished -> {
                //remove last item in history as it would be limit+1.
                removeLastHistoryPage()
                addNewPageForNextReLearn()
                //signal host to scroll
                effectsInternal.postValue(ReLearnAdapterEffect.ShowNextReLearnEffect)
            }
            ReLearnAction.DeleteReLearn -> viewModel.deleteReLearn()
        }
    }

    private fun removeViewModelAt(position: Int) {
        Timber.d("Removing viewmodel @ position $position")
        if (bindingJobs[position] != null) {
            bindingJobs[position].cancel()
            bindingJobs.remove(position)
        }
        if (bindingEffectsJobs[position] != null) {
            bindingEffectsJobs[position].cancel()
            bindingEffectsJobs.remove(position)
        }
        viewModels.removeAt(position)
        notifyItemRemoved(position)
        reindexViewModels()
    }

    private fun removeLastHistoryPage() {
        removeViewModelAt(0)
    }

    private fun addViewModelAt(position: Int) =
        viewModelProvider.get().also {
            Timber.v("Adding new view model @ position $position")
            viewModels.add(position, it)
            notifyItemInserted(position)
            reindexViewModels()
        }

    private fun onReLearnDeleted(
        viewModel: ReLearnCardViewModel,
        relearn: ReLearnTranslation,
        position: Int
    ) {
        Timber.d("View model deleted @ position $position")
        //remove viewmodel holding the deleted data, don't bind the deleting state
        removeViewModelAt(position)

        effectsInternal.postValue(ReLearnAdapterEffect.ReLearnDeletedEffect(relearn, position))

        if (isNextReLearn(position)) {
            addNewPageForNextReLearn()
            return
        }

        //add new viewmodel at the beginning as we can now load one more in history
        addViewModelAt(0)
    }

    private fun addNewPageForNextReLearn() {
        //add viewmodel for next relearn
        val newViewModel = addViewModelAt(itemCount - 1)

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

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(sourceCount: Int): ReLearnAdapter
    }

    companion object {
        private const val ITEM_TYPE_HISTORY = 1
        private const val ITEM_TYPE_NEXT = 2
    }
}
