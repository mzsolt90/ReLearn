package com.azyoot.relearn.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.main.relearn.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Provider
import kotlin.math.min

sealed class ReLearnAdapterActions {
    data class LaunchReLearn(val reLearnTranslation: ReLearnTranslation) : ReLearnAdapterActions()
}

class ReLearnAdapter @AssistedInject constructor(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val viewModelProvider: Provider<ReLearnCardViewModel>,
    private val nextReLearnCardFactory: ReLearnNextCardViewHolder.Factory,
    private val historyReLearnCardFactory: ReLearnHistoryCardViewHolder.Factory,
    @Assisted private val sourceCount: Int
) : RecyclerView.Adapter<ReLearnBaseViewHolder>(), LifecycleOwner {

    private val actionsInternal = MutableLiveData<ReLearnAdapterActions>()
    val actionsLiveData: LiveData<ReLearnAdapterActions>
        get() = actionsInternal

    private val viewModels = mutableListOf<ReLearnCardViewModel>()

    private val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle() = lifecycleRegistry

    init {
        Timber.v("Setting up viewmodels")
        repeat(itemCount) {
            viewModels.add(setupViewModel(viewModelProvider.get(), it))
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    private fun setupViewModel(viewModel: ReLearnCardViewModel, position: Int) =
        viewModel.also { viewModel ->
            viewModel.stateLiveData.removeObservers(this)
            viewModel.stateLiveData.observe(this, Observer {
                notifyItemChanged(position)
            })
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
        recyclerView.itemAnimator = null
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getItemId(position: Int) =
        viewModels[position].hashCode().toLong()

    override fun getItemCount() = min(sourceCount, MAX_HISTORY) + 1

    override fun onBindViewHolder(holder: ReLearnBaseViewHolder, position: Int) {
        Timber.v("Binding view holder at position: $position")
        val viewModel = viewModels[position]
        if (isNextReLearn(position)) {
            if (viewModel.currentState is ReLearnCardViewState.Initial) {
                viewModel.loadNextReLearn()
            }
        } else {
            if (viewModel.currentState is ReLearnCardViewState.Initial) {
                viewModel.loadNthHistory(itemCount - position - 1)
            }
        }
        holder.bind(viewModel.currentState)
        holder.actionsListener = {
            handleAction(it, position)
        }
    }

    private fun handleAction(action: ReLearnAction, position: Int) {
        Timber.v("Adapter action $action at position $position")
        if (!isNextReLearn(position)) return
        val viewModel = viewModels[position]
        val state = viewModel.currentState

        when (action) {
            ReLearnAction.ViewReLearn -> launchReLearn((state as ReLearnCardViewState.ReLearnTranslationState).reLearnTranslation)
            ReLearnAction.AcceptReLearn -> acceptReLearn(
                viewModel,
                (state as ReLearnCardViewState.ReLearnTranslationState).reLearnTranslation
            )
            ReLearnAction.AcceptAnimationFinished -> addNewPageForNextReLearn()
        }
    }

    private fun launchReLearn(relearn: ReLearnTranslation) {
        actionsInternal.postValue(ReLearnAdapterActions.LaunchReLearn(relearn))
    }

    private fun acceptReLearn(viewModel: ReLearnCardViewModel, relearn: ReLearnTranslation) {
        viewModel.acceptReLearn()
    }

    private fun addNewPageForNextReLearn() {
        viewModels[0].stateLiveData.removeObservers(this)
        viewModels.removeAt(0)

        viewModels.forEachIndexed { index, viewModel ->
            setupViewModel(viewModel, index)
        }

        val newViewModel = setupViewModel(viewModelProvider.get(), itemCount - 1)
        viewModels.add(newViewModel)

        notifyItemRemoved(0)
        notifyItemInserted(itemCount)

        newViewModel.loadNextReLearn()
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
        const val MAX_HISTORY = 10

        private const val ITEM_TYPE_HISTORY = 1
        private const val ITEM_TYPE_NEXT = 2
    }
}
