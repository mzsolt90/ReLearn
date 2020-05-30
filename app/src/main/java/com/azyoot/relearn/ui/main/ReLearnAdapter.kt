package com.azyoot.relearn.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.domain.config.MAX_HISTORY
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.main.relearn.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber
import javax.inject.Provider
import kotlin.math.min

sealed class ReLearnAdapterAction {
    data class LaunchReLearn(val reLearnTranslation: ReLearnTranslation) : ReLearnAdapterAction()
    data class ReLearnDeletedEffect(val relearn: ReLearnTranslation, val position: Int): ReLearnAdapterAction()
    object ShowNextReLearn : ReLearnAdapterAction()
}

class ReLearnAdapter @AssistedInject constructor(
    private val viewModelProvider: Provider<ReLearnCardViewModel>,
    private val nextReLearnCardFactory: ReLearnNextCardViewHolder.Factory,
    private val historyReLearnCardFactory: ReLearnHistoryCardViewHolder.Factory,
    @Assisted private val sourceCount: Int
) : RecyclerView.Adapter<ReLearnBaseViewHolder>(), LifecycleOwner {

    private val actionsInternal = MutableLiveData<ReLearnAdapterAction>()
    val actionsLiveData: LiveData<ReLearnAdapterAction>
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

        val viewModel = viewModels[position]
        val state = viewModel.currentState
        val relearn = (state as? ReLearnCardViewState.ReLearnTranslationState)?.reLearnTranslation

        when (action) {
            ReLearnAction.ViewReLearn -> launchReLearn(relearn!!)
            ReLearnAction.AcceptReLearn -> acceptReLearn(
                viewModel,
                (state as ReLearnCardViewState.ReLearnTranslationState).reLearnTranslation
            )
            ReLearnAction.AcceptAnimationFinished ->  {
                //remove last item in history as it would be limit+1.
                removeLastHistoryPage()
                addNewPageForNextReLearn()
                //signal host to scroll
                actionsInternal.postValue(ReLearnAdapterAction.ShowNextReLearn)
            }
            ReLearnAction.DeleteReLearn -> deleteReLearn(viewModel, relearn!!, position)
        }
    }

    private fun launchReLearn(relearn: ReLearnTranslation) {
        actionsInternal.postValue(ReLearnAdapterAction.LaunchReLearn(relearn))
    }

    private fun acceptReLearn(viewModel: ReLearnCardViewModel, relearn: ReLearnTranslation) {
        viewModel.acceptReLearn()
    }

    private fun reindexViewModels() {
        viewModels.forEachIndexed { index, viewModel ->
            setupViewModel(viewModel, index)
        }
    }

    private fun removeViewModelAt(position: Int){
        viewModels[position].stateLiveData.removeObservers(this)
        viewModels.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun removeLastHistoryPage(){
        removeViewModelAt(0)

        //reindex viewmodels, but no need to reload their data
        reindexViewModels()
    }

    private fun addViewModelAt(position: Int) =
        setupViewModel(viewModelProvider.get(), position).also {
            viewModels.add(position, it)
            notifyItemInserted(position)
        }

    private fun deleteReLearn(viewModel: ReLearnCardViewModel, relearn: ReLearnTranslation, position: Int) {
        //remove viewmodel holding the deleted data, don't bind the deleting state
        removeViewModelAt(position)

        //do the delete
        viewModel.deleteReLearn()

        actionsInternal.postValue(ReLearnAdapterAction.ReLearnDeletedEffect(relearn, position))

        if(isNextReLearn(position)){
            //reindex other viewmodels, no need to reload their data
            reindexViewModels()

            addNewPageForNextReLearn()
            return
        }

        //add new viewmodel at the beginning as we can now load one more in history
        addViewModelAt(0)

        //reindex other viewmodels, no need to reload their data
        reindexViewModels()
    }

    private fun addNewPageForNextReLearn() {
        //add viewmodel for next relearn
        val newViewModel = addViewModelAt(itemCount - 1)

        //actually load next relearn's data
        newViewModel.loadNextReLearn()
    }

    fun undoReLearnDelete(relearn: ReLearnTranslation, position: Int){
        if(isNextReLearn(position)) {
            removeViewModelAt(itemCount - 1)
        } else {
            removeViewModelAt(0)
        }

        val newViewModel = addViewModelAt(position)
        newViewModel.undeleteReLearn(relearn)

        //reindex other viewmodels, no need to reload their data
        reindexViewModels()
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
